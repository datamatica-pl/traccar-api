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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.TraccarConfig;
import pl.datamatica.traccar.api.dtos.ImeiNumberDto;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;
import pl.datamatica.traccar.api.metadata.model.ImeiNumber;
import pl.datamatica.traccar.api.responses.HttpHeaders;
import pl.datamatica.traccar.api.validators.MetadataValidator;

/**
 *
 * @author Jan Usarek
 */
public class ImeiProvider {
    private final EntityManager emMetadata;
    private static final Logger logger = LoggerFactory.getLogger(ImeiProvider.class);
    
    public ImeiProvider(EntityManager emm) {
        this.emMetadata = emm;
    }
    
    public List<ImeiNumber> getAllImeis() {
        TypedQuery<ImeiNumber> tq = emMetadata.createQuery("Select x from ImeiNumber x", ImeiNumber.class);
        return tq.getResultList();
    }
    
    public ImeiNumber getImeiById(long imeiId) {
        List<ImeiNumber> imeis  = emMetadata.createQuery("SELECT x FROM ImeiNumber x WHERE x.id = :imeiId", ImeiNumber.class)
                                        .setParameter("imeiId", imeiId)
                                        .setMaxResults(1)
                                        .getResultList();
        
        if (imeis.isEmpty()) {
            return null;
        } else {
            return imeis.get(0);
        }
    }
    
    public void saveImeiNumber(final ImeiNumber imei) {
        emMetadata.persist(imei);
    }
    
    public ImeiNumber getImeiByImeiString(final String imeiStr) {
        try {
            ImeiNumber imei = emMetadata.createQuery("SELECT x FROM ImeiNumber x WHERE x.imei = :imeiNumber", ImeiNumber.class)
                                        .setParameter("imeiNumber", imeiStr)
                                        .getSingleResult();
            return imei;
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    public ImeiNumber setNewImeiNumber(ImeiNumber imei, final ImeiNumberDto imeiDto) {
        imei.setImei(imeiDto.getImei());
        imei = updateImeiNumber(imei, imeiDto);
        
        return imei;
    }
    
    public ImeiNumber updateImeiNumber(ImeiNumber imei, final ImeiNumberDto imeiDto) {
        imei.setEmail(imeiDto.getEmail());
        imei.setContactPhone(imeiDto.getContactPhone());
        imei.setFirstName(imeiDto.getFirstName());
        imei.setLastName(imeiDto.getLastName());
        imei.setInvoiceNumber(imeiDto.getInvoiceNumber());
        imei.setComment(imeiDto.getComment());
        
        return imei;
    };
    
    public boolean isImeiRegistered(String imeiStr) {
        try {
            final TraccarConfig traccarConf = TraccarConfig.getInstance();
            final String imeiExistenceCheckURL = traccarConf.getStringParam("api.imei_manager.check_imei_existence_url");
            final String imeiManagerUser = traccarConf.getStringParam("api.imei_manager.imei_manager_user");
            final String imeiManagerPassword = traccarConf.getStringParam("api.imei_manager.imei_manager_password");
            final MetadataValidator mdv = new MetadataValidator();
            
            if (!mdv.isImeiValid(imeiStr)) {
                throw new IllegalArgumentException("Invalid IMEI, only digits are valid in IMEI.");
            }
            
            if (imeiExistenceCheckURL.equals("")) {
                return isImeiRegisteredLocally(imeiStr);
            } else {
                URL myURL = new URL(imeiExistenceCheckURL + imeiStr);
                HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
                connection.setRequestProperty("Authorization", "Basic " 
                                            + HttpHeaders.getEncodedCredentials(imeiManagerUser, imeiManagerPassword));
                connection.setRequestMethod("HEAD");

                int responseCode = connection.getResponseCode();
                return responseCode == HttpURLConnection.HTTP_OK;
            }
        } catch (IOException | ConfigLoadException | IllegalAccessException | InvocationTargetException e) {
            logger.error(String.format("Existence of IMEI %s can't be check by API, trying to check in local DB.", imeiStr), e);
            return isImeiRegisteredLocally(imeiStr);
        }
    }
    
    // Check local database to determine, whether IMEI exists
    public boolean isImeiRegisteredLocally(String imeiStr) {
        ImeiNumber imei = getImeiByImeiString(imeiStr);
        if (imei == null) {
            return false;
        } else {
            return !imei.getIsDeleted(); // Treat softly deleted IMEI's as not registered, always,
                                         // regardless of state of softDelete filter
        }
    }
}
