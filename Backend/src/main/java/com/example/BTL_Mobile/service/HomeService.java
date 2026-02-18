package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.CourseProgressResponse;
import com.example.BTL_Mobile.dto.response.CurrentLessonResponse;
import com.example.BTL_Mobile.dto.response.HomeResponse;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.Topic;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.TopicRepository;
import com.example.BTL_Mobile.repository.UserLessonResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int DEFAULT_COURSE_PAGE_SIZE = 10;
    private static final int MOCK_COURSE_TOTAL_PAGES = 4;

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;
    private final UserLessonResultRepository userLessonResultRepository;

    public HomeResponse getHomeData(User user) {
        String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName()
                : user.getUsername();

        // Lấy lessons gần nhất mà user đã học
        List<Lesson> recentLessons = userLessonResultRepository.findRecentLessonsByUser(user.getId());
        List<CurrentLessonResponse> currentLessons = recentLessons.stream()
                .limit(3)
                .map(lesson -> {
                    // Tính progress từ UserLessonResult (nếu có)
                    int progressPercent = userLessonResultRepository
                            .findByUserAndLesson(user, lesson)
                            .map(result -> (int) Math.round(result.getScore() * 10)) // score 0-10 -> percent
                            .orElse(0);
                    
                    // Level tạm thời tính từ id % 20 + 1 (vì Lesson không có level field)
                    // Có thể thêm field level vào Lesson entity sau nếu cần
                    String level = String.valueOf((lesson.getId() % 20) + 1);
                    
                    // lessonNumber: dùng id hoặc có thể tính từ order trong topic
                    int lessonNumber = lesson.getId();
                    
                    return CurrentLessonResponse.builder()
                            .id(lesson.getId())
                            .lessonNumber(lessonNumber)
                            .title(lesson.getTitle())
                            .level(level)
                            .progressPercent(progressPercent)
                            .build();
                })
                .collect(Collectors.toList());

        // Nếu user chưa học lesson nào, fallback mock để UI sinh động
        if (currentLessons.isEmpty()) {
            currentLessons = Arrays.asList(
                    CurrentLessonResponse.builder()
                            .id(1)
                            .lessonNumber(1)
                            .title("Welcome to school")
                            .level("15")
                            .progressPercent(30)
                            .build(),
                    CurrentLessonResponse.builder()
                            .id(3)
                            .lessonNumber(3)
                            .title("Summer festival")
                            .level("5")
                            .progressPercent(50)
                            .build(),
                    CurrentLessonResponse.builder()
                            .id(5)
                            .lessonNumber(5)
                            .title("Travelling")
                            .level("8")
                            .progressPercent(20)
                            .build()
            );
        }

        // Lấy page đầu tiên của Topics để hiển thị list khoá học ban đầu
        List<CourseProgressResponse> courses = getCoursesPage(user, 0, DEFAULT_COURSE_PAGE_SIZE);

        return HomeResponse.builder()
                .fullName(displayName)
                .currentLessons(currentLessons)
                .courses(courses)
                .build();
    }

    /**
     * Lấy danh sách khoá học (topics) theo dạng phân trang cho phần list phía dưới.
     */
    public List<CourseProgressResponse> getCoursesPage(User user, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Topic> topicPage = topicRepository.findAll(pageable);

        List<CourseProgressResponse> courses = topicPage.stream()
                .map(topic -> {
                    Long totalLessons = lessonRepository.countByTopicId(topic.getId());
                    Long completedLessons = userLessonResultRepository
                            .countCompletedLessonsByUserAndTopic(user.getId(), topic.getId());

                    int progressPercent = totalLessons > 0
                            ? (int) Math.round((completedLessons.doubleValue() / totalLessons) * 100)
                            : 0;

                    return CourseProgressResponse.builder()
                            .id(topic.getId())
                            .title(topic.getName())
                            .imageUrl("") // Có thể thêm imageUrl vào Topic entity sau
                            .progressPercent(progressPercent)
                            .build();
                })
                .collect(Collectors.toList());

        // Bơm thêm mock để có nhiều page test "load more":
        // - Nếu DB ít topic (ví dụ chỉ 5) thì page 0 vẫn trả DB, page 1+ sẽ trả mock
        // - Luôn đảm bảo trả tối đa MOCK_COURSE_TOTAL_PAGES page mock (ngoài DB)
        if (safePage >= MOCK_COURSE_TOTAL_PAGES) {
            // Ngoài số page mock cho phép thì dừng
            return courses;
        }

        if (courses.size() >= safeSize) {
            return courses;
        }

        // Fill phần còn thiếu bằng mock để đủ size cho page hiện tại
        int baseIndex = safePage * safeSize; // 0-based "global index" của page
        List<CourseProgressResponse> filled = new ArrayList<>(courses);

        for (int i = courses.size(); i < safeSize; i++) {
            int global = baseIndex + i + 1; // 1-based label
            int mockId = 100_000 + global;  // tránh trùng id với DB
            int progress = switch (global % 5) {
                case 0 -> 10;
                case 1 -> 30;
                case 2 -> 50;
                case 3 -> 65;
                default -> 80;
            };
            String title = switch (global % 5) {
                case 0 -> "At the airport " + global;
                case 1 -> "Travelling " + global;
                case 2 -> "Summer festival " + global;
                case 3 -> "Ordering food " + global;
                default -> "Welcome to school " + global;
            };

            filled.add(CourseProgressResponse.builder()
                    .id(mockId)
                    .title(title)
                    .imageUrl("")
                    .progressPercent(progress)
                    .build());
        }

        return filled;
    }
}
