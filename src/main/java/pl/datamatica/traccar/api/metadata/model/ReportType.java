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
package pl.datamatica.traccar.api.metadata.model;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;

/**
 *
 * @author Jan Usarek
 */
@Entity
@Table( name = "report_types" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
@SQLDelete(sql="UPDATE report_types d SET d.isDeleted = 1 WHERE d.id = ?")
@FilterDef(name="softDelete", defaultCondition="isDeleted = 0")
@Filter(name="softDelete")
public class ReportType extends UpdateTimestampedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true, length=100)
    private String reportName;

    @Column(length=500)
    private String imageUrl;

    @Column(length=100)
    private String descriptionShort;

    @Column(length=500)
    private String descriptionLong;

    @OneToMany(fetch=FetchType.EAGER)
    @JoinColumn(name="ReportTypeId", nullable = false)
    private List<ReportParameter> parameters;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isDeleted;

    public long getId() {
        return id;
    }

    public String getReportName() {
        return reportName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDescriptionShort() {
        return descriptionShort;
    }

    public String getDescriptionLong() {
        return descriptionLong;
    }

    public List<ReportParameter> getParameters() {
        return parameters;
    }

    public boolean isIsDeleted() {
        return isDeleted;
    }

}
