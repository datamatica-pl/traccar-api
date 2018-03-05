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

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Jan Usarek
 */
public class ImeiNumberDto {

    private long id;
    private String imei;
    private String email;
    private String contactPhone;
    private String deviceModel;

    private String firstName;
    private String lastName;
    private String invoiceNumber;
    private String comment;

    public long getId() {
        return id;
    }

    public String getImei() {
        return imei;
    }

    public String getEmail() {
        return email;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getComment() {
        return comment;
    }

    public void trimAllStrings() {
        imei = StringUtils.trim(imei);
        email = StringUtils.trim(email);
        contactPhone = StringUtils.trim(contactPhone);
        firstName = StringUtils.trim(firstName);
        lastName = StringUtils.trim(lastName);
        invoiceNumber = StringUtils.trim(invoiceNumber);
        comment = StringUtils.trim(comment);
    }
}
