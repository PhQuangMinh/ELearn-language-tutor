package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.ScenarioDetailResponse;
import com.example.BTL_Mobile.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scenarios")
@RequiredArgsConstructor
public class ScenarioController {

  private final ScenarioService scenarioService;

  @GetMapping("/by-lesson/{lessonId}")
  @Operation(summary = "Lấy thông tin scenario theo lesson_id",
          description = "Vì 1 lesson chỉ có 1 scenario nên trả về 1 object")
  public ResponseEntity<ApiResponse<ScenarioDetailResponse>> getScenarioByLesson(
          @PathVariable int lessonId
  ) {
    ScenarioDetailResponse data = scenarioService.getScenarioByLessonId(lessonId);
    return ResponseEntity.ok(ApiResponse.success(data));
  }
}

