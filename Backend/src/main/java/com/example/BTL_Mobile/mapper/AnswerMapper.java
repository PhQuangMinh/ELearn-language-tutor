package com.example.BTL_Mobile.mapper;

import com.example.BTL_Mobile.dto.response.lesson.AnswerDTO;
import com.example.BTL_Mobile.model.Answer;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnswerMapper {
    AnswerDTO toAnswerDTO(Answer answer);
}
