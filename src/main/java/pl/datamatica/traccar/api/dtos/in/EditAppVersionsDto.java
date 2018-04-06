/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.datamatica.traccar.api.dtos.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;

/**
 *
 * @author piotrkrzeszewski
 */
public class EditAppVersionsDto {
    
    private final String androidVersion;
    private final String androidRequired;
    private final String iosVersion;
    private final String iosRequired;
    private final String messageKey;
    private final String localizedMessage;
    private final String messageUrl;

    public EditAppVersionsDto(String androidVersion, String androidRequired, String iosVersion, 
            String iosRequired, String messageKey, String localizedMessage, String messageUrl) {
        this.androidVersion = androidVersion;
        this.androidRequired = androidRequired;
        this.iosVersion = iosVersion;
        this.iosRequired = iosRequired;
        this.messageKey = messageKey;
        this.localizedMessage = localizedMessage;
        this.messageUrl = messageUrl;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getAndroidRequired() {
        return androidRequired;
    }

    public String getIosVersion() {
        return iosVersion;
    }

    public String getIosRequired() {
        return iosRequired;
    }

    public String getMessageKey() {
        return messageKey;
    }
    
    public String getLocalizedMessage() {
        return localizedMessage;
    }

    public String getMessageUrl() {
        return messageUrl;
    }
    
    public static List<ErrorDto> validate(EditAppVersionsDto dto) {
        if (dto == null)
            return Collections.singletonList(new ErrorDto(MessageKeys.ERR_DATA_NOT_PROVIDED));
        
        List<ErrorDto> errors = new ArrayList<>();
        if (dto.iosVersion == null ||
                dto.iosRequired == null ||
                dto.androidVersion == null ||
                dto.androidRequired == null) {
            errors.add(new ErrorDto(MessageKeys.ERR_APPVERSIONS_VERSION_NOT_PROVIDED));
        }
        else {
            // if versions are provided then check their format
            String versionRegex = "^(\\d+)\\.(\\d+)\\.(\\d+)$";
            if (!dto.iosVersion.matches(versionRegex) ||
                    !dto.iosRequired.matches(versionRegex) ||
                    !dto.androidVersion.matches(versionRegex) ||
                    !dto.androidRequired.matches(versionRegex))
                errors.add(new ErrorDto(MessageKeys.ERR_APPVERSIONS_VERSION_REGEX_MISMATCH));
        }
        if ((dto.messageKey == null || dto.messageKey.isEmpty()) && 
                (dto.messageUrl != null || dto.localizedMessage != null))
            errors.add(new ErrorDto(MessageKeys.ERR_APPVERSIONS_MESSAGE_KEY_IS_REQUIRED_FOR_MESSAGES));
        if (dto.messageUrl != null && 
                (!dto.messageUrl.startsWith("http://") && !dto.messageUrl.startsWith("https://")))
            errors.add(new ErrorDto(MessageKeys.ERR_APPVERSIONS_MESSAGE_URL_INVALID_PROTOCOL));
        return errors;
    }
}
