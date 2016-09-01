-- Fixtures for commands metadata, run this SQL file with any MySQL client

-- Commands fixures

INSERT INTO traccar_api_metadata.device_models(id, imageUrl, name, createTime, updateTime)
VALUES (1, 'datamatica.pl/images/test1.png', 'LK_210', '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_types(id, name, createTime, updateTime)
VALUES (1, 'positionPeriodic', '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_types(id, name, createTime, updateTime)
VALUES (2, 'setSOSNumbers', '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 1);

INSERT INTO traccar_api_metadata.devicemodel_commandtype(deviceModelId, commandTypeId)
VALUES (1, 2);

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId, createTime, updateTime)
VALUES (1, 'frequency', 'integer', 1, '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId, createTime, updateTime)
VALUES (2, 'SOSNumber1', 'string', 2, '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_parameters(id, name, valueType, commandTypeId, createTime, updateTime)
VALUES (3, 'SOSNumber2', 'string', 2, '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId, createTime, updateTime)
VALUES (1, 'GTE', '10', 1, '2016-01-01 00:00:00', '2016-01-01 00:00:00');

INSERT INTO traccar_api_metadata.command_constraints(id, constraintType, constraintValue, commandParameterId, createTime, updateTime)
VALUES (2, 'LTE', '1200', 1, '2016-01-01 00:00:00', '2016-01-01 00:00:00');

-- Report fixtures

INSERT INTO traccar_api_metadata.report_types(id, reportName, imageUrl)
VALUES (1, 'Jazda i postoje', 'www.datamatica.pl/images/jazda_postoje.png');

INSERT INTO traccar_api_metadata.report_parameters(id, parameterKey, parameterName, parameterValue, reportTypeId)
VALUES (1, 'timePeriod', 'Obejmowany okres', 'doubleTime', 1);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('minLength', '3', 1);

INSERT INTO traccar_api_metadata.report_constraints(constraintKey, constraintValue, ReportParameterId)
VALUES ('maxLength', '20', 1);
