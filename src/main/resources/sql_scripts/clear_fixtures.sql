-- Clear fixtures for commands metadata, run this SQL file with any MySQL client

DELETE FROM traccar_api_metadata.device_icons WHERE id BETWEEN 1 AND 100000;
DELETE FROM traccar_api_metadata.command_constraints WHERE id BETWEEN 1 AND 100000;
DELETE FROM traccar_api_metadata.command_parameters WHERE id BETWEEN 1 AND 100000;
DELETE FROM traccar_api_metadata.devicemodel_commandtype WHERE deviceModelId BETWEEN 1 AND 100000;
DELETE FROM traccar_api_metadata.device_models WHERE id BETWEEN 1 AND 100000;
DELETE FROM traccar_api_metadata.command_types WHERE id BETWEEN 1 AND 100000;
