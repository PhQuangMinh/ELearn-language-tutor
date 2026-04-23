package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminFlashCardCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminWordCreateRequest;
import com.example.BTL_Mobile.dto.response.AdminImportResult;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportCommitResponse;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportPreviewAnswer;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportPreviewItem;
import com.example.BTL_Mobile.dto.response.AdminQuestionImportPreviewResponse;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.Answer;
import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.Media;
import com.example.BTL_Mobile.model.Question;
import com.example.BTL_Mobile.model.enums.EMediaType;
import com.example.BTL_Mobile.model.enums.EQuestionType;
import com.example.BTL_Mobile.model.enums.EWordType;
import com.example.BTL_Mobile.repository.AnswerRepository;
import com.example.BTL_Mobile.repository.LessonRepository;
import com.example.BTL_Mobile.repository.MediaRepository;
import com.example.BTL_Mobile.repository.QuestionRepository;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminExcelImportService {

    private static final int MAX_ERROR_LINES = 100;

    private final AdminLessonVocabularyService adminLessonVocabularyService;
    private final AdminLessonFlashCardService adminLessonFlashCardService;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final MediaRepository mediaRepository;
    private final PlatformTransactionManager transactionManager;

    public AdminImportResult importWords(Integer topicId, MultipartFile file) {
        requireTopicId(topicId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File import trống", "IMPORT_FILE_EMPTY");
        }
        List<String> errorLines = new ArrayList<>();
        int success = 0;
        int errors = 0;

        DataFormatter formatter = new DataFormatter();
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException("File Excel không có sheet", "IMPORT_INVALID");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> col = readHeader(headerRow, formatter);
            int cWord = requireColumn(col, "word");
            int cPron = requireColumn(col, "pronunciation");
            int cMean = requireColumn(col, "meaning");
            int cType = requireColumn(col, "type");

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }
                int excelRow = r + 1;
                try {
                    String word = requireText(cellString(row, cWord, formatter), "word");
                    String pronunciation = requireText(cellString(row, cPron, formatter), "pronunciation");
                    String meaning = requireText(cellString(row, cMean, formatter), "meaning");
                    EWordType type = parseWordType(cellString(row, cType, formatter));

                    AdminWordCreateRequest req = new AdminWordCreateRequest();
                    req.setWord(word);
                    req.setPronunciation(pronunciation);
                    req.setMeaning(meaning);
                    req.setType(type);

                    adminLessonVocabularyService.addWord(topicId, req);
                    success++;
                } catch (Exception e) {
                    errors++;
                    appendErrorLine(errorLines, excelRow, e);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Không đọc được file Excel: " + e.getMessage(), "IMPORT_PARSE_ERROR");
        }

        return AdminImportResult.builder()
                .successCount(success)
                .errorCount(errors)
                .errors(errorLines)
                .build();
    }

    public AdminImportResult importFlashCards(Integer topicId, MultipartFile file) {
        requireTopicId(topicId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File import trống", "IMPORT_FILE_EMPTY");
        }
        List<String> errorLines = new ArrayList<>();
        int success = 0;
        int errors = 0;

        DataFormatter formatter = new DataFormatter();
        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException("File Excel không có sheet", "IMPORT_INVALID");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> col = readHeader(headerRow, formatter);
            int cWord = requireColumn(col, "word");
            int cPron = requireColumn(col, "pronunciation");
            int cMean = requireColumn(col, "meaning");
            int cType = requireColumn(col, "type");
            int cExample = requireColumn(col, "example");

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }
                int excelRow = r + 1;
                try {
                    String word = requireText(cellString(row, cWord, formatter), "word");
                    String pronunciation = requireText(cellString(row, cPron, formatter), "pronunciation");
                    String meaning = requireText(cellString(row, cMean, formatter), "meaning");
                    EWordType type = parseWordType(cellString(row, cType, formatter));
                    String example = requireText(cellString(row, cExample, formatter), "example");

                    AdminFlashCardCreateRequest req = new AdminFlashCardCreateRequest();
                    req.setDictionaryWordId(null);
                    req.setWord(word);
                    req.setPronunciation(pronunciation);
                    req.setMeaning(meaning);
                    req.setType(type);
                    req.setExample(example);
                    req.setImageUrl(AdminLessonFlashCardService.FLASHCARD_PLACEHOLDER_IMAGE_URL);
                    req.setImageName("import_placeholder");
                    req.setImageSize(0);

                    adminLessonFlashCardService.addFlashCard(topicId, req);
                    success++;
                } catch (Exception e) {
                    errors++;
                    appendErrorLine(errorLines, excelRow, e);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Không đọc được file Excel: " + e.getMessage(), "IMPORT_PARSE_ERROR");
        }

        return AdminImportResult.builder()
                .successCount(success)
                .errorCount(errors)
                .errors(errorLines)
                .build();
    }

    public AdminQuestionImportPreviewResponse previewQuestions(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File import trống", "IMPORT_FILE_EMPTY");
        }

        List<String> errorLines = new ArrayList<>();
        List<AdminQuestionImportPreviewItem> previewItems = new ArrayList<>();
        Set<Integer> skippedLessonIds = new LinkedHashSet<>();
        int skippedRows = 0;

        Map<Integer, Boolean> lessonExistsCache = new HashMap<>();
        Map<Integer, Boolean> lessonHasQuestionCache = new HashMap<>();
        DataFormatter formatter = new DataFormatter();

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new BusinessException("File Excel không có sheet", "IMPORT_INVALID");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> col = readHeader(headerRow, formatter);

            int cLessonId = requireColumn(col, "lessonId", "lesson_id");
            int cContent = requireColumn(col, "content");
            int cType = requireColumn(col, "type");
            int cRepeatable = requireColumn(col, "repeatable");
            int cMediaUrl = optionalColumn(col, "mediaUrl", "media_url");

            int[] cAnswerContent = new int[4];
            int[] cAnswerCorrect = new int[4];
            for (int i = 0; i < 4; i++) {
                int idx = i + 1;
                cAnswerContent[i] = optionalColumn(col, "answer" + idx + "Content", "answer" + idx + "_content");
                cAnswerCorrect[i] = optionalColumn(col, "answer" + idx + "Correct", "answer" + idx + "_correct");
            }

            int lastRow = sheet.getLastRowNum();
            for (int r = 1; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row, formatter)) {
                    continue;
                }
                int excelRow = r + 1;
                try {
                    int lessonId = parsePositiveInt(
                            requireText(cellString(row, cLessonId, formatter), "lessonId"),
                            "lessonId"
                    );

                    boolean lessonExists = lessonExistsCache.computeIfAbsent(lessonId, lessonRepository::existsById);
                    if (!lessonExists) {
                        throw new IllegalArgumentException("lessonId không tồn tại");
                    }

                    boolean hasQuestion = lessonHasQuestionCache.computeIfAbsent(
                            lessonId,
                            questionRepository::existsByLesson_Id
                    );
                    if (hasQuestion) {
                        skippedRows++;
                        skippedLessonIds.add(lessonId);
                        continue;
                    }

                    String content = requireText(cellString(row, cContent, formatter), "content");
                    EQuestionType type = parseQuestionType(cellString(row, cType, formatter));
                    boolean repeatable = parseBooleanStrict(cellString(row, cRepeatable, formatter), "repeatable");
                    validateRepeatable(type, repeatable);

                    List<AdminQuestionImportPreviewAnswer> answers = parsePreviewAnswers(
                            row,
                            formatter,
                            cAnswerContent,
                            cAnswerCorrect,
                            type
                    );

                    previewItems.add(AdminQuestionImportPreviewItem.builder()
                            .sourceRow(excelRow)
                            .lessonId(lessonId)
                            .content(content)
                            .type(type)
                            .repeatable(repeatable)
                            .mediaUrl(trimToNull(cMediaUrl >= 0 ? cellString(row, cMediaUrl, formatter) : null))
                            .answers(answers)
                            .build());
                } catch (Exception e) {
                    appendErrorLine(errorLines, excelRow, e);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Không đọc được file Excel: " + e.getMessage(), "IMPORT_PARSE_ERROR");
        }

        return AdminQuestionImportPreviewResponse.builder()
                .validCount(previewItems.size())
                .skippedCount(skippedRows)
                .errorCount(errorLines.size())
                .skippedLessonIds(new ArrayList<>(skippedLessonIds))
                .questions(previewItems)
                .errors(errorLines)
                .build();
    }

    public AdminQuestionImportCommitResponse commitQuestions(List<AdminQuestionImportPreviewItem> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException("Danh sách question cần lưu đang trống", "IMPORT_EMPTY_PAYLOAD");
        }

        List<String> errorLines = new ArrayList<>();
        Set<Integer> skippedLessonIds = new LinkedHashSet<>();
        int imported = 0;
        int skipped = 0;

        Map<Integer, Boolean> lessonExistsCache = new HashMap<>();
        Map<Integer, Boolean> lessonHasQuestionCache = new HashMap<>();

        for (int i = 0; i < requests.size(); i++) {
            AdminQuestionImportPreviewItem request = requests.get(i);
            int rowNo = request.getSourceRow() == null ? (i + 1) : request.getSourceRow();
            try {
                int lessonId = parsePositiveInt(String.valueOf(request.getLessonId()), "lessonId");

                boolean lessonExists = lessonExistsCache.computeIfAbsent(lessonId, lessonRepository::existsById);
                if (!lessonExists) {
                    throw new BusinessException("lessonId không tồn tại", "LESSON_NOT_FOUND");
                }

                boolean hasQuestion = lessonHasQuestionCache.computeIfAbsent(
                        lessonId,
                        questionRepository::existsByLesson_Id
                );
                if (hasQuestion) {
                    skipped++;
                    skippedLessonIds.add(lessonId);
                    continue;
                }

                EQuestionType type = request.getType();
                if (type == null) {
                    throw new IllegalArgumentException("type trống");
                }
                String content = requireText(request.getContent(), "content");
                validateRepeatable(type, request.isRepeatable());
                List<AdminQuestionImportPreviewAnswer> answers = validateCommitAnswers(request.getAnswers(), type);
                String mediaUrl = trimToNull(request.getMediaUrl());
                persistQuestionRow(lessonId, content, type, request.isRepeatable(), mediaUrl, answers);
                imported++;
            } catch (Exception e) {
                appendErrorLine(errorLines, rowNo, e);
            }
        }

        return AdminQuestionImportCommitResponse.builder()
                .importedCount(imported)
                .skippedCount(skipped)
                .errorCount(errorLines.size())
                .skippedLessonIds(new ArrayList<>(skippedLessonIds))
                .errors(errorLines)
                .build();
    }

    private void persistQuestionRow(
            Integer lessonId,
            String content,
            EQuestionType type,
            boolean repeatable,
            String mediaUrl,
            List<AdminQuestionImportPreviewAnswer> answers
    ) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult((status) -> {
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new BusinessException("lessonId không tồn tại", "LESSON_NOT_FOUND"));

            Question question = new Question();
            question.setLesson(lesson);
            question.setContent(content);
            question.setType(type);
            question.setRepeatable(repeatable);

            if (mediaUrl != null) {
                Media media = new Media();
                media.setType(resolveQuestionMediaType(type));
                media.setUrl(mediaUrl);
                media.setName("question_import_media");
                media.setSize(0);
                question.setMedia(mediaRepository.save(media));
            }

            Question savedQuestion = questionRepository.save(question);
            if (!answers.isEmpty()) {
                List<Answer> entities = answers.stream().map((a) -> {
                    Answer answer = new Answer();
                    answer.setQuestion(savedQuestion);
                    answer.setContent(requireText(a.getContent(), "answer content"));
                    answer.setCorrect(a.isCorrect());
                    return answer;
                }).toList();
                answerRepository.saveAll(entities);
            }
        });
    }

    private static void requireTopicId(Integer topicId) {
        if (topicId == null || topicId <= 0) {
            throw new BusinessException("Thiếu topic hợp lệ (topicId)", "IMPORT_TOPIC_REQUIRED");
        }
    }

    private static void appendErrorLine(List<String> errorLines, int excelRow, Exception e) {
        if (errorLines.size() >= MAX_ERROR_LINES) {
            return;
        }
        String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        errorLines.add("Dòng " + excelRow + ": " + msg);
    }

    private static Map<String, Integer> readHeader(Row row, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (row == null) {
            return map;
        }
        short last = row.getLastCellNum();
        for (int i = 0; i < last; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }
            String raw = formatter.formatCellValue(cell);
            String key = normalizeHeader(raw);
            if (!key.isEmpty()) {
                map.putIfAbsent(key, i);
            }
        }
        return map;
    }

    private static String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        return header.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static int requireColumn(Map<String, Integer> col, String... aliases) {
        for (String alias : aliases) {
            String key = normalizeHeader(alias);
            if (col.containsKey(key)) {
                return col.get(key);
            }
        }
        throw new BusinessException(
                "Thiếu cột bắt buộc trong header: " + String.join(" / ", aliases),
                "IMPORT_MISSING_COLUMN"
        );
    }

    private static int optionalColumn(Map<String, Integer> col, String... aliases) {
        for (String alias : aliases) {
            String key = normalizeHeader(alias);
            if (col.containsKey(key)) {
                return col.get(key);
            }
        }
        return -1;
    }

    private static boolean isRowEmpty(Row row, DataFormatter formatter) {
        short last = row.getLastCellNum();
        if (last <= 0) {
            return true;
        }
        for (int i = 0; i < last; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                continue;
            }
            String v = formatter.formatCellValue(cell);
            if (v != null && !v.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String cellString(Row row, int colIndex, DataFormatter formatter) {
        if (row == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        return formatter.formatCellValue(cell);
    }

    private static String requireText(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(field + " trống");
        }
        return raw.trim();
    }

    private static String trimToNull(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        return s.isEmpty() ? null : s;
    }

    private static int parsePositiveInt(String raw, String field) {
        try {
            int value = Integer.parseInt(raw.trim());
            if (value <= 0) {
                throw new NumberFormatException("<= 0");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " phải là số nguyên dương");
        }
    }

    private static boolean parseBooleanStrict(String raw, String field) {
        String value = requireText(raw, field).toLowerCase(Locale.ROOT);
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        throw new IllegalArgumentException(field + " phải là true/false");
    }

    private static EWordType parseWordType(String raw) {
        String s = requireText(raw, "type");
        try {
            return EWordType.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("type phải là NOUN, VERB hoặc ADJECTIVE");
        }
    }

    private static EQuestionType parseQuestionType(String raw) {
        String s = requireText(raw, "type");
        try {
            return EQuestionType.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "type không hợp lệ (ONE_SELECTION, LISTEN_AND_ARRANGE_SENTENCE, "
                            + "TRANSLATE_AND_ARRANGE_SENTENCE, SPEAKING_ASSESSMENT)"
            );
        }
    }

    private static void validateRepeatable(EQuestionType type, boolean repeatable) {
        if (type == EQuestionType.SPEAKING_ASSESSMENT && !repeatable) {
            throw new IllegalArgumentException("SPEAKING_ASSESSMENT bắt buộc repeatable=true");
        }
        if (type != EQuestionType.SPEAKING_ASSESSMENT && repeatable) {
            throw new IllegalArgumentException("Chỉ SPEAKING_ASSESSMENT mới được repeatable=true");
        }
    }

    private static List<AdminQuestionImportPreviewAnswer> parsePreviewAnswers(
            Row row,
            DataFormatter formatter,
            int[] cAnswerContent,
            int[] cAnswerCorrect,
            EQuestionType type
    ) {
        List<AdminQuestionImportPreviewAnswer> answers = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            String content = trimToNull(cAnswerContent[i] >= 0 ? cellString(row, cAnswerContent[i], formatter) : null);
            String rawCorrect = trimToNull(cAnswerCorrect[i] >= 0 ? cellString(row, cAnswerCorrect[i], formatter) : null);

            if (content == null && rawCorrect == null) {
                continue;
            }
            if (content == null) {
                throw new IllegalArgumentException("answer" + (i + 1) + "Content trống");
            }
            if (rawCorrect == null) {
                throw new IllegalArgumentException("answer" + (i + 1) + "Correct trống");
            }

            boolean correct = parseBooleanStrict(rawCorrect, "answer" + (i + 1) + "Correct");
            answers.add(AdminQuestionImportPreviewAnswer.builder()
                    .content(content)
                    .correct(correct)
                    .build());
        }

        if (type == EQuestionType.ONE_SELECTION) {
            if (answers.size() < 2) {
                throw new IllegalArgumentException("ONE_SELECTION cần tối thiểu 2 đáp án");
            }
            long correctCount = answers.stream().filter(AdminQuestionImportPreviewAnswer::isCorrect).count();
            if (correctCount != 1) {
                throw new IllegalArgumentException("ONE_SELECTION cần đúng 1 đáp án isCorrect=true");
            }
            return answers;
        }

        if (type == EQuestionType.LISTEN_AND_ARRANGE_SENTENCE
                || type == EQuestionType.TRANSLATE_AND_ARRANGE_SENTENCE) {
            if (answers.size() != 1) {
                throw new IllegalArgumentException(type.name() + " cần đúng 1 đáp án");
            }
            if (!answers.get(0).isCorrect()) {
                throw new IllegalArgumentException(type.name() + " yêu cầu đáp án duy nhất phải isCorrect=true");
            }
            return answers;
        }

        if (!answers.isEmpty()) {
            throw new IllegalArgumentException("SPEAKING_ASSESSMENT không được có đáp án");
        }
        return answers;
    }

    private static List<AdminQuestionImportPreviewAnswer> validateCommitAnswers(
            List<AdminQuestionImportPreviewAnswer> rawAnswers,
            EQuestionType type
    ) {
        List<AdminQuestionImportPreviewAnswer> answers = rawAnswers == null ? new ArrayList<>() : rawAnswers.stream()
                .map((a) -> {
                    if (a == null) {
                        throw new IllegalArgumentException("Đáp án không hợp lệ");
                    }
                    return AdminQuestionImportPreviewAnswer.builder()
                            .content(requireText(a.getContent(), "answer content"))
                            .correct(a.isCorrect())
                            .build();
                })
                .toList();

        if (type == EQuestionType.ONE_SELECTION) {
            if (answers.size() < 2) {
                throw new IllegalArgumentException("ONE_SELECTION cần tối thiểu 2 đáp án");
            }
            long correctCount = answers.stream().filter(AdminQuestionImportPreviewAnswer::isCorrect).count();
            if (correctCount != 1) {
                throw new IllegalArgumentException("ONE_SELECTION cần đúng 1 đáp án isCorrect=true");
            }
            return answers;
        }

        if (type == EQuestionType.LISTEN_AND_ARRANGE_SENTENCE
                || type == EQuestionType.TRANSLATE_AND_ARRANGE_SENTENCE) {
            if (answers.size() != 1) {
                throw new IllegalArgumentException(type.name() + " cần đúng 1 đáp án");
            }
            if (!answers.get(0).isCorrect()) {
                throw new IllegalArgumentException(type.name() + " yêu cầu đáp án duy nhất phải isCorrect=true");
            }
            return answers;
        }

        if (!answers.isEmpty()) {
            throw new IllegalArgumentException("SPEAKING_ASSESSMENT không được có đáp án");
        }
        return answers;
    }

    private static EMediaType resolveQuestionMediaType(EQuestionType type) {
        if (type == EQuestionType.LISTEN_AND_ARRANGE_SENTENCE) {
            return EMediaType.AUDIO;
        }
        return EMediaType.IMAGE;
    }
}
