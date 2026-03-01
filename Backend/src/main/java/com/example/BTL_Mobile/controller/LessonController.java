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

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonFacadeService lessonFacadeService;

    @GetMapping("/{lessonId}/questions")
    @Operation(summary = "Lấy danh sách câu hỏi của lesson khi người dùng bắt đầu làm bài",
            description = "Trả về thông tin câu hỏi kèm các câu trả lời để xử lý ở client")
    public ResponseEntity<List<QuestionDetailDTO>> getQuestions(@PathVariable int lessonId){
        List<QuestionDetailDTO> questions = lessonFacadeService.getQuestions(lessonId);
        return ResponseEntity.ok(questions);
    }

    @RequestMapping(path = "/{lessonId}/submit", method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Submit bài làm lesson của người dùng",
            description = "Gửi các câu trả lời của câu hỏi trong lesson")
    public ResponseEntity<Void> submit(@PathVariable int lessonId,
                                       @RequestBody SubmitLessonRequest submitLesson,
                                       @AuthenticationPrincipal User user){
        lessonFacadeService.submit(lessonId, submitLesson);
        return ResponseEntity.ok().build();
    }

}
