package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.response.LessonInTopicResponse;
import com.example.BTL_Mobile.dto.response.lesson.LessonSubmittedDTO;
import com.example.BTL_Mobile.dto.response.streak.StreakExtendedDTO;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.dto.request.lesson.SubmitLessonRequest;
import com.example.BTL_Mobile.dto.request.lesson.SubmitQuestionDTO;
import com.example.BTL_Mobile.mapper.UserLessonResultMapper;
import com.example.BTL_Mobile.model.*;
import com.example.BTL_Mobile.model.enums.EQuestionType;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.UserLessonResultRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import com.example.BTL_Mobile.security.CurrentUserContext;
import com.example.BTL_Mobile.service.LessonService;
import com.example.BTL_Mobile.service.UserStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final TopicRepository topicRepository;
    private final LessonRepository lessonRepository;
    private final CurrentUserContext currentUserContext;

    private final UserLessonResultRepository lessonResultRepository;

    private final UserLessonResultMapper lessonResultMapper;

    private final UserStreakService userStreakService;

    @Override
    public Optional<Lesson> getLessonById(int id) {
        return lessonRepository.findById(id);
    }

    @Override
    public List<LessonInTopicResponse> getLessonsByTopic(Integer topicId) {
        if (topicId == null) {
            throw new BusinessException("TopicId không hợp lệ", "INVALID_TOPIC_ID");
        }

        if (!topicRepository.existsById(topicId)) {
            throw new BusinessException("Topic không tồn tại", "TOPIC_NOT_FOUND");
        }

        Integer userId = currentUserContext.requireUserId();

        List<Lesson> lessons = lessonRepository.findByTopic_IdOrderByIdAsc(topicId);
        Set<Integer> completedLessonIds = new HashSet<>(
                lessonResultRepository.findCompletedLessonIdsInTopic(userId, topicId)
        );

        return lessons.stream()
            .map(lesson -> LessonInTopicResponse.builder()
                .id(lesson.getId())
                .name(lesson.getTitle())
                .imageUrl(lesson.getImageUrl())
                .completed(completedLessonIds.contains(lesson.getId()))
                .build())
            .toList();
    }

    @Override
    public LessonSubmittedDTO submitLesson(int lessonId, SubmitLessonRequest submitLesson) {
        Lesson lesson = getLessonById(lessonId)
                .orElseThrow(() -> new BusinessException("Lesson not found"));
        validateSubmittedLesson(lesson, submitLesson);
        int userId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        UserLessonResult existedResult = getResultOfUser(userId, lessonId);
        double score = calculateLessonScore(lesson, submitLesson);
        if(existedResult == null) saveNewResult(lesson, submitLesson, score);
        else if(existedResult.getScore() < score) updateResult(existedResult, submitLesson, score);

        StreakExtendedDTO streakExtended = userStreakService.extendStreak(userId);
        return LessonSubmittedDTO.builder()
                .streakExtended(streakExtended.isExtended())
                .currentStreak(streakExtended.getCurrentStreak())
                .build();
    }

    private void validateSubmittedLesson(Lesson lesson, SubmitLessonRequest submitLesson) {
        Map<Integer, Integer> submittedQuestionIds = submitLesson.getQuestionAnswers()
                .stream()
                .collect(Collectors.toMap(SubmitQuestionDTO::getId, q -> q.getAnswer().getId()));
        boolean invalid = lesson.getQuestions().stream()
                .anyMatch(q -> {
                    if(!submittedQuestionIds.containsKey(q.getId())) return true;
                    if(q.getType() != EQuestionType.ONE_SELECTION) return false;
                    return !q.getAnswers().stream()
                            .map(Answer::getId)
                            .collect(Collectors.toSet())
                            .contains(submittedQuestionIds.get(q.getId()));
                });
        if (invalid) throw new BusinessException("Invalid submitted questions");
    }

    public UserLessonResult getResultOfUser(int userId, int lessonId) {
        User user = User.builder().id(userId).build();
        Lesson lesson = new Lesson();
        lesson.setId(lessonId);
        return lessonResultRepository.findByUserAndLesson(user, lesson)
                .orElse(null);
    }

    // Điểm chia theo tỉ lệ * 100 để tránh số thập phân nhỏ
    // VD: đúng 7/10 => 70 điểm
    public double calculateLessonScore(Lesson lesson, SubmitLessonRequest submitLesson) {
        Map<Integer, Question> questionMap = lesson.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        int correctCount = 0;
        for(SubmitQuestionDTO q : submitLesson.getQuestionAnswers()){
            Question question = questionMap.get(q.getId());
            if(question.getType() == EQuestionType.ONE_SELECTION) {
                Answer correctAns = question.getAnswers().stream()
                        .filter(Answer::isCorrect)
                        .toList()
                        .get(0);
                if(correctAns.getId() == q.getAnswer().getId()) correctCount++;
            }
            else {
                Answer correctAns = question.getAnswers().stream().toList().get(0);
                if(correctAns.getContent().equals(q.getAnswer().getContent())) correctCount++;
            }
        }
        return 100.0 * correctCount / questionMap.size();
    }

    private void saveNewResult(Lesson lesson, SubmitLessonRequest submitLesson, double score) {
        UserLessonResult lessonResult = lessonResultMapper.toModel(lesson, submitLesson, score);
        lessonResultRepository.save(lessonResult);
    }

    private void updateResult(UserLessonResult existedResult, SubmitLessonRequest submitLesson, double score) {
        Lesson lesson = existedResult.getLesson();
        lessonResultRepository.delete(existedResult);
        saveNewResult(lesson, submitLesson, score);
    }

}
