-- Fixtures for commands metadata, run this SQL file with any MySQL client

-- Commands fixures

INSERT INTO traccar_api_metadata.device_models(id, imageUrl, name)
VALUES (1, 'datamatica.pl/images/test1.png', 'LK_210');

INSERT INTO traccar_api_metadata.command_types(id, name)
VALUES (1, 'positionPeriodic');

INSERT INTO traccar_api_metadata.command_types(id, name)
VALUES (2, 'setSOSNumbers');

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 1);

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 2);

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId)
VALUES (1, 'frequency', 'integer', 1);

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId)
VALUES (2, 'SOSNumber1', 'string', 2);

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId)
VALUES (3, 'SOSNumber2', 'string', 2);

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId)
VALUES (1, 'GTE', '10', 1);

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId)
VALUES (2, 'LTE', '1200', 1);

-- Report fixtures

INSERT INTO traccar_api_metadata.report_types(id, reportName, imageUrl)
VALUES (1, 'Jazda i postoje', 'www.datamatica.pl/images/jazda_postoje.png');

INSERT INTO traccar_api_metadata.report_parameters(id, parameterKey, parameterName, parameterValue, reportTypeId)
VALUES (1, 'timePeriod', 'Obejmowany okres', 'doubleDate', 1);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('minDate', '2016-01-01', 1);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('maxDate', '2016-09-01', 1);

INSERT INTO traccar_api_metadata.report_parameters(id, parameterKey, parameterName, parameterValue, reportTypeId)
VALUES (2, 'selectedDevices', 'Wybrane urządzenia', 'list', 1);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('maxNumber', '3', 2);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('allowedModels', '["LK210", "LK209"]', 2);
