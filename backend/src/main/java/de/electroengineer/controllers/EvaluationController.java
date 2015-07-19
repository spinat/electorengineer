package de.electroengineer.controllers;

import de.electroengineer.domain.Evaluation;
import de.electroengineer.services.EvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class EvaluationController {

    public static final String API_EVALUATION_SHOW = "/api/evaluation/{evaluationName}";
    public static final String API_EVALUATION_LIST = "/api/evaluation/list";
    private static Logger LOG = LoggerFactory.getLogger(EvaluationController.class);

    @Autowired
    EvaluationService evaluationService;

    @RequestMapping(API_EVALUATION_SHOW)
    public Evaluation showEvaluation(@PathVariable String evaluationName) throws IOException {

        LOG.info("Request to {}. evaluationName={}", API_EVALUATION_SHOW, evaluationName);
        Evaluation evaluation = evaluationService.getEvaluation(evaluationName);

        return evaluation;
    }

    @RequestMapping(API_EVALUATION_LIST)
    public List<String> listEvaluation() throws IOException {
        LOG.info("Request to {}.", API_EVALUATION_LIST);

        List<String> allEvaluations = evaluationService.findAllEvaluations();
        return allEvaluations;
    }
}
