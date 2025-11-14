package com.ianctchinese.controller;

import com.ianctchinese.dto.AutoAnnotationResponse;
import com.ianctchinese.dto.ClassificationResponse;
import com.ianctchinese.dto.TextInsightsResponse;
import com.ianctchinese.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class TextAnalysisController {

  private final AnalysisService analysisService;

  @PostMapping("/{textId}/classify")
  public ResponseEntity<ClassificationResponse> classify(@PathVariable Long textId) {
    return ResponseEntity.ok(analysisService.classifyText(textId));
  }

  @PostMapping("/{textId}/auto-annotate")
  public ResponseEntity<AutoAnnotationResponse> autoAnnotate(@PathVariable Long textId) {
    return ResponseEntity.ok(analysisService.autoAnnotate(textId));
  }

  @GetMapping("/{textId}/insights")
  public ResponseEntity<TextInsightsResponse> insights(@PathVariable Long textId) {
    return ResponseEntity.ok(analysisService.buildInsights(textId));
  }
}
