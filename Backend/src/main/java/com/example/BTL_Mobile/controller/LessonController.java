package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import com.example.BTL_Mobile.dto.response.lesson.QuestionDetailDTO;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.service.facade.LessonFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.LessonInTopicResponse;
import com.example.BTL_Mobile.service.LessonService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {

    private final LessonFacadeService lessonFacadeService;
    private final LessonService lessonService;

    @GetMapping("/lessons/{lessonId}/questions")
    @Operation(summary = "Lấy danh sách câu hỏi của lesson khi người dùng bắt đầu làm bài",
            description = "Trả về thông tin câu hỏi kèm các câu trả lời để xử lý ở client")
    public ResponseEntity<List<QuestionDetailDTO>> getQuestions(@PathVariable int lessonId){
        List<QuestionDetailDTO> questions = lessonFacadeService.getQuestions(lessonId);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/topics/{topicId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonInTopicResponse>>> getLessonsByTopic(
            @PathVariable Integer topicId
    ) {
        List<LessonInTopicResponse> data = lessonService.getLessonsByTopic(topicId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách bài học theo topic thành công!", data));
    }
    @RequestMapping(path = "/lessons/{lessonId}/submit", method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Submit bài làm lesson của người dùng",
            description = "Gửi các câu trả lời của câu hỏi trong lesson")
    public ResponseEntity<Void> submit(@PathVariable int lessonId,
                                       @RequestBody SubmitLessonRequest submitLesson,
                                       @AuthenticationPrincipal User user){
        lessonFacadeService.submit(lessonId, submitLesson);
        return ResponseEntity.ok().build();
    }

}

