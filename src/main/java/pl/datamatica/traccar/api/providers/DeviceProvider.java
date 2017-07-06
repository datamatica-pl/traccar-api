/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Maintenance;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserDeviceStatus;

public class DeviceProvider extends ProviderBase {
    private User requestUser;
    private ImeiProvider imeis;
    private Logger logger;
    
    public DeviceProvider(EntityManager em, User requestUser, ImeiProvider imeis) {
        super(em);
        this.requestUser = requestUser;
        this.imeis = imeis;
        logger = DbLog.getLogger();
    }
    
    public Device getDevice(long id) throws ProviderException {
        return get(Device.class, id, this::isVisible);
    }
    
    public Device getDeviceByImei(String imei) {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x where x.uniqueId = :imei", Device.class);
        tq.setParameter("imei", imei);
        List<Device> devices = tq.getResultList();
        if(devices.isEmpty())
            return null;
        return devices.get(0);
    }
    
    public Stream<Device> getAllAvailableDevices() {
        List<Device> devices = null;
        if(requestUser.getAdmin())
            devices = getAllDevices();
        else
            devices = new ArrayList<>(requestUser.getAllAvailableDevices());
        
        loadAlarmStatus(devices);
        loadMaintenances(devices);
        
        return devices.stream();
    }

    private void loadMaintenances(List<Device> devices) {
        List<Maintenance> maintenaces = em.createQuery(
                "SELECT m FROM Maintenance m WHERE m.device IN :devices ORDER BY m.indexNo ASC", Maintenance.class)
                .setParameter("devices", devices)
                .getResultList();
        for (Maintenance maintenance : maintenaces) {
            Device device = maintenance.getDevice();
            if (device.getMaintenances() == null) {
                device.setMaintenances(new ArrayList<>());
            }
            device.getMaintenances().add(maintenance);
        }
    }

    private void loadAlarmStatus(List<Device> devices) {
        TypedQuery<UserDeviceStatus> alarmQuery = em.createQuery(
                "FROM UserDeviceStatus x "
                        + "WHERE x.id.user = :user AND x.id.device in (:devices)", UserDeviceStatus.class);
        alarmQuery.setParameter("user", requestUser);
        alarmQuery.setParameter("devices", devices);
        Map<Device, UserDeviceStatus> statesMap = new HashMap<>();
        for(UserDeviceStatus x : alarmQuery.getResultList())
            statesMap.put(x.getDevice(), x);
        for(Device d : devices) {
            UserDeviceStatus status = statesMap.get(d);
            if(status != null) {
                d.setUnreadAlarms(status.hasUnreadAlarms());
                d.setLastAlarmsCheck(status.getLastCheck());
            }
        }
    }

    public Device createDevice(String imei) throws ProviderException {
        if(!isImeiValid(imei))
            throw new ProviderException(Type.INVALID_IMEI);
        
        Device existing = getDeviceByImei(imei);
        if(existing != null) {
            if(!existing.isDeleted())
                throw new ProviderException(Type.DEVICE_ALREADY_EXISTS);
            hardDelete(existing);
        }
        
        Device device = new Device();
        device.setName(createGpsName());
        device.setUniqueId(imei);
        device.setUsers(Collections.singleton(requestUser));
        device.setIconId(4L);
        device.setOwner(requestUser);
        em.persist(device);
        
        logger.info("{} created device {} (id={})", 
                requestUser.getLogin(), device.getName(), device.getId());
        
        return device;
    }

    private static String createGpsName() {
        Random random = new Random();
        return GPS_NAME_PREFIX+(random.nextInt(99)+1);
    }
    private static final String GPS_NAME_PREFIX = "gps-";
    
    public void delete(long id) throws ProviderException {
        boolean shouldManageTransaction = !em.getTransaction().isActive();
        if(shouldManageTransaction)
            em.getTransaction().begin();
        Device device = getDevice(id);
        if(!isVisible(device))
            throw new ProviderException(Type.ACCESS_DENIED);
        if(device.isDeleted())
            throw new ProviderException(Type.ALREADY_DELETED);
        if(representsOwner(device)) {
            logger.info("{} deleted device {} (id={})",
                    requestUser.getLogin(), device.getName(), device.getId());
            device.setDeleted(true);
        } else {
            logger.info("{} stopped seeing {} (id={})",
                    requestUser.getLogin(), device.getName(), device.getId());
            device.getUsers().remove(requestUser);
        }
        em.persist(device);
        if(shouldManageTransaction)
            em.getTransaction().commit();
    }

    private boolean representsOwner(Device device) {
        return requestUser.getAdmin() 
               || requestUser.equals(device.getOwner())
               || requestUser.getAllManagedUsers().contains(device.getOwner());
    }
    
    private boolean isVisible(Device device) {
        if(requestUser.getAdmin())
            return true;
        return getAllAvailableDevices().anyMatch(d -> d.equals(device));
    }
    
    private List<Device> getAllDevices() {
        TypedQuery<Device> tq = em.createQuery("Select x from Device x", Device.class);
        return tq.getResultList();
    }
    
    private boolean isImeiValid(String imei) {
        return imeis.isImeiRegistered(imei);
    }
    
    private void hardDelete(Device device) {
        device.getUsers().clear();
        device.setLatestPosition(null);
        em.flush();
        
        Query query = em.createQuery("DELETE FROM DeviceEvent x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("DELETE FROM Position x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("SELECT g FROM GeoFence g WHERE :device MEMBER OF g.devices");
        query.setParameter("device", device);
        for (GeoFence geoFence : (List<GeoFence>) query.getResultList()) {
            geoFence.getDevices().remove(device);
        }
        em.flush();

        query = em.createQuery("DELETE FROM Maintenance x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("DELETE FROM Sensor x WHERE x.device = :device");
        query.setParameter("device", device);
        query.executeUpdate();

        query = em.createQuery("SELECT x FROM Report x WHERE :device MEMBER OF x.devices");
        query.setParameter("device", device);
        List<Report> reports = query.getResultList();
        for (Report report : reports) {
            report.getDevices().remove(device);
        }
        
        query = em.createNativeQuery("Delete from devices where id = ?");
        query.setParameter(1, device.getId());
        query.executeUpdate();
    }

    private static final Double NauticMilesToKilometersMultiplier = 0.54;
    
    public void updateDevice(long id, EditDeviceDto deviceDto) throws ProviderException {
        Device device = getDevice(id);
        
        device.setName(deviceDto.getDeviceName());
        device.setDeviceModelId(deviceDto.getDeviceModelId());
        device.setIconId(deviceDto.getIconId());
        device.setCustomIconId(deviceDto.getCustomIconId());
        device.setColor(deviceDto.getColor());
        device.setPhoneNumber(deviceDto.getPhoneNumber());
        device.setPlateNumber(deviceDto.getPlateNumber());
        device.setDescription(deviceDto.getDescription());
        
        if(deviceDto.getSpeedLimit() != null)
            device.setSpeedLimit(deviceDto.getSpeedLimit() * NauticMilesToKilometersMultiplier);
        else
            device.setSpeedLimit(null);
        
        device.setFuelCapacity(deviceDto.getFuelCapacity());
        
        em.persist(device);
        
        logger.info("{} updated device {} (id={})", 
                requestUser.getEmail(), device.getName(), device.getId());
    }
}
