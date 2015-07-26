package de.electroengineer.controllers;

import de.electroengineer.domain.Evaluation;
import de.electroengineer.services.EvaluationService;
import de.electroengineer.services.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class EvaluationController {

    public static final String API_EVALUATION_SHOW = "/api/evaluation/{evaluationName}";
    public static final String API_EVALUATION_LIST = "/api/evaluation/list";
    public static final String API_EVALUATION_CALC = "/api/evaluation/{evaluationName}/calc/{rmsAmperePeriodMs}/{rmsVoltPeriodMs}";
    private static Logger LOG = LoggerFactory.getLogger(EvaluationController.class);

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    FileService fileService;

    @RequestMapping(API_EVALUATION_LIST)
    public List<String> listEvaluation() throws IOException {
        LOG.info("Request to {}.", API_EVALUATION_LIST);

        List<String> allEvaluations = evaluationService.findAllEvaluations();
        return allEvaluations;
    }

    @RequestMapping(API_EVALUATION_SHOW)
    public Evaluation showEvaluation(@PathVariable String evaluationName) throws IOException {
        LOG.info("Request to {}. evaluationName={}", API_EVALUATION_SHOW, evaluationName);

        Evaluation evaluation = evaluationService.getEvaluation(evaluationName);

//        evaluationService.generatePreviewData(evaluation);

        return evaluation;
    }

    @RequestMapping(value = API_EVALUATION_CALC, method = RequestMethod.POST)
    public Evaluation calculate(@PathVariable("evaluationName") String evaluationName,
                                @PathVariable("rmsAmperePeriodMs") Double rmsAmperePeriodMs,
                                @PathVariable("rmsVoltPeriodMs") Double rmsVoltPeriodMs) throws IOException {

        LOG.info("Request to {}. evaluationName={}, rmsAmperePeriodMs={}, rmsVoltPeriodMs={}", API_EVALUATION_CALC, evaluationName, rmsAmperePeriodMs, rmsVoltPeriodMs);

        Evaluation evaluation = evaluationService.getEvaluation(evaluationName);

        evaluation.setRmsAmperePeriodMs(rmsAmperePeriodMs);
        evaluation.setRmsVoltPeriodMs(rmsVoltPeriodMs);

        evaluationService.calc(evaluation);
        fileService.storeEvaluation(evaluation);

        evaluationService.generatePreviewData(evaluation);

        return evaluation;
    }
}
