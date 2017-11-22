/*
 * Copyright 2015 Vitaly Litvak (vitavaque@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.datamatica.traccar.api.reports;

import pl.datamatica.traccar.model.ReportType;

public class ReportGeneratorFactory {
    
    public ReportGenerator getGenerator(ReportType type) {
        switch(type) {
            case GENERAL_INFORMATION:
                return new ReportGI();
            case DRIVES_AND_STOPS:
                return new ReportDS();
            case OVERSPEEDS:
                return new ReportOS();
            case GEO_FENCE_IN_OUT:
                return new ReportGFIO();
            case EVENTS:
                return new ReportEV();
            case MILEAGE_DETAIL:
                return new ReportMD();
            case FUEL_CONSUMPTION:
                return new ReportFuel();
            case TRACK:
                return new ReportTrack();
            default:
                return null;
        }
    }
}
