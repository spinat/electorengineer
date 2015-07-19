package de.electroengineer.controllers;

import de.electroengineer.services.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ImportController {

    @Autowired
    private ImportService importService;

    private static Logger LOG = LoggerFactory.getLogger(ImportController.class);

    @RequestMapping(value = "/api/import", method = RequestMethod.GET)
    public String importMeasures() throws IOException {

        LOG.info("Request to /api/import");

        importService.startImport();

        return "Import done!";
    }
}
