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
package pl.datamatica.traccar.api.dtos;

public class MessageKeys {    
    public static final String ERR_ACCESS_DENIED = "err_access_denied";
    public static final String ERR_NOT_FOUND = "err_not_found";
    
    public static final String ERR_TOKEN_REJECTED = "err_token_rejected";
    public static final String ERR_TOKEN_NOT_PROVIDED = "err_token_not_provided";
    
    public static final String ERR_IMEI_NOT_PROVIDED = "err_imei_not_provided";
    public static final String ERR_INVALID_IMEI = "err_invalid_imei";
    public static final String ERR_DEVICE_DATA_NOT_PROVIDED = "err_device_data_not_provided";
    public static final String ERR_DEVICE_COLOR_NOT_PROVIDED = "err_device_color_not_provided";
    public static final String ERR_INVALID_DEVICE_COLOR_FORMAT = "err_invalid_device_color_format";
    public static final String ERR_DEVICE_MODEL_ID_NOT_PROVIDED = "err_device_model_id_not_provided";
    public static final String ERR_DEVICE_NAME_NOT_PROVIDED = "err_device_name_not_provided";
    public static final String ERR_DEVICE_ICON_ID_NOT_PROVIDED = "err_device_icon_id_not_provided";
    
    public static final String ERR_USER_ALREADY_EXISTS = "err_user_already_exists";
    public static final String ERR_REGISTRATION_DATA_NOT_PROVIDED = "err_registration_data_not_provided";
    public static final String ERR_EMAIL_NOT_PROVIDED = "err_email_not_provided";
    public static final String ERR_PASSWORD_NOT_PROVIDED = "err_password_not_provided";
    public static final String ERR_CHECK_MARKETING_NOT_PROVIDED = "err_check_marketing_not_provided";
    
    public static final String ERR_ACCOUNT_BLOCKED = "err_account_blocked";
    public static final String ERR_ACCOUNT_EXPIRED = "err_account_expired";
    
    
    public static final String ERR_GEOFENCE_DATA_NOT_PROVIDED = "err_geofence_data_not_provided";
    public static final String ERR_GEOFENCE_ALL_DEVICES_NOT_PROVIDED = "err_all_devices_not_provided";
    public static final String ERR_GEOFENCE_COLOR_NOT_PROVIDED = "err_geofence_color_not_provided";
    public static final String ERR_GEOFENCE_NAME_NOT_PROVIDED = "err_geofence_name_not_provided";
    public static final String ERR_INVALID_COLOR_FORMAT = "err_invalid_color_format";
    public static final String ERR_GEOFENCE_TYPE_NOT_PROVIDED = "err_geofence_type_not_provided";
    public static final String ERR_INVALID_GEOFENCE_TYPE_FORMAT = "err_invalid_geofence_type_format";
    public static final String ERR_GEOFENCE_POINTS_NOT_PROVIDED = "err_geofence_points_not_provided";
    public static final String ERR_GEOFENCE_RADIUS_NOT_PROVIDED = "err_circle_radius_not_provided";
    public static final String ERR_TOO_FEW_GEOFENCE_POINTS = "err_too_few_geofence_points";
}
