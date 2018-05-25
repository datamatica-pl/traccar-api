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

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import pl.datamatica.traccar.api.Application;
import pl.datamatica.traccar.api.TraccarConfig;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.MessageKeys;
import pl.datamatica.traccar.api.dtos.in.AddUserDto;
import pl.datamatica.traccar.api.dtos.in.BleDeviceDto;
import pl.datamatica.traccar.api.dtos.in.EditUserDto;
import pl.datamatica.traccar.api.dtos.in.EditUserSettingsDto;
import pl.datamatica.traccar.api.dtos.in.RegisterUserDto;
import pl.datamatica.traccar.api.dtos.in.ResetPassReqDto;
import pl.datamatica.traccar.api.dtos.out.ErrorDto;
import pl.datamatica.traccar.api.dtos.out.UserDto;
import pl.datamatica.traccar.api.dtos.out.UserSettingsDto;
import pl.datamatica.traccar.api.exceptions.ConfigLoadException;
import pl.datamatica.traccar.api.providers.MailSender;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ProviderRemovingException;
import pl.datamatica.traccar.api.providers.UserProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.User;
import pl.datamatica.traccar.model.UserPermission;
import pl.datamatica.traccar.model.UserSettings;
import spark.Request;
import spark.Spark;

public class UsersController extends ControllerBase {
    
    public static class Binder extends ControllerBinder{

        @Override
        public void bind() {
            Spark.get(rootUrl(), (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.get(), res);
            }, gson::toJson);

            Spark.get(rootUrl() + "/:id", (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.get(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.post(rootUrl(), (req, res) -> {
                UsersController uc = createController(req);
                if (req.attribute(RequestContext.REQUEST_FIELD_IS_AUTH) != null 
                        && (Boolean)req.attribute(RequestContext.REQUEST_FIELD_IS_AUTH) == true) {
                    AddUserDto addDto = gson.fromJson(req.body(), AddUserDto.class);
                    return render(uc.post(addDto), res);
                }
                RegisterUserDto userDto = gson.fromJson(req.body(), RegisterUserDto.class);
                return render(uc.register(userDto), res);
            }, gson::toJson);
            
            Spark.post(rootUrl()+"/register", (req, res) -> {
                UsersController uc = createController(req);
                RegisterUserDto userDto = gson.fromJson(req.body(), RegisterUserDto.class);
                return render(uc.register(userDto), res);
            }, gson::toJson);
            
            Spark.put(rootUrl()+"/:id", (req, res) -> {
                UsersController uc = createController(req);
                EditUserDto dto = gson.fromJson(req.body(), EditUserDto.class);
                return render(uc.put(Long.parseLong(req.params(":id")), dto), res);
            }, gson::toJson);
            
            Spark.delete(rootUrl()+"/:id", (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.delete(Long.parseLong(req.params(":id"))), res);
            }, gson::toJson);
            
            Spark.get(rootUrl()+"/activate/:token", (req, res) -> {
                UsersController uc = createController(req);
                return render(uc.activateUser(req.params(":token")), res);
            });
            
            Spark.post(rootUrl()+"/resetreq", (req, res) -> {
                UsersController uc = createController(req);
                ResetPassReqDto dto = gson.fromJson(req.body(), ResetPassReqDto.class);
                return render(uc.requestPasswordReset(dto), res);
            });
            
            Spark.get(rootUrl()+"/reset/:token", (req, res) -> {
                UsersController uc = createController(req);
                return uc.resetPassword(req.params(":token"));
            });
            
            Spark.post(rootUrl()+"/resend", (req, res) -> {
                UsersController uc = createController(req);
                ResetPassReqDto dto = gson.fromJson(req.body(), ResetPassReqDto.class);
                return render(uc.resendLink(dto), res);
            });
            
            Spark.get(rootUrl()+"/:id/settings", (req, res) -> {
                UsersController uc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                return render(uc.getUserSettings(id), res);
            }, gson::toJson);
            
            Spark.put(rootUrl()+"/:id/settings", (req, res) -> {
                UsersController uc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                UserSettingsDto dto = gson.fromJson(req.body(), UserSettingsDto.class);
                return render(uc.updateUserSettings(id, dto), res);
            }, gson::toJson);
            
            Spark.put(rootUrl()+"/:id/ble", (req, res) -> {
                UsersController uc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                List<BleDeviceDto> dto = gson.fromJson(req.body(), 
                        new TypeToken<List<BleDeviceDto>>(){}.getType());
                return render(uc.updateUserBleDevices(id, dto), res);
            }, gson::toJson);
        }

        private UsersController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            UsersController uc = new UsersController(context);
            return uc;
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/users";
        }
    }
    
    private UserProvider up;
    private MailSender sender;
    
    public UsersController(RequestContext requestContext) {
        super(requestContext);
        up = requestContext.getUserProvider();
        sender = requestContext.getMailSender();
    }
    
    public HttpResponse get() throws Exception {
        List<UserDto> users = up.getAllAvailableUsers()
                .map(user ->  {
                    UserDto.Builder builder = new UserDto.Builder().user(user);
                    if(requestContext.getUser().hasPermission(UserPermission.USER_GROUP_MANAGEMENT)
                            && user.getUserGroup() != null)
                        builder.userGroupName(user.getUserGroup().getName());
                    return builder.build();
                        })
                .collect(Collectors.toList());
        return ok(users);
    }
    
    public HttpResponse get(long id) throws Exception {
        try {
            User other = up.getUser(id);
            return ok(new UserDto.Builder().user(other).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }

    public HttpResponse post(AddUserDto userDto) throws ProviderException {
        List<ErrorDto> errors = AddUserDto.validate(userDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        try {
            User user = up.createUser(userDto);
            return created("users/"+user.getId(), new UserDto.Builder().user(user).build());
        } catch (ProviderException e) {
            if (e.getType() == ProviderException.Type.USER_ALREADY_EXISTS)
                return conflict(MessageKeys.ERR_USER_ALREADY_EXISTS);
            return handle(e);
        }
    }

    public HttpResponse register(RegisterUserDto userDto) throws ProviderException {
        List<ErrorDto> errors = RegisterUserDto.validate(userDto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        try {
            User user = up.registerUser(userDto.getEmail().trim(), 
                    userDto.getPassword(), userDto.isCheckMarketing());
            requestContext.setUser(user);
            if(userDto.getImei() != null)
                requestContext.getDeviceProvider().createDevice(userDto.getImei(), 
                        requestContext.getDeviceModelProvider());
            sendActivationToken(user);
            return created("users/"+user.getId(), "");
        } catch (ProviderException ex) {
            requestContext.rollbackTransaction();
            switch(ex.getType()) {
                case USER_ALREADY_EXISTS:
                    return conflict(MessageKeys.ERR_USER_ALREADY_EXISTS);
                case INVALID_IMEI:
                case DEVICE_ALREADY_EXISTS:
                    return badRequest(MessageKeys.ERR_INVALID_IMEI);                    
            }
            throw ex;
        }
    }
           
    public HttpResponse put(long id, EditUserDto dto) throws ProviderException {
        List<ErrorDto> errors = EditUserDto.validate(dto);
        if(!errors.isEmpty())
            return badRequest(errors);
        
        try {
            up.updateUser(id, dto);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse delete(long id) throws Exception {
        
        try  {
            up.removeUser(id);
            return ok("");
            
        } catch(ProviderRemovingException e) {
            if (e.getType() == ProviderException.Type.DELETING_ITSELF) {
                return badRequest(Collections.singletonList(new ErrorDto(MessageKeys.ERR_USER_DELETING_ITSELF)));
            }
            if (e.getType() == ProviderException.Type.ACCESS_DENIED || e.getType() == ProviderException.Type.NOT_FOUND) {
                return handle(e);
            }
            throw e;
        }
    }
    
    public HttpResponse resendLink(ResetPassReqDto dto) {
        User user = up.getUserByLogin(dto.getLogin());
        if(user == null)
            return ok("");
        if(!user.isBlocked() || user.isEmailValid())
            return ok("");
        sendActivationToken(user);
        return ok("");
    }
    
    private void sendActivationToken(User user) {
        String token = user.getEmailValidationToken();
        if(token != null) {
            String url = requestContext.getApiRoot()+"/users/activate/"+token;
            sender.sendMessage(user.getEmail(), "Email confirmation",
                    emailConfirmationContent(url));
        }
    }
    
    public HttpResponse requestPasswordReset(ResetPassReqDto dto) throws ProviderException{
        if(dto == null || dto.getLogin() == null)
            return badRequest();
        
        try {
            String token = up.requestPasswordReset(dto.getLogin());
            sender.sendMessage(dto.getLogin(), "Resetowanie hasła", 
                    passResetReqContent(requestContext.getApiRoot()+"/users/reset/"+token));
            return ok("");
        } catch (ProviderException p) {
            return handle(p);
        }
    }
    
    public String resetPassword(String token) throws ProviderException {
        try {
            User u = up.resetPassword(token);
            sender.sendMessage(u.getLogin(), "Nowe hasło",
                    newPasswordContent(u.getPasswordRaw()));
        } catch(ProviderException e) {
            if(e.getType() == ProviderException.Type.NOT_FOUND)
                return "<html><head></head><body>Niepoprawny link</body></html>";
        }
        return "<html><head></head><body>"
                + "<h1>Nowe hasło zostało wysłane na adres e-mail</h1>"
                + "</body></html>";
    }
    
    private HttpResponse getUserSettings(long id) throws ProviderException {
        try {
            UserSettings us = up.getUserSettings(id);
            return ok(new UserSettingsDto.Builder().userSettings(us).build());
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse updateUserSettings(long id, EditUserSettingsDto dto) throws ProviderException {
        if(id != requestContext.getUser().getId())
            return forbidden();
        List<ErrorDto> errors = EditUserSettingsDto.validate(dto);
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            up.updateUserSettings(id, dto);
        } catch(ProviderException e) {
            return handle(e);
        }
        return ok("");
    }
    
    public HttpResponse updateUserBleDevices(long id, List<BleDeviceDto> dtos) 
            throws ProviderException {
        List<ErrorDto> errors = new ArrayList<ErrorDto>();
        for(BleDeviceDto dto : dtos)
            errors.addAll(BleDeviceDto.validate(dto));
        if(!errors.isEmpty())
            return badRequest(errors);
        try {
            up.updateUserBleDevices(id, dtos);
        } catch(ProviderException e) {
            return handle(e);
        }
        return ok("");
    }
    
    private static String emailConfirmationContent(String url) {
        url = url.replace("46.41.148.107", "gps.petio.eu").replace("46.41.149.43", "trackman.pl");
        Map<String, String> appInfo = Application.getAppInfo();
        
        return String.format("Witaj,<br/><br/>" +
                "Dziękujemy za założenie konta w systemie " + appInfo.get("appName") + ".<br/>" +
                "Twoje konto jest nieaktywne. Aby aktywować konto kliknij w poniższy link.<br/><br/><br/>" +
                "<a href=\"%s\">Link do aktywacji konta</a><br/><br/>" +
                "bądź skopiuj poniższy link i wklej do przeglądarki w pasku adresu.<br/><br/>" +
                "%s<br/><br/>" +
                "Uwaga: link aktywacyjny ważny jest przez 7 dni.<br/><br/><br/>" +
                "Dziękujemy,<br/><br/>" +
                "Zespół serwisu " + appInfo.get("appName") + "<br/><br/>" +
                "Ten email został wygenerowany automatycznie - nie odpowiadaj na niego.",
                url, url);
    }
    
    private static String passResetReqContent(String url) {
        url = url.replace("46.41.148.107", "gps.petio.eu").replace("46.41.149.43", "trackman.pl");
        Map<String, String> appInfo = Application.getAppInfo();
        
        return String.format("Witaj,<br/><br/>"
                + "Odnotowaliśmy próbę odzyskania hasła do konta w systemie " + appInfo.get("appName") + ".<br/>"
                + "Nowe hasło zostanie wysłane na adres e-mail po kliknięciu poniższego linku:<br/><br/>"
                + "%s<br/><br/>"
                + "Zespół serwisu " + appInfo.get("appName") +  "<br/><br/>"
                + "Ten email został wygenerowany automatycznie - nie odpowiadaj na niego.", 
                url);
    }
    
    private static String newPasswordContent(String pass) {
        return String.format("Witaj,<br/><br/>"
                + "Twoje hasło zostało skutecznie zresetowane. Nowe hasło:<br/><br/>"
                + "%s<br/><br/>"
                + "W celu zwiększenia bezpieczeństwa prosimy o zmianę wygenerowanego hasła "
                + "po zalogowaniu się do systemu.<br/>"
                + "Zespół serwisu " + Application.getAppInfo().get("appName") +  "<br/><br/>"
                + "Ten email został wygenerowany automatycznie - nie odpowiadaj na niego.",
                pass);
    }
    
    public HttpResponse activateUser(String token) {
        try {
            up.activateUser(token);
            return redirect(Application.getAppInfo().get("afterRegisterLink"));
        } catch (ProviderException ex) {
            return ok("");
        }
    }
}
