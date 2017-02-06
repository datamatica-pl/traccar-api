/*
 *  Copyright (C) 2016  Datamatica (dev@datamatica.pl)
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *  
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *  
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package pl.datamatica.traccar.api.providers;

import javax.persistence.EntityManager;
import pl.datamatica.traccar.model.Picture;
import pl.datamatica.traccar.model.PictureType;

public class PicturesProvider {
    private final EntityManager em;
    
    public PicturesProvider(EntityManager em) {
        this.em = em;
    }
    
    public Picture getImage(Long id) {
        if(id == null)
            return null;
        return em.find(Picture.class, id);
    }
    
    public Picture createPicture(byte[] data) {
        Picture picture = new Picture();
        picture.setType(PictureType.ICON);
        picture.setWidth(192);
        picture.setHeight(192);
        picture.setData(data);
        picture.setMimeType("image/png;charset=utf-8");
        em.persist(picture);
        return picture;
    }

    public void deletePictureIfExists(long id) {
        Picture picture = em.find(Picture.class, id);
        if(picture != null)
            em.remove(picture);
    }
}
