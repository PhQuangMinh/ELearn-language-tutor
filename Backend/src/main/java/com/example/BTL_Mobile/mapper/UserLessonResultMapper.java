package com.example.BTL_Mobile.mapper;

import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import com.example.BTL_Mobile.model.*;
import com.example.BTL_Mobile.model.enums.EQuestionType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserLessonResultMapper {

    public UserLessonResult toModel(Lesson lesson, SubmitLessonRequest submitLesson, double score) {
        int userId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        User user = User.builder().id(userId).build();
        UserLessonResult newResult = new UserLessonResult();
        newResult.setLesson(lesson);
        newResult.setUser(user);
        newResult.setStartedAt(submitLesson.getStartedAt());
        newResult.setDuration((int) ChronoUnit.SECONDS
                .between(submitLesson.getStartedAt(), submitLesson.getEndedAt()));
        newResult.setScore(score);

        Map<Integer, Question> questionMap = lesson.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        Set<UserQuestionAnswer> answers = submitLesson.getQuestionAnswers().stream()
                .map(qa -> {
                    Question q = questionMap.get(qa.getId());
                    UserQuestionAnswer uqa = new UserQuestionAnswer();
                    uqa.setLessonResult(newResult);
                    uqa.setQuestion(q);
                    if(q.getType() == EQuestionType.ONE_SELECTION) {
                        q.getAnswers().stream()
                                .filter(a -> a.getId() == qa.getAnswer().getId())
                                .findFirst().ifPresent(uqa::setAnswer);
                    }
                    else{
                        uqa.setContent(qa.getAnswer().getContent());
                    }
                    return uqa;
                })
                .collect(Collectors.toSet());
        newResult.setUserQuestionAnswers(answers);
        return newResult;
    }

}
