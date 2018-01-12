/*
 *   Copyright (C) 2016  Datamatica (dev@datamatica.pl)
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
package pl.datamatica.traccar.api.metadata.model;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.datamatica.traccar.api.TraccarConfig;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;
import pl.datamatica.traccar.api.providers.ImeiProvider;
import pl.datamatica.traccar.api.responses.HttpHeaders;
import pl.datamatica.traccar.api.validators.MetadataValidator;

/**
 *
 * @author Jan Usarek
 */
public class LocalOrRemoteImeiNumber {

    private final String imeiStr;
    private final ImeiProvider imeiProvider;
    private final Logger logger;
    private final MetadataValidator mdv;
    private String deviceModel = "";
    private final String imeiModelCheckURL;
    private final String imeiManagerUser;
    private final String imeiManagerPassword;
    private boolean isImeiValid = false;

    public LocalOrRemoteImeiNumber(String imei, ImeiProvider iProv) {
        this.logger = LoggerFactory.getLogger(LocalOrRemoteImeiNumber.class);
        this.mdv = new MetadataValidator();
        imeiStr = imei;
        imeiProvider = iProv;
        String tempReqUrl = "";
        String tempReqUser = "";
        String tempReqPass = "";

        try {
            final TraccarConfig traccarConf = TraccarConfig.getInstance();
            tempReqUrl = traccarConf.getStringParam("api.imei_manager.check_imei_url");
            if (StringUtils.isNotBlank(tempReqUrl)) {
                tempReqUrl += imeiStr + "/deviceModel";
            }
            tempReqUser = traccarConf.getStringParam("api.imei_manager.imei_manager_user");
            tempReqPass = traccarConf.getStringParam("api.imei_manager.imei_manager_password");
        } catch (ConfigLoadException | IllegalAccessException | InvocationTargetException ex) {
            logger.error("Can't get config object or one of it's properties." + ex.getMessage());
        }

        imeiModelCheckURL = tempReqUrl;
        imeiManagerUser = tempReqUser;
        imeiManagerPassword = tempReqPass;

        this.initializeDeviceModelAndIsValid();
    }

    private void initializeDeviceModelAndIsValid() {
        if (shouldCheckImeiRemotely()) {
            try {
                URL myURL = new URL(imeiModelCheckURL);
                HttpURLConnection connection = (HttpURLConnection) myURL.openConnection();
                connection.setRequestProperty("Authorization", "Basic "
                        + HttpHeaders.getEncodedCredentials(imeiManagerUser, imeiManagerPassword));
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(30000);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    isImeiValid = true;
                    deviceModel = getRequestResponse(connection);
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    logger.error("IMEI manager can't check IMEI remotely, authorization failed."
                            + " Check user, password, and privileges to resource management.");
                }
            } catch (MalformedURLException mue) {
                logger.error("IMEI manager can't check IMEI remotely because of malformed URL: " + mue.getMessage());
            } catch (IOException ioe) {
                logger.error("IMEI manager can't check IMEI remotely: " + ioe.getMessage());
            }
        } else {
            ImeiNumber imeiNum = imeiProvider.getImeiByImeiString(imeiStr);
            if (imeiNum != null && !imeiNum.getIsDeleted()) {
                isImeiValid = true;
                deviceModel = imeiNum.getDeviceModel();
            }
        }
    }

    private String getRequestResponse(HttpURLConnection connection) throws IOException {
        try (InputStream is = connection.getInputStream()) {
            int intEl;
            StringBuilder sb = new StringBuilder();
            while ((intEl = is.read()) != -1) {
                sb.append((char) intEl);
            }
            return sb.toString();
        } catch (IOException e) {
            throw e;
        }
    }

    private boolean shouldCheckImeiRemotely() {
        return StringUtils.isNoneBlank(imeiModelCheckURL, imeiManagerUser);
    }

    public String getDeviceModelName() {
        return deviceModel;
    }

    public boolean isImeiValidAndRegistered() {
        if (!mdv.isImeiValid(imeiStr)) {
            return false;
        }

        return isImeiValid;
    }

}
