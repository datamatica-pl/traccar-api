/*
 *  Copyright (C) 2017  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class ReportProvider extends ProviderBase {
    
    private User requestUser;
    
    public ReportProvider(EntityManager em, User requestUser) {
        super(em);
        this.requestUser = requestUser;
    }
    
    public Stream<Report> getReports() throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        if (requestUser.hasPermission(UserPermission.ALL_DEVICES)) {
            return em.createQuery("SELECT x FROM Report x", Report.class).getResultList().stream();
        } else {
            return requestUser.getAllAvailableReports().stream();
        }
    }
    
    public Report createReport(Report report) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
            
        Report toSave = new Report().copyFrom(report);

        toSave.setDevices(new HashSet<Device>(report.getDevices().size()));
        for(Device d : report.getDevices())
            toSave.getDevices().add(new Device(d));
        toSave.setGeoFences(new HashSet<GeoFence>(report.getGeoFences().size()));
        for(GeoFence gf : report.getGeoFences())
            toSave.getGeoFences().add(new GeoFence().copyFrom(gf));
        toSave.setUsers(new HashSet<User>(1));
        toSave.getUsers().add(requestUser);
        em.persist(toSave);
        
        return toSave;
    }
    
    public void updateReport(long id, Report report) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Report toSave = em.find(Report.class, id);

        toSave.copyFrom(report);
        processDevicesAndGeoFences(report, toSave);
    }
    
    private void processDevicesAndGeoFences(Report report, Report toSave) {
        for (Device device : report.getDevices()) {
            if (!toSave.getDevices().contains(device)) {
                toSave.getDevices().add(em.find(Device.class, device.getId()));
            }
        }
        for (Iterator<Device> it = toSave.getDevices().iterator(); it.hasNext(); ) {
            if (!report.getDevices().contains(it.next())) {
                it.remove();
            }
        }

        for (GeoFence geoFence : report.getGeoFences()) {
            if (!toSave.getGeoFences().contains(geoFence)) {
                toSave.getGeoFences().add(em.find(GeoFence.class, geoFence.getId()));
            }
        }
        for (Iterator<GeoFence> it = toSave.getGeoFences().iterator(); it.hasNext(); ) {
            if (!report.getGeoFences().contains(it.next())) {
                it.remove();
            }
        }
    }
    
    public void removeReport(long id) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Report toRemove = get(Report.class, id, r -> true);
        em.remove(toRemove);
    }
}
