package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.CourseProgressResponse;
import com.example.BTL_Mobile.dto.response.HomeResponse;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int DEFAULT_COURSE_PAGE_SIZE = 10;
    // Số page mock dùng để test trước đây – hiện tại đã tắt mock,
    // API chỉ trả dữ liệu thật từ database.
    // private static final int MOCK_COURSE_TOTAL_PAGES = 4;

    private final LessonRepository lessonRepository;
    private final TopicRepository topicRepository;
    private final UserLessonResultRepository userLessonResultRepository;

    public HomeResponse getHomeData(User user) {
        String displayName = (user.getFullName() != null && !user.getFullName().isBlank())
                ? user.getFullName()
                : user.getUsername();

        // Lấy page đầu tiên của Topics để hiển thị list khoá học ban đầu
        List<CourseProgressResponse> courses = getCoursesPage(user, 0, DEFAULT_COURSE_PAGE_SIZE);

        return HomeResponse.builder()
                .fullName(displayName)
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

        // Trả đúng danh sách topic từ database, không bơm thêm mock.
        return courses;
    }
}
