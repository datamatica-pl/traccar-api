-- Fixtures for commands metadata, run this SQL file with any MySQL client

SET NAMES 'utf8';

-- Commands fixures

INSERT INTO traccar_api_metadata.device_models(id, imageUrl, modelName, descriptionShort, descriptionLong, shopUrl)
VALUES (1, 'datamatica.pl/images/lk210.png', 'LK210', 'GPS motocyklowy', 'GPS motocyklowy, niewielki, łatwy do ukrycia, wodoszczelny', 'https://datamatica.pl/shop/lk210');

INSERT INTO traccar_api_metadata.command_types(id, commandName, description)
VALUES (1, 'positionPeriodic', 'Jednokrotne zwrócenie aktualnej pozycji.');

INSERT INTO traccar_api_metadata.command_types(id, commandName)
VALUES (2, 'setSOSNumbers');

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 1);

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 2);

INSERT INTO traccar_api_metadata.command_parameters(id, parameterName, valueType, description, commandTypeId)
VALUES (1, 'frequency', 'integer', 'Częstotliwość powiadamiana w ruchu', 1);

INSERT INTO traccar_api_metadata.command_parameters(id, parameterName, valueType, commandTypeId)
VALUES (2, 'SOSNumber1', 'string', 2);

INSERT INTO traccar_api_metadata.command_parameters(id, parameterName, valueType, commandTypeId)
VALUES (3, 'SOSNumber2', 'string', 2);

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId)
VALUES (1, 'GTE', '10', 1);

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId)
VALUES (2, 'LTE', '1200', 1);

INSERT INTO traccar_api_metadata.device_models(id, imageUrl, modelName)
VALUES (2, 'datamatica.pl/images/gt06.png', 'GT06');

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (2, 1);

-- Report fixtures

INSERT INTO traccar_api_metadata.report_types(id, reportName, imageUrl, descriptionShort, descriptionLong)
VALUES (1, 'Jazda i postoje', 'www.datamatica.pl/images/jazda_postoje.png', 'Raport z przebiegu trasy', 'Szczegółowy raport z przebiegu trasy wraz z zatrzymaniami');

INSERT INTO traccar_api_metadata.report_parameters(id, parameterKey, parameterName, parameterValue, description, reportTypeId)
VALUES (1, 'timePeriod', 'Obejmowany okres', 'doubleDate', 'Zakres dat wraz z godzinami', 1);

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

INSERT INTO traccar_api_metadata.report_types(id, reportName, imageUrl)
VALUES (2, 'Informacje ogólne', 'www.datamatica.pl/images/informacje_ogolne.png');

INSERT INTO traccar_api_metadata.report_parameters(id, parameterKey, parameterName, parameterValue, description, reportTypeId)
VALUES (3, 'timePeriod', 'Obejmowany okres', 'doubleDate', 'Zakres dat wraz z godzinami', 2);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('minDate', '2016-02-01', 3);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('maxDate', '2016-08-01', 3);

-- DeviceIcon fixtures
INSERT INTO traccar_api_metadata.device_icons(iconUrl)
VALUES ('http://datamatica.pl/images/deviceicon_1.png');

INSERT INTO traccar_api_metadata.device_icons(iconUrl)
VALUES ('http://datamatica.pl/images/deviceicon_2.png');

INSERT INTO traccar_api_metadata.device_icons(iconUrl)
VALUES ('http://datamatica.pl/images/deviceicon_3.png');

INSERT INTO traccar_api_metadata.device_icons(iconUrl)
VALUES ('http://datamatica.pl/images/new_deviceicon_1.png');
