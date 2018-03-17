package com.forceFilesEditor.controller;

import com.forceFilesEditor.dto.ReviewRequest;
import com.forceFilesEditor.pmd.PmdReviewService;
import net.sourceforge.pmd.RuleViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/review")
public class PMDReviewController {

    private final PmdReviewService pmdReviewService;

    public PMDReviewController(PmdReviewService pmdReviewService) {
        this.pmdReviewService = pmdReviewService;
    }

    @PostMapping
    public ResponseEntity<?> review(@RequestBody ReviewRequest reviewRequest) {
        List<RuleViolation> ruleViolations = pmdReviewService.review(reviewRequest.getData(), reviewRequest.getFileName());
        return ResponseEntity.ok(ruleViolations);
    }
}
