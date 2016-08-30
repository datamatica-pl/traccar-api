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
package pl.datamatica.traccar.api.dtos.out;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Łukasz Ławniczak
 */
public class DeviceListDto implements Collection<DeviceDto> {
    private final List<DeviceDto> changedDevices;
    private final List<Long> deviceIds;
    
    public DeviceListDto(List<DeviceDto> changedDevices, List<Long> devicesIds) {
        this.changedDevices = changedDevices;
        this.deviceIds = devicesIds;
    }

    public List<DeviceDto> getChangedDevices() {
        return changedDevices;
    }

    public List<Long> getDeviceIds() {
        return deviceIds;
    }

    @Override
    public int size() {
        return changedDevices.size();
    }

    @Override
    public boolean isEmpty() {
        return changedDevices.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return changedDevices.contains(o);
    }

    @Override
    public Iterator<DeviceDto> iterator() {
        return changedDevices.iterator();
    }

    @Override
    public Object[] toArray() {
        return changedDevices.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return changedDevices.toArray(a);
    }

    @Override
    public boolean add(DeviceDto e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends DeviceDto> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
