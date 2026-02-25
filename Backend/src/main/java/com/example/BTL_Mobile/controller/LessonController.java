package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.lesson.QuestionDetailDTO;
import com.example.BTL_Mobile.service.facade.LessonFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
