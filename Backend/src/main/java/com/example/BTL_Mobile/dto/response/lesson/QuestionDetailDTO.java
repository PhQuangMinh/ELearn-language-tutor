package com.example.BTL_Mobile.dto.response.lesson;

import com.example.BTL_Mobile.dto.response.MediaDTO;
import com.example.BTL_Mobile.model.enums.EQuestionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionDetailDTO {

    private int id;

    private String content;

    private EQuestionType type;

    private MediaDTO media;

    private List<AnswerDTO> answers;

    private boolean repeatable;

}
