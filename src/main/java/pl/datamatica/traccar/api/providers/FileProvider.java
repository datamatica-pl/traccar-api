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
package pl.datamatica.traccar.api.providers;

import pl.datamatica.traccar.api.dtos.out.FileDto;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.stream.Stream;

public class FileProvider {
    private final Charset charset;
    private final File rootDirectory;
    
    public FileProvider(String rootDirectory) {
        this.rootDirectory = new File(rootDirectory);
        this.charset = StandardCharsets.UTF_8;
    }
    
    public Date getListLastModified() {
        return new Date(rootDirectory.lastModified());
    }
    
    public Stream<FileDto> getAllFiles() {
        return Stream.of(rootDirectory.listFiles())
                .map(f -> new FileDto(f.getName(), f.lastModified()));
    }
    
    public String getFileContent(String id) throws IOException {
        File file = new File(rootDirectory, id);
        return new String(Files.readAllBytes(file.toPath()), charset);
    }
    
    public FileDto getFileInfo(String id) {
        File file = new File(rootDirectory, id);
        return new FileDto(file.getName(), file.lastModified());
    }
}
