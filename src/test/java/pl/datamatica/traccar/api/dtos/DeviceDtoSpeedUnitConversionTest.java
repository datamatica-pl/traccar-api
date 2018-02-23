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

import java.util.Arrays;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import pl.datamatica.traccar.api.dtos.out.DeviceDto;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.User;

/**
 *
 * @author Jan Usarek
 */
public class DeviceDtoSpeedUnitConversionTest {

    @Test
    public void testSpeedLimitUnitConversion() throws Exception {
        Device testDevice = getTestDevice();
        double speedNauticalMiles = 1000D;
        int expectedSpeedKmH = 1852;

        testDevice.setSpeedLimit(speedNauticalMiles);
        DeviceDto deviceDto = new DeviceDto.Builder().device(testDevice).build();

        assertEquals(expectedSpeedKmH, deviceDto.getSpeedLimit().intValue());
    }

    @Test
    public void testIdleSpeedThresholdUnitConversion() throws Exception {
        Device testDevice = getTestDevice();
        double speedNauticalMiles = 1000D;
        int expectedSpeedKmH = 1852;

        testDevice.setIdleSpeedThreshold(speedNauticalMiles);
        DeviceDto deviceDto = new DeviceDto.Builder().device(testDevice).build();

        assertEquals(expectedSpeedKmH, deviceDto.getIdleSpeedThreshold().intValue());
    }

    private Device getTestDevice() {
        User owner = new User();
        Device device = new Device();

        device.setName("Test Device");
        device.setOwner(owner);
        device.setUsers(new HashSet<>(Arrays.asList(owner)));

        return device;
    }
}
