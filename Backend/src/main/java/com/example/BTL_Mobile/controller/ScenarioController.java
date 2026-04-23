package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.InitSpeakingSessionResponse;
import com.example.BTL_Mobile.dto.response.ScenarioDetailResponse;
import com.example.BTL_Mobile.service.ScenarioService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @PostMapping("/{scenarioId}/speaking-session/init")
  public ResponseEntity<ApiResponse<InitSpeakingSessionResponse>> initSession(@PathVariable int scenarioId){
    InitSpeakingSessionResponse response = scenarioService.initSpeakingSession(scenarioId);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  @PostMapping("/speaking-session/{sessionId}/end")
  public ResponseEntity<Void> endSpeakingSession(@PathVariable int sessionId){
    scenarioService.endSpeakingSession(sessionId);
    return ResponseEntity.ok().build();
  }

}

