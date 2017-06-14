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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ImageProvider {
    
    private final File rootDirectory;
    private static Image emptyMarker;
    private static final Map<String, byte[]> markerCache = new HashMap<>();
    
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
    
    public byte[] getMarker(String name) throws IOException {
        if(!markerCache.containsKey(name)) {
            Image icon = getImage(name+".png");
            if(icon == null)
                return null;
            float l = 30f/141, t=28f/189, r=110f/141, b=108f/189;
            int w = emptyMarker.getWidth(null), h = emptyMarker.getHeight(null);
            BufferedImage marker = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            marker.getGraphics().drawImage(emptyMarker, 0, 0, null);
            marker.getGraphics().drawImage(icon, (int)Math.round(l*w), 
                    (int)Math.round(t*h), (int)Math.round((r-l)*w), 
                    (int)Math.round((b-t)*h), null);

            ByteArrayOutputStream boss = new ByteArrayOutputStream();
            ImageIO.write((RenderedImage)marker, "png", boss);
            markerCache.put(name, boss.toByteArray());
        }
        return markerCache.get(name);
    }
    
    public FileInputStream getInputStreamForImage(String name) {
        try {
            return new FileInputStream(new File(rootDirectory, name + ".png"));
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            return null;
        }
    }
    
    
    public static final void setEmptyMarker(Image img) {
        emptyMarker = img;
    }
}
