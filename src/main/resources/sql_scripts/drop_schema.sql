-- Drop metadata tables in proper order to avoid problems with removing references

DROP TABLE IF EXISTS traccar_api_metadata.device_icons;
DROP TABLE IF EXISTS traccar_api_metadata.command_constraints;
DROP TABLE IF EXISTS traccar_api_metadata.command_parameters;
DROP TABLE IF EXISTS traccar_api_metadata.devicemodel_commandtype;
DROP TABLE IF EXISTS traccar_api_metadata.device_models;
DROP TABLE IF EXISTS traccar_api_metadata.command_types;
