/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.providers;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import pl.datamatica.traccar.api.dtos.in.EditAppVersionsDto;
import pl.datamatica.traccar.model.AppVersions;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;

/**
 *
 * @author piotrkrzeszewski
 */
public class AppVersionsProvider {
    private final EntityManager em;
    private User requestUser;
    private AppVersions versions;

    public AppVersionsProvider(EntityManager em) {
        this.em = em;
    }
    
    public void setRequestUser(User user) {
        this.requestUser = user;
    }
    
    public AppVersions get() {
        if(versions == null) {
            TypedQuery<AppVersions> tq = em.createQuery("Select x from AppVersions x", 
                    AppVersions.class);
            tq.setMaxResults(1);
            List<AppVersions> result = tq.getResultList();
            versions = result.isEmpty() ? new AppVersions() : result.get(0);
        }
        return versions;
    }
    
    public void editAppVersions(EditAppVersionsDto editDto) throws ProviderException {
        if (!requestUser.hasPermission(UserPermission.SERVER_MANAGEMENT))
            throw new ProviderException(ProviderException.Type.ACCESS_DENIED);
        
        AppVersions av = get();
        
        av.setIosVersion(editDto.getIosVersion());
        av.setIosRequiredVersion(editDto.getIosRequired());
        av.setAndroidVersion(editDto.getAndroidVersion());
        av.setAndroidRequiredVersion(editDto.getAndroidRequired());
        
        av.setMessageKey(editDto.getMessageKey());
        av.setLocalizedMessage(editDto.getLocalizedMessage());
        av.setMessageUrl(editDto.getMessageUrl());
        
        em.persist(av);
    }
}
