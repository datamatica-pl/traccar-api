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
package pl.datamatica.traccar.api.controllers;

import pl.datamatica.traccar.api.reports.ReportGenerator;
import pl.datamatica.traccar.api.reports.ReportGeneratorFactory;
import pl.datamatica.traccar.model.Report;

import pl.datamatica.traccar.api.Application;
import spark.Request;
import spark.Response;
import spark.Spark;

public class ReportsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder{

        @Override
        public void bind() {
            Spark.post(rootUrl()+"/*", (req, res) -> {
                ReportsController rc = createController(req);
                Report report = gson.fromJson(req.queryParams("report"), Report.class);
                String lang = req.queryParams("lang");
                return rc.generateReport(report, lang, res);
            });
            
            Spark.post(rootUrl(), (req, res)-> {
                ReportsController rc = createController(req);
                Report report = gson.fromJson(req.queryParams("report"), Report.class);
                String lang = req.queryParams("lang");
                return rc.generateReport(report, lang, res);
            });
        }

        @Override
        public String rootUrl() {
            return super.rootUrl() + "/reports";
        }
        
        public ReportsController createController(Request req) {
            RequestContext context = req.attribute(Application.REQUEST_CONTEXT_KEY);
            ReportsController rc = new ReportsController(context);
            return rc;
        }
    }
    
    private ReportGeneratorFactory generators;

    public ReportsController(RequestContext requestContext) {
        super(requestContext);
    }

    public String generateReport(Report report, String lang, Response res) throws Exception {
        ReportGenerator generator = requestContext.getReportGenerator(report.getType(), 
                lang);
        if (generator == null) {
            render(notFound(), res);
            return "";
        }

        return generator.generate(report, res.raw(), lang);
    }
}
