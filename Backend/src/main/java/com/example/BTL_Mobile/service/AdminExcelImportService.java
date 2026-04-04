package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AdminFlashCardCreateRequest;
import com.example.BTL_Mobile.dto.request.AdminWordCreateRequest;
import com.example.BTL_Mobile.dto.response.AdminImportResult;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.model.enums.EWordType;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminExcelImportService {

    /** Flashcard import không có cột ảnh: backend gắn URL placeholder hợp lệ để thỏa DB. */
    public static final String FLASHCARD_IMPORT_PLACEHOLDER_IMAGE =
            "https://placehold.co/600x400/e2e8f0/64748b/png?text=Flashcard";

    private static final int MAX_ERROR_LINES = 100;

    private final AdminLessonVocabularyService adminLessonVocabularyService;
    private final AdminLessonFlashCardService adminLessonFlashCardService;

    public AdminImportResult importWords(Integer lessonId, MultipartFile file) {
        requireLessonId(lessonId);
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

                    adminLessonVocabularyService.addWord(lessonId, req);
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

    public AdminImportResult importFlashCards(Integer lessonId, MultipartFile file) {
        requireLessonId(lessonId);
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
                    req.setImageUrl(FLASHCARD_IMPORT_PLACEHOLDER_IMAGE);
                    req.setImageName("import_placeholder");
                    req.setImageSize(0);

                    adminLessonFlashCardService.addFlashCard(lessonId, req);
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

    private static void requireLessonId(Integer lessonId) {
        if (lessonId == null || lessonId <= 0) {
            throw new BusinessException("Thiếu lesson hợp lệ (lessonId)", "IMPORT_LESSON_REQUIRED");
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

    private static EWordType parseWordType(String raw) {
        String s = requireText(raw, "type");
        try {
            return EWordType.valueOf(s.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("type phải là NOUN, VERB hoặc ADJECTIVE");
        }
    }
}
