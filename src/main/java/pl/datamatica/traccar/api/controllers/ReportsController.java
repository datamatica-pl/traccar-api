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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import pl.datamatica.traccar.api.reports.ReportGenerator;
import pl.datamatica.traccar.api.reports.ReportGeneratorFactory;
import pl.datamatica.traccar.model.Report;

import pl.datamatica.traccar.api.Application;
import static pl.datamatica.traccar.api.controllers.ControllerBase.render;
import pl.datamatica.traccar.api.dtos.in.EditReportDto;
import pl.datamatica.traccar.api.dtos.out.ReportDto;
import pl.datamatica.traccar.api.providers.ProviderException;
import pl.datamatica.traccar.api.providers.ReportProvider;
import pl.datamatica.traccar.api.responses.HttpResponse;
import pl.datamatica.traccar.model.Device;
import pl.datamatica.traccar.model.GeoFence;
import spark.Request;
import spark.Response;
import spark.Spark;

public class ReportsController extends ControllerBase {
    
    public static class Binder extends ControllerBinder{

        @Override
        public void bind() {
            Spark.post(rootUrl()+"/generate/*", (req, res) -> {
                ReportsController rc = createController(req);
                Report report = gson.fromJson(req.queryParams("report"), Report.class);
                String lang = req.queryParams("lang");
                return rc.generateReport(report, lang, res);
            });
            
            Spark.post(rootUrl()+"/generate", (req, res)-> {
                ReportsController rc = createController(req);
                Report report = gson.fromJson(req.queryParams("report"), Report.class);
                String lang = req.queryParams("lang");
                return rc.generateReport(report, lang, res);
            });
            
            Spark.get(rootUrl(), (req, res) -> {
                ReportsController rc = createController(req);
                return render(rc.getAllReports(), res);
            }, gson::toJson);
            
            Spark.post(rootUrl(), (req, res) -> {
                ReportsController rc = createController(req);
                EditReportDto report = gson.fromJson(req.body(), EditReportDto.class);
                return render(rc.createReport(report), res);
            }, gson::toJson);
            
            Spark.put(rootUrl()+"/:id", (req, res) -> {
                ReportsController rc = createController(req);
                EditReportDto report = gson.fromJson(req.body(), EditReportDto.class);
                long id = Long.parseLong(req.params(":id"));
                return render(rc.updateReport(id, report), res);
            }, gson::toJson);
            
            Spark.delete(rootUrl()+"/:id", (req, res) -> {
                ReportsController rc = createController(req);
                long id = Long.parseLong(req.params(":id"));
                return render(rc.removeReport(id), res);
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
    
    public HttpResponse getAllReports() throws ProviderException {
        ReportProvider rp = requestContext.getReportProvider();
        try {
            List<ReportDto> reports = rp.getReports().map(r -> new ReportDto.Builder().report(r).build())
                    .collect(Collectors.toList());
            return ok(reports);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse createReport(EditReportDto report) throws ProviderException {
        ReportProvider rp = requestContext.getReportProvider();
        try {
            Report r = rp.createReport(report);
            ReportDto dto = new ReportDto.Builder().report(r).build();
            return created("reports/"+r.getId(), dto);
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse updateReport(long id, EditReportDto report) throws ProviderException {
        ReportProvider rp = requestContext.getReportProvider();
        try {
            rp.updateReport(id, report);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
    
    public HttpResponse removeReport(long id) throws ProviderException {
        ReportProvider rp = requestContext.getReportProvider();
        try{
            rp.removeReport(id);
            return ok("");
        } catch(ProviderException e) {
            return handle(e);
        }
    }
}
