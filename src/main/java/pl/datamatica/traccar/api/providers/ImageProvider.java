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

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageProvider {
    
    private final File rootDirectory;
    
    public ImageProvider(String rootDirectory) {
        this.rootDirectory = new File(rootDirectory);
    }
    
    // TODO: It should have some cache logic.
    public Image getImage(String name) {
        File source = new File(rootDirectory, name);
        try {
            return ImageIO.read(source);
        } catch (IOException ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    public FileInputStream getInputStreamForImage(String name) {
        try {
            return new FileInputStream(new File(rootDirectory, name + ".png"));
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            return null;
        }
    }
}
