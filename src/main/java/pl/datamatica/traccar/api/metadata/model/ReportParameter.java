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
import javax.persistence.*;

/**
 *
 * @author Jan Usarek
 */
@Entity
@Table( name = "report_parameters" )
@PersistenceContext(unitName = "traccar_api_metadata_persistence")
public class ReportParameter extends UpdateTimestampedEntity implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true, length=100)
    private String parameterKey;

    @Column(nullable = false, unique = true, length=100)
    private String parameterName;

    @Column(nullable = false, length=100)
    private String parameterValue;

    @Column(length=100)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="reportTypeId", nullable = false)
    private ReportType reportType;

    public long getId() {
        return id;
    }

    public String getParameterKey() {
        return parameterKey;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public String getDescription() {
        return description;
    }

    public ReportType getReportType() {
        return reportType;
    }
}
