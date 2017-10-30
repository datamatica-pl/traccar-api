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
import pl.datamatica.traccar.api.dtos.in.EditReportDto;
import pl.datamatica.traccar.api.providers.ProviderException.Type;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import pl.datamatica.traccar.model.Report;
import pl.datamatica.traccar.model.ReportType;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author ŁŁ
 */
public class ReportProvider extends ProviderBase {
    
    private User requestUser;
    private DeviceProvider dp;
    private GeoFenceProvider gfp;
    
    public ReportProvider(EntityManager em, User requestUser, DeviceProvider dp,
            GeoFenceProvider gfp) {
        super(em);
        this.requestUser = requestUser;
        this.dp = dp;
        this.gfp = gfp;
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
    
    public Report createReport(EditReportDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Report toSave = new Report();
        toSave.setDevices(new HashSet<>());
        toSave.setGeoFences(new HashSet<>());
        editReport(toSave, dto);
        toSave.setUsers(new HashSet<>(1));
        toSave.getUsers().add(requestUser);
        em.persist(toSave);
        
        return toSave;
    }
    
    public void updateReport(long id, EditReportDto dto) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Report toSave = em.find(Report.class, id);
        editReport(toSave, dto);
    }
    
    private void editReport(Report report, EditReportDto dto) throws ProviderException {
        report.setName(dto.getName());
        report.setType(ReportType.valueOf(dto.getReportType()));
        report.setFromDate(dto.getFromDate());
        report.setToDate(dto.getToDate());
        report.setDisableFilter(dto.isDisableFilter());
        report.setIncludeMap(dto.isIncludeMap());
        if(requestUser.hasPermission(UserPermission.ALL_DEVICES))
            report.getDevices().clear();
        else
            report.getDevices().removeAll(requestUser.getAllAvailableDevices());
        for(long did : dto.getDeviceIds())
            report.getDevices().add(dp.getEditableDevice(did));
        if(requestUser.hasPermission(UserPermission.ALL_GEOFENCES))
            report.getGeoFences().clear();
        else
            report.getGeoFences().removeAll(requestUser.getAllAvailableGeoFences());
        for(long gid : dto.getGeofenceIds())
            report.getGeoFences().add(gfp.getEditableGeofence(gid));
    }
        
    
    public void removeReport(long id) throws ProviderException {
        if(!requestUser.hasPermission(UserPermission.REPORTS))
            throw new ProviderException(Type.ACCESS_DENIED);
        
        Report toRemove = get(Report.class, id, r -> true);
        em.remove(toRemove);
    }
}
