package com.example.BTL_Mobile.mapper;

import com.example.BTL_Mobile.dto.response.lesson.QuestionDetailDTO;
import com.example.BTL_Mobile.model.Question;
import org.mapstruct.*;

import java.util.ArrayList;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {ArrayList.class})
public abstract class QuestionMapper {

    public abstract QuestionDetailDTO toQuestionDetailDTO(Question question);

}
