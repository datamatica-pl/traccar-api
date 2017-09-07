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
    
    public static final String ERR_AUTH_INVALID_HEADER_FORMAT = "err_auth_invalid_header_format";
    public static final String ERR_AUTH_INVALID_SCHEME = "err_auth_invalid_scheme";
    public static final String ERR_AUTH_PARAMETER_NOT_BASE64 = "err_auth_parameter_not_base64";
    public static final String ERR_AUTH_NO_PASSWORD = "err_auth_no_password";
    public static final String ERR_AUTH_NO_CREDENTIALS = "err_auth_no_credentials";
    public static final String ERR_AUTH_NO_SUCH_USER = "err_auth_no_such_user";
    public static final String ERR_AUTH_NO_COLON_IN_PARAMETER = "err_auth_no_colon_in_parameter";
    
    
    public static final String ERR_TOKEN_REJECTED = "err_token_rejected";
    
    public static final String ERR_DATA_NOT_PROVIDED = "err_data_not_provided";
    
    public static final String ERR_IMEI_NOT_PROVIDED = "err_imei_not_provided";
    public static final String ERR_INVALID_IMEI = "err_invalid_imei";
    public static final String ERR_ALREADY_DELETED = "err_already_deleted";
    public static final String ERR_DEVICE_COLOR_NOT_PROVIDED = "err_device_color_not_provided";
    public static final String ERR_INVALID_DEVICE_COLOR_FORMAT = "err_invalid_device_color_format";
    public static final String ERR_DEVICE_MODEL_ID_NOT_PROVIDED = "err_device_model_id_not_provided";
    public static final String ERR_DEVICE_NAME_NOT_PROVIDED = "err_device_name_not_provided";
    public static final String ERR_DEVICE_ICON_ID_NOT_PROVIDED = "err_device_icon_id_not_provided";
    public static final String ERR_INVALID_VALID_TO_FORMAT = "err_invalid_valid_to_format";
    public static final String ERR_AUTO_UPDATE_ODOMETER_CANT_BE_NULL = "err_auto_update_odometer_cant_be_null";
    public static final String ERR_TIMEOUT_CANT_BE_NULL = "err_timeout_cant_be_null";
    public static final String ERR_MIN_IDLE_TIME_CANT_BE_NULL = "err_min_idle_time_cant_be_null";
    public static final String ERR_IDLE_SPEED_THRESHOLD_CANT_BE_NULL = "err_idle_speed_threshold_cant_be_null";
    public static final String ERR_HISTORY_LENGTH_CANT_BE_NULL = "err_history_length_cant_be_null";
    public static final String ERR_SHOW_ODOMETER_CANT_BE_NULL = "err_show_odometer_cant_be_null";
    public static final String ERR_SHOW_PROTOCOL_CANT_BE_NULL = "err_show_protocol_cant_be_null";
    public static final String ERR_SHOW_NAME_CANT_BE_NULL = "err_show_name_cant_be_null";
    public static final String ERR_ARROW_RADIUS_CANT_BE_NULL = "err_arrow_radius_cant_be_null";
    public static final String ERR_ARROW_MOVING_COLOR_CANT_BE_NULL = "err_arrow_moving_color_cant_be_null";
    public static final String ERR_ARROW_STOPPED_COLOR_CANT_BE_NULL = "err_arrow_stopped_color_cant_be_null";
    public static final String ERR_ARROW_PAUSED_COLOR_CANT_BE_NULL = "err_arrow_paused_color_cant_be_null";
    public static final String ERR_ARROW_OFFLINE_COLOR_CANT_BE_NULL = "err_arrow_offline_color_cant_be_null";
    
    public static final String ERR_USER_ALREADY_EXISTS = "err_user_already_exists";
    public static final String ERR_EMAIL_NOT_PROVIDED = "err_email_not_provided";
    public static final String ERR_PASSWORD_NOT_PROVIDED = "err_password_not_provided";
    public static final String ERR_CHECK_MARKETING_NOT_PROVIDED = "err_check_marketing_not_provided";
    public static final String ERR_EMAIL_RESENT = "err_email_resent";
    
    public static final String ERR_ACCOUNT_BLOCKED = "err_account_blocked";
    public static final String ERR_ACCOUNT_EXPIRED = "err_account_expired";
    
    public static final String ERR_GEOFENCE_ALL_DEVICES_NOT_PROVIDED = "err_all_devices_not_provided";
    public static final String ERR_GEOFENCE_COLOR_NOT_PROVIDED = "err_geofence_color_not_provided";
    public static final String ERR_GEOFENCE_NAME_NOT_PROVIDED = "err_geofence_name_not_provided";
    public static final String ERR_INVALID_COLOR_FORMAT = "err_invalid_color_format";
    public static final String ERR_GEOFENCE_TYPE_NOT_PROVIDED = "err_geofence_type_not_provided";
    public static final String ERR_INVALID_GEOFENCE_TYPE_FORMAT = "err_invalid_geofence_type_format";
    public static final String ERR_GEOFENCE_POINTS_NOT_PROVIDED = "err_geofence_points_not_provided";
    public static final String ERR_GEOFENCE_RADIUS_NOT_PROVIDED = "err_circle_radius_not_provided";
    public static final String ERR_TOO_FEW_GEOFENCE_POINTS = "err_too_few_geofence_points";
    public static final String ERR_GEOFENCE_DEVICES_NOT_PROVIDED = "err_geofence_devices_not_provided";
    
    public static final String ERR_COMMAND_PARSE_PARAMS_FAILED = "err_command_parse_params_failed";
    public static final String ERR_ACTIVE_DEVICE_NOT_FOUND = "err_active_device_not_found";
    public static final String ERR_CREATE_COMMAND_OBJECT_FAILED = "err_create_command_object_failed";
    public static final String ERR_SET_COMMAND_ATTRIBUTES_FAILED = "err_set_command_attributes_failed";
    public static final String ERR_COMMAND_RESPONSE_TIMEOUT = "err_command_response_timeout";
    public static final String ERR_SEND_COMMAND_FAILED = "err_send_command_failed";
    public static final String ERR_DEVICE_NOT_FOUND_OR_NO_PRIVILEGES = "err_device_not_found_or_no_privileges";

    public static final String ERR_GEO_MODE_NOT_PROVIDED = "err_geo_mode_not_provided";
    public static final String ERR_INVALID_GEO_MODE = "err_invalid_geo_mode";
    public static final String ERR_OSPD_MODE_NOT_PROVIDED = "err_ospd_mode_not_provided";
    public static final String ERR_INVALID_OSPD_MODE = "err_invalid_ospd_mode";
    public static final String ERR_POLICY_MODE_NOT_PROVIDED = "err_policy_mode_not_provided";
    public static final String ERR_INVALID_POLICY_MODE = "err_invalid_policy_mode";
    public static final String ERR_MTNCE_MODE_NOT_PROVIDED = "err_mtnce_mode_not_provided";
    public static final String ERR_INVALID_MTNCE_MODE = "err_invalid_mtnce_mode";

    public static final String NOTIFICATION_ENDING_SUBSCRIPTION_SINGLE = "notification_ending_subscription_single";
    public static final String NOTIFICATION_ENDING_SUBSCRIPTION_MULTIPLE = "notification_ending_subscription_multiple";

    public static final String ERR_APPSETTINGS_REGISTRATION_ENABLED_NOT_PROVIDED = "err_appsettings_registration_enabled_not_provided";
    public static final String ERR_APPSETTINGS_UPDATE_INTERVAL_NOT_PROVIDED = "err_appsettings_update_interval_not_provided";
    public static final String ERR_APPSETTINGS_UPDATE_INTERVAL_INVALID_VALUE = "err_appsettings_update_interval_invalid_value";
    public static final String ERR_APPSETTINGS_EVENT_RECORDING_NOT_PROVIDED = "err_appsettings_event_recording_not_provided";
    public static final String ERR_APPSETTINGS_NOTIFICATION_EXPIRATION_PERIOD_NOT_PROVIDED = "err_appsettings_notification_expiration_period_not_provided";
    public static final String ERR_APPSETTINGS_NOTIFICATION_EXPIRATION_PERIOD_NEGATIVE = "err_appsettings_notification_expiration_period_negative";
    public static final String ERR_APPSETTINGS_LANGUAGE_NOT_PROVIDED = "err_appsettings_language_not_provided";
    public static final String ERR_APPSETTINGS_PASSWORD_HASH_METHOD_NOT_PROVIDED = "err_appsettings_password_hash_method_not_provided";
    public static final String ERR_APPSETTINGS_ILLEGAL_PASSWORD_HASH_METHOD = "err_appsettings_illegal_password_hash_method";
    public static final String ERR_APPSETTINGS_DEFAULT_USER_GROUP_NOT_PROVIDED = "err_appsettings_default_user_group_not_provided";

    public static final String ERR_USER_MANAGER_NOT_PROVIDED = "err_user_manager_not_provided";
    public static final String ERR_USER_ADMIN_NOT_PROVIDED = "err_user_admin_not_provided";
    public static final String ERR_USER_ARCHIVE_NOT_PROVIDED = "err_user_archive_not_provided";
    public static final String ERR_USER_READONLY_NOT_PROVIDED = "err_user_readonly_not_provided";
    public static final String ERR_USER_BLOCKED_NOT_PROVIDED = "err_user_blocked_not_provided";
    public static final String ERR_USER_PASSWORD_NOT_PROVIDED = "err_user_password_not_provided";
    public static final String ERR_USER_NOTIFICATION_EVENTS_NOT_PROVIDED = "err_user_notification_events_not_provided";
    public static final String ERR_USER_LOGIN_NOT_PROVIDED = "err_user_login_not_provided";
    
    public static final String ERR_USER_DELETING_ITSELF = "err_user_deleting_itself";
    
    public static final String ERR_DEVICE_GROUP_NAME_NOT_PROVIDED = "err_device_group_name_not_provided";
    public static final String ERR_DEVICE_GROUP_WOULD_CREATE_CYCLE = "err_device_group_would_create_cycle";
    
    public static final String ERR_POSITIONS_QUERY_MINDISTANCE_VALUE_NOT_PROVIDED = "err_positions_query_mindistance_value_not_provided";
    public static final String ERR_POSITIONS_QUERY_MINDISTANCE_WRONG_FORMAT = "err_positions_query_mindistance_wrong_format";
    public static final String ERR_POSITIONS_QUERY_SPEED_VALUE_NOT_PROVIDED = "err_positions_query_speed_value_not_provided";
    public static final String ERR_POSITIONS_QUERY_SPEED_WRONG_FORMAT = "err_positions_query_speed_wrong_format";
    public static final String ERR_POSITIONS_QUERY_DATE_VALUE_NOT_PROVIDED = "err_positions_query_date_value_not_provided";
    public static final String ERR_POSITIONS_QUERY_DATE_WRONG_FORMAT = "err_positions_query_date_wrong_format";
    
    public static final String ERR_USERSETTINGS_CENTER_LATITUDE_NOT_PROVIDED = "err_usersettings_center_latitude_not_provided";
    public static final String ERR_USERSETTINGS_CENTER_LONGITUDE_NOT_PROVIDED = "err_usersettings_center_longitude_not_provided";
    public static final String ERR_USERSETTINGS_HIDE_DUPLICATES_NOT_PROVIDED = "err_usersettings_hide_duplicates_not_provided";
    public static final String ERR_USERSETTINGS_HIDE_INVALID_LOCATIONS_NOT_PROVIDED = "err_usersettings_hide_invalid_locations_not_provided";
    public static final String ERR_USERSETTINGS_HIDE_ZERO_COORDINATES_NOT_PROVIDED = "err_usersettings_hide_zero_coordinates_not_provided";
    public static final String ERR_USERSETTINGS_MAP_TYPE_NOT_PROVIDED = "err_usersettings_map_type_not_provided";
    public static final String ERR_USERSETTINGS_MAXIMIZE_OVERVIEW_MAP_NOT_PROVIDED = "err_usersettings_maximize_overview_map_not_provided";
    public static final String ERR_USERSETTINGS_OVERLAYS_NOT_PROVIDED = "err_usersettings_overlays_not_provided";
    public static final String ERR_TIME_PRINT_INTERVAL_NOT_PROVIDED = "err_time_print_interval_not_provided";
    public static final String ERR_TIME_ZONE_ID_NOT_PROVIDED = "err_time_zone_id_not_provided";
    public static final String ERR_ZOOM_LEVEL_NOT_PROVIDED = "err_zoom_level_not_provided";
    public static final String ERR_USERSETTINGS_SPEED_UNIT_NOT_PROVIDED = "err_usersettings_speed_unit_not_provided";
    
    public static final String ERR_USER_GROUP_NAME_NOT_PROVIDED = "err_user_group_name_not_provided";
    public static final String ERR_USER_GROUP_PERMISSIONS_NOT_PROVIDED = "err_user_group_permissions_not_provided";
    public static final String ERR_USER_GROUP_ALREADY_EXISTS = "err_user_group_already_exists";
    public static final String ERR_USER_GROUP_INVALID_PERMISSION = "err_user_group_invalid_permission";
    public static final String ERR_USER_GROUP_DELETING_DEFAULT = "err_user_group_deleting_default";
    public static final String ERR_USER_GROUP_LACKING_BASE_PERMISSIONS = "err_user_group_lacking_base_permissions";
}
