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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import pl.datamatica.traccar.api.providers.DeviceGroupProvider;
import pl.datamatica.traccar.api.providers.PicturesProvider;
import pl.datamatica.traccar.api.providers.PositionProvider;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.api.responses.OkCachedResponse;
import pl.datamatica.traccar.api.utils.GeoUtils;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.Picture;
import pl.datamatica.traccar.model.Position;
import pl.datamatica.traccar.model.User;
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
                return render(dc.getPositions(Long.parseLong(req.params(":id")), req.queryMap().toMap()), res);
            }, gson::toJson);
            
            Spark.get(rootUrl()+"/:id/customicon", (req, res) -> {
                try {
                    DevicesController dc = createController(req);
                    Picture pic = dc.getCustomIcon(Long.parseLong(req.params(":id")));
                    if(pic == null) {
                        res.status(404);
                        return res;
                    }
                    res.raw().setContentType(pic.getMimeType());
                    res.raw().getOutputStream().write(pic.getData(),0, pic.getData().length);
                    return res;
                } catch(ProviderException e) {
                    res.status(404);
                    return res;
                }
            });
            
            Spark.put(rootUrl()+"/:id/customicon", (req, res) -> {
                DevicesController dc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                return render(dc.updateCustomIcon(id, req.bodyAsBytes()), res);
            });
            
            Spark.patch(rootUrl()+"/:id", (req, res)-> {
                DevicesController dc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                JsonElement body = new JsonParser().parse(req.body());
                return render(dc.patch(id, body), res);
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
    private final DeviceGroupProvider gp;
    private final PositionProvider positions;
    private final Date minDate;
    private final Set<Long> userIds;
    private final SimpleDateFormat dateFormat;

    public DevicesController(RequestContext requestContext) {
        super(requestContext);
        this.dp = requestContext.getDeviceProvider();
        this.gp = requestContext.getDeviceGroupProvider();
        this.positions = requestContext.getPositionProvider();
        this.minDate = requestContext.getModificationDate();
        this.userIds = requestContext.getUserProvider().getAllAvailableUsers()
                .map(User::getId).collect(Collectors.toSet());
        this.dateFormat = new SimpleDateFormat(Application.DATE_FORMAT);
    }

    public HttpResponse get() throws Exception {
        List<Device> devices = dp.getAllAvailableDevices()
                .filter(d -> !d.isDeleted())
                .collect(Collectors.toList());
        List<DeviceDto> changedDevices = devices.stream()
                .map(d -> new DeviceDto.Builder().device(d, userIds).build())
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
            return okCached(new DeviceDto.Builder().device(dp.getDevice(id), userIds).build());
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
            return created("devices/"+device.getId(), new DeviceDto.Builder().device(device, userIds).build());
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

    public HttpResponse patch(long id, JsonElement body) throws ProviderException {
        if(!body.isJsonObject()) {
            return badRequest();
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Application.DATE_FORMAT);
        JsonObject changes = body.getAsJsonObject();
        Device d = dp.getDevice(id);
        List<ErrorDto> errors = validatePatch(changes, d);
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            dp.applyPatch(id, changes);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    private List<ErrorDto> validatePatch(JsonObject changes, Device d) {
        List<ErrorDto> errors = new ArrayList<>();
        if(changes.has("deviceName") && changes.get("deviceName").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_NAME_NOT_PROVIDED));
        if(changes.has("deviceModelId") && changes.get("deviceModelId").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_MODEL_ID_NOT_PROVIDED));
        if(changes.has("iconId") && changes.get("iconId").isJsonNull()
                && ((d.getCustomIconId() == null && !changes.has("customIconId"))
                    || (changes.has("customIconId") && changes.get("customIconId").isJsonNull())))
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_ICON_ID_NOT_PROVIDED));
        else if(changes.has("customIconId") && changes.get("customIconId").isJsonNull()
                && ((d.getIconId() == null && !changes.has("iconId"))
                    || (changes.has("iconId") && changes.get("iconId").isJsonNull())))
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_ICON_ID_NOT_PROVIDED));
        if(changes.has("color") && changes.get("color").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_DEVICE_COLOR_NOT_PROVIDED));
        if(changes.has("autoUpdateOdometer") && changes.get("autoUpdateOdometer").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_AUTO_UPDATE_ODOMETER_CANT_BE_NULL));
        if(changes.has("timeout") && changes.get("timeout").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_TIMEOUT_CANT_BE_NULL));
        if(changes.has("minIdleTime") && changes.get("minIdleTime").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_MIN_IDLE_TIME_CANT_BE_NULL));
        if(changes.has("idleSpeedThreshold") && changes.get("idleSpeedThreshold").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_IDLE_SPEED_THRESHOLD_CANT_BE_NULL));
        if(changes.has("historyLength") && changes.get("historyLength").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_HISTORY_LENGTH_CANT_BE_NULL));
        if(changes.has("showOdometer") && changes.get("showOdometer").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_SHOW_ODOMETER_CANT_BE_NULL));
        if(changes.has("showProtocol") && changes.get("showProtocol").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_SHOW_PROTOCOL_CANT_BE_NULL));
        if(changes.has("showName") && changes.get("showName").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_SHOW_NAME_CANT_BE_NULL));
        if(changes.has("arrowRadius") && changes.get("arrowRadius").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_ARROW_RADIUS_CANT_BE_NULL));
        if(changes.has("arrowMovingColor") && changes.get("arrowMovingColor").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_ARROW_MOVING_COLOR_CANT_BE_NULL));
        if(changes.has("arrowStoppedColor") && changes.get("arrowStoppedColor").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_ARROW_STOPPED_COLOR_CANT_BE_NULL));
        if(changes.has("arrowPausedColor") && changes.get("arrowPausedColor").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_ARROW_PAUSED_COLOR_CANT_BE_NULL));
        if(changes.has("arrowOfflineColor") && changes.get("arrowOfflineColor").isJsonNull())
            errors.add(new ErrorDto(MessageKeys.ERR_ARROW_OFFLINE_COLOR_CANT_BE_NULL));
        if(changes.has("validTo") && !changes.get("validTo").isJsonNull()) {
            try {
                dateFormat.parse(changes.get("validTo").getAsString());
            } catch(ParseException e) {
                errors.add(new ErrorDto(MessageKeys.ERR_INVALID_VALID_TO_FORMAT));
            }
        }
        return errors;
    }
    
    public HttpResponse delete(long id) throws Exception {
        try {
            dp.delete(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse getPositions(long id, Map<String, String[]> params) throws Exception {
        try {
            Device device = dp.getDevice(id);  
            PositionsQueryParams qp = parsePositionsQuery(params);
            if (!qp.errors.isEmpty()) 
                return badRequest(qp.errors);
            
            List<Position> pos = positions
                    .getAllAvailablePositions(device, qp.minDate, qp.maxDate, 
                            qp.getAll ? 0 : MAX_RESULT_COUNT+1)
                    .collect(Collectors.toList());
            
            pos = filterPositions(pos, qp);
            
            return okCached(new ListDto<>(
                    pos.stream()
                        .map(p -> new PositionDto.Builder().position(p).build())
                        .collect(Collectors.toList()),
                    qp.getAll ? Integer.MAX_VALUE : MAX_RESULT_COUNT));
        } catch (ProviderException ex) {
            return handle(ex);
        }
    }
    
    List<Position> filterPositions(List<Position> pos, PositionsQueryParams qp) {
        Stream<Position> filtered = pos.stream();
             
        if (qp.hideZero)
            filtered = filtered.filter(p -> p.getLatitude() != 0 || p.getLongitude() != 0);
        if (qp.hideInvalid)
            filtered = filtered.filter(p -> p.getValid() != null && p.getValid());
        if (qp.speedValue != null) {
            filtered = filtered.filter(p -> p.getSpeed() != null);
            switch (qp.speedComp) {
                case LESS: filtered = filtered.filter(p -> p.getSpeed() < qp.speedValue); break;
                case LESSEQUAL: filtered = filtered.filter(p -> p.getSpeed() <= qp.speedValue); break;
                case EQUAL: filtered = filtered.filter(p -> p.getSpeed() == qp.speedValue.doubleValue()); break;
                case GREATEREQUAL: filtered = filtered.filter(p -> p.getSpeed() >= qp.speedValue); break;
                case GREATER: filtered = filtered.filter(p -> p.getSpeed() > qp.speedValue); break;
            }
        }
        
        List<Position> posList = filtered.collect(Collectors.toList());
        if (posList.isEmpty())
            return posList;
        
        List<Position> resList = new ArrayList<>();
        Position last = posList.get(0);
        resList.add(last);
        for (int i = 1; i < posList.size(); i++) {
            boolean add = true;
            Position current = posList.get(i);
            
            if (qp.hideDuplicates && last.getTime().equals(current.getTime()))
                add = false;
            if (qp.minDistance > 0)
                if (GeoUtils.getDistance(last.getLongitude(), last.getLatitude(), current.getLongitude(), current.getLatitude()) * 1000.0 < qp.minDistance)
                    add = false;
            
            if (add) {
                resList.add(current);
                last = current;
            }
        }
        return resList;
    }
    
    /* PARSING POSITIONS QUERY */
    
    enum PositionSpeedOperator {
        LESS, LESSEQUAL, EQUAL, GREATEREQUAL, GREATER
    }
    
    class PositionsQueryParams {
        public Date minDate = null;
        public Date maxDate = null;
        public Boolean hideZero = false;
        public Boolean hideInvalid = false;
        public Boolean hideDuplicates = false;
        public Integer minDistance = 0;
        public PositionSpeedOperator speedComp;
        public Integer speedValue = null;
        public Boolean getAll = false;
        
        public List<ErrorDto> errors = new ArrayList<>();
        
        public PositionsQueryParams() {}
    }
    
    PositionsQueryParams parsePositionsQuery(Map<String, String[]> params) {
        PositionsQueryParams parsed = new PositionsQueryParams();
        
        if (params.containsKey("fromDate") && params.containsKey("toDate")) {
            DateFormat df = new SimpleDateFormat(Application.DATE_FORMAT);
            try {
                parsed.minDate = df.parse(params.get("fromDate")[0]);
                parsed.maxDate = df.parse(params.get("toDate")[0]);
            }
            catch(ArrayIndexOutOfBoundsException oob) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_DATE_VALUE_NOT_PROVIDED));
                parsed.minDate = parsed.maxDate = null;
            }
            catch(ParseException pe) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_DATE_WRONG_FORMAT));
                parsed.minDate = parsed.maxDate = null;
            }
        }
        if (params.containsKey("hideZero"))
            parsed.hideZero = true;
        if (params.containsKey("hideInvalid"))
            parsed.hideInvalid = true;
        if (params.containsKey("hideDup"))
            parsed.hideDuplicates = true;
        if (params.containsKey("minDistance")) {
            try {
                parsed.minDistance = Integer.parseInt(params.get("minDistance")[0]);
            }
            catch(ArrayIndexOutOfBoundsException oob) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_MINDISTANCE_VALUE_NOT_PROVIDED));
            }
            catch (NumberFormatException nfe) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_MINDISTANCE_WRONG_FORMAT));
            }
        }
        if (params.containsKey("speedComp") && params.containsKey("speedValue")) {
            try {
                switch (params.get("speedComp")[0]) {
                    case "lt": parsed.speedComp = PositionSpeedOperator.LESS; break;
                    case "lte": parsed.speedComp = PositionSpeedOperator.LESSEQUAL; break;
                    case "eq": parsed.speedComp = PositionSpeedOperator.EQUAL; break;
                    case "gte": parsed.speedComp = PositionSpeedOperator.GREATEREQUAL; break;
                    case "gt": parsed.speedComp = PositionSpeedOperator.GREATER; break;
                    default:
                        throw new IllegalArgumentException();
                }
                parsed.speedValue = Integer.parseInt(params.get("speedValue")[0]);
            }
            catch(ArrayIndexOutOfBoundsException oob) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_SPEED_VALUE_NOT_PROVIDED));
            }
            catch (IllegalArgumentException e) {
                parsed.errors.add(new ErrorDto(MessageKeys.ERR_POSITIONS_QUERY_SPEED_WRONG_FORMAT));
            }
        }
        if (params.containsKey("all"))
            parsed.getAll = true;
        
        return parsed;
    }
    
    /* PARSING POSITIONS QUERY - END */
    
    
    
    private Picture getCustomIcon(long deviceId) throws ProviderException {
        Device device = dp.getDevice(deviceId);
        PicturesProvider provider = requestContext.getPicturesProvider();
        return provider.getImage(device.getCustomIconId());
    }

    private HttpResponse updateCustomIcon(long deviceId, byte[] data) throws ProviderException {
        Device device = dp.getDevice(deviceId);
        PicturesProvider pp = requestContext.getPicturesProvider();
        if(device.getCustomIconId() != null)
            pp.deletePictureIfExists(device.getCustomIconId());
        Picture p = pp.createPicture(data);
        device.setCustomIconId(p.getId());
        return ok(p.getId());
    }
}
