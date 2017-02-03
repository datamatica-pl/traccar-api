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
package pl.datamatica.traccar.api.controllers;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddDeviceDto;
import pl.datamatica.traccar.api.dtos.in.EditDeviceDto;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.ListDto;
import pl.datamatica.traccar.api.dtos.out.PositionDto;
import pl.datamatica.traccar.api.providers.DeviceProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkCachedResponse;
import pl.datamatica.traccar.model.Device;
import spark.Request;
import spark.Spark;

public class DevicesController extends ControllerBase {

    public static class Binder extends ControllerBinder {

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                DevicesController dc = createController(req);
                return render(dc.get(), res);
            }, gson::toJson);

            Spark.post(rootUrl(), (req, res) -> {
                DevicesController dc = createController(req);
                AddDeviceDto deviceDto = gson.fromJson(req.body(), AddDeviceDto.class);
                return render(dc.post(deviceDto), res);
            }, gson::toJson);

            Spark.put(rootUrl()+"/:id", (req, res) -> {
                DevicesController dc = createController(req);
                EditDeviceDto deviceDto = gson.fromJson(req.body(), EditDeviceDto.class);
                return render(dc.put(Long.parseLong(req.params(":id")), deviceDto), res);
            }, gson::toJson);

            Spark.get(rootUrl()+"/:id", (req, res) -> {
                DevicesController dc = createController(req);
                return render(dc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);

            Spark.delete(rootUrl() + "/:id", (req, res)-> {
                DevicesController dc = createController(req);
                return render(dc.delete(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);

            Spark.get(rootUrl() + "/:id/positions", (req, res) -> {
                DevicesController dc = createController(req);
                return render(dc.getPositions(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
        }

        private DevicesController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            DevicesController dc = new DevicesController(context);
            return dc;
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/devices";
        }
    }

    private static final int MAX_RESULT_COUNT = 4000;
    private final DeviceProvider dp;
    private final PositionProvider positions;
    private final Date minDate;

    public DevicesController(RequestContext requestContext) {
        super(requestContext);
        this.dp = requestContext.getDeviceProvider();
        this.positions = requestContext.getPositionProvider();
        this.minDate = requestContext.getModificationDate();
    }

    public HttpResponse get() throws Exception {
        List<Device> devices = dp.getAllAvailableDevices()
                .filter(d -> !d.isDeleted())
                .collect(Collectors.toList());
        List<DeviceDto> changedDevices = devices.stream()
                .map(d -> new DeviceDto.Builder().device(d).build())
                .filter(d -> isModified(d.getModificationTime()))
                .collect(Collectors.toList());
        long[] deviceIds = devices.stream()
                .mapToLong(d -> d.getId())
                .toArray();

        return new OkCachedResponse(new ListDto<>(changedDevices, deviceIds),
                devices.stream()
                        .map(d -> d.getLastUpdate())
                        .max((d1, d2) -> d1.compareTo(d2))
                        .orElse(Application.EMPTY_RESPONSE_MODIFICATION_DATE));
    }

    public HttpResponse get(long id) throws Exception {
        try{
            return okCached(new DeviceDto.Builder().device(dp.getDevice(id)).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse post(AddDeviceDto deviceDto) throws Exception {
        List<ErrorDto> validationErrors = AddDeviceDto.validate(deviceDto);
        if(!validationErrors.isEmpty())
            return badRequest(validationErrors);
        try {
            Device device = dp.createDevice(deviceDto.getImei());
            return created("devices/"+device.getId(), new DeviceDto.Builder().device(device).build());
        } catch(ProviderException e) {
            switch(e.getType()) {
                case INVALID_IMEI:
                    return badRequest(MessageKeys.ERR_INVALID_IMEI);
                case DEVICE_ALREADY_EXISTS:
                    return badRequest(MessageKeys.ERR_INVALID_IMEI);
            }
            throw e;
        }
    }

    public HttpResponse put(long id, EditDeviceDto deviceDto) throws ProviderException {
        List<ErrorDto> errors = EditDeviceDto.validate(deviceDto);
        if(!errors.isEmpty())
            return badRequest(errors);

        try {
            dp.updateDevice(id, deviceDto);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse delete(long id) throws Exception {
        try {
            dp.delete(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse getPositions(long id) throws Exception {
        try {
            Device device = dp.getDevice(id);

            return okCached(new ListDto<PositionDto>(
                    positions
                        .getAllAvailablePositions(device, minDate, MAX_RESULT_COUNT+1)
                        .filter(position -> position.hasProperValidStatus())
                        .map(p -> new PositionDto.Builder().position(p).build())
                        .collect(Collectors.toList()),
                    MAX_RESULT_COUNT));
        } catch (ProviderException ex) {
            return handle(ex);
        }
    }
}
