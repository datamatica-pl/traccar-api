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
package pl.datamatica.traccar.api.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private static final SimpleDateFormat RFC_1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    private static final SimpleDateFormat RFC_850 = new SimpleDateFormat("EEEE,  dd-MMM-yy HH:mm:ss z", Locale.US);
    private static final SimpleDateFormat ASCTIME = new SimpleDateFormat("EEE MMM  d HH:mm:ss yyyy", Locale.US);

    static {
        RFC_1123.setTimeZone(TimeZone.getTimeZone("GMT"));
        RFC_850.setTimeZone(TimeZone.getTimeZone("GMT"));
        ASCTIME.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static synchronized Date parseDate(String in) throws ParseException {
        ParseException lastException = null;
        for(SimpleDateFormat format : getSupportedFormats())
            try{
                return format.parse(in);
            } catch(ParseException e) {
                lastException = e;
            }
        throw lastException;
    }

    public static synchronized String formatDate(Date in) {
        return RFC_1123.format(in);
    }

    private static SimpleDateFormat[] getSupportedFormats() {
        return new SimpleDateFormat[]{RFC_1123, RFC_850, ASCTIME};
    }
}
