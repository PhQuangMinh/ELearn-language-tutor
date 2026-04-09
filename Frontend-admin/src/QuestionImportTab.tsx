import { useEffect, useMemo, useState } from "react";
import * as XLSX from "xlsx";
import {
  commitQuestionsImport,
  listAllLessons,
  listAllTopics,
  listQuestionsByLesson,
  previewQuestionsExcel,
} from "./api";
import type {
  Lesson,
  QuestionDetail,
  QuestionImportPreviewItem,
  QuestionImportPreviewResponse,
  Topic,
} from "./types";

type Props = {
  apiBase: string;
  token: string;
  defaultLessonId?: number | null;
  onMessage: (message: string) => void;
};

const isPositiveInt = (value: unknown) => Number.isInteger(value) && Number(value) > 0;

const QUESTION_TEMPLATE_ROWS = [
  [
    "lessonId",
    "content",
    "type",
    "repeatable",
    "mediaUrl",
    "answer1Content",
    "answer1Correct",
    "answer2Content",
    "answer2Correct",
    "answer3Content",
    "answer3Correct",
    "answer4Content",
    "answer4Correct",
  ],
  [
    "101",
    "What does 'apple' mean in Vietnamese?",
    "ONE_SELECTION",
    "false",
    "",
    "Táo",
    "true",
    "Cam",
    "false",
    "Chuối",
    "false",
    "Nho",
    "false",
  ],
  [
    "101",
    "Listen and arrange: I am going to school",
    "LISTEN_AND_ARRANGE_SENTENCE",
    "false",
    "https://example.com/audio/listen-arrange-1.mp3",
    "I am going to school",
    "true",
    "",
    "",
    "",
    "",
    "",
    "",
  ],
  [
    "102",
    "Translate and arrange: Tôi đang học tiếng Anh",
    "TRANSLATE_AND_ARRANGE_SENTENCE",
    "false",
    "",
    "I am learning English",
    "true",
    "",
    "",
    "",
    "",
    "",
    "",
  ],
  [
    "102",
    "Please read this sentence aloud: Learning English is fun",
    "SPEAKING_ASSESSMENT",
    "true",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
    "",
  ],
];

const toUiError = (error: unknown) => (error instanceof Error ? error.message : "Lỗi không xác định");
const QUESTION_TYPES = [
  "ONE_SELECTION",
  "LISTEN_AND_ARRANGE_SENTENCE",
  "TRANSLATE_AND_ARRANGE_SENTENCE",
  "SPEAKING_ASSESSMENT",
] as const;

export function QuestionImportTab({ apiBase, token, defaultLessonId, onMessage }: Props) {
  const [loading, setLoading] = useState(false);
  const [questionLoading, setQuestionLoading] = useState(false);
  const [committing, setCommitting] = useState(false);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [topicId, setTopicId] = useState<number | "">("");
  const [lessonId, setLessonId] = useState<number | "">("");
  const [questions, setQuestions] = useState<QuestionDetail[]>([]);
  const [importPreview, setImportPreview] = useState<QuestionImportPreviewResponse | null>(null);
  const [editableQuestions, setEditableQuestions] = useState<QuestionImportPreviewItem[]>([]);
  const [fileName, setFileName] = useState("");
  const [commitNotice, setCommitNotice] = useState("");

  useEffect(() => {
    if (!isPositiveInt(defaultLessonId) || lessonId !== "") {
      return;
    }
    const targetLessonId = Number(defaultLessonId);
    setLessonId(targetLessonId);
    const targetLesson = lessons.find((item) => item.id === targetLessonId);
    if (targetLesson?.topicId && isPositiveInt(targetLesson.topicId)) {
      setTopicId(targetLesson.topicId);
    }
  }, [defaultLessonId, lessonId, lessons]);

  useEffect(() => {
    if (!token) return;
    setLoading(true);
    Promise.all([listAllTopics({ apiBase, token }), listAllLessons({ apiBase, token })])
      .then(([topicItems, lessonItems]) => {
        setTopics(topicItems);
        setLessons(lessonItems);
      })
      .catch((error) => onMessage(`Không tải được danh sách lesson: ${toUiError(error)}`))
      .finally(() => setLoading(false));
  }, [apiBase, token, onMessage]);

  useEffect(() => {
    if (!isPositiveInt(lessonId)) {
      setQuestions([]);
      return;
    }
    setQuestionLoading(true);
    listQuestionsByLesson({ apiBase, token }, Number(lessonId))
      .then((items) => setQuestions(items))
      .catch((error) => {
        setQuestions([]);
        onMessage(`Không tải được câu hỏi theo lesson: ${toUiError(error)}`);
      })
      .finally(() => setQuestionLoading(false));
  }, [apiBase, token, lessonId, onMessage]);

  const filteredLessons = useMemo(() => {
    if (!isPositiveInt(topicId)) {
      return [];
    }
    return lessons.filter((lesson) => lesson.topicId === Number(topicId));
  }, [lessons, topicId]);

  const topicSelectValue = useMemo(() => (topicId === "" ? "" : String(topicId)), [topicId]);
  const lessonSelectValue = useMemo(() => (lessonId === "" ? "" : String(lessonId)), [lessonId]);
  const lessonLabelById = useMemo(() => {
    const map = new Map<number, string>();
    lessons.forEach((lesson) => {
      map.set(lesson.id, `#${lesson.id} · Topic ${lesson.topicId} · ${lesson.title}`);
    });
    return map;
  }, [lessons]);

  const downloadTemplate = () => {
    const workbook = XLSX.utils.book_new();
    const worksheet = XLSX.utils.aoa_to_sheet(QUESTION_TEMPLATE_ROWS);
    XLSX.utils.book_append_sheet(workbook, worksheet, "QuestionsTemplate");
    XLSX.writeFile(workbook, "questions-import-template.xlsx");
  };

  const onPickFile = async (file: File) => {
    setLoading(true);
    setCommitNotice("");
    setFileName(file.name);
    try {
      const preview = await previewQuestionsExcel({ apiBase, token }, file);
      setImportPreview(preview);
      setEditableQuestions(preview.questions.map((item) => ({ ...item, answers: [...item.answers] })));
      const hint =
        preview.errors?.length > 0 ? ` Ví dụ lỗi: ${preview.errors.slice(0, 3).join(" | ")}` : "";
      onMessage(
        `Preview question: ${preview.validCount} hợp lệ, ${preview.skippedCount} bị skip, ${preview.errorCount} lỗi.${hint}`
      );
    } catch (error) {
      onMessage(`Preview import question thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const updateEditableQuestion = (
    index: number,
    updater: (current: QuestionImportPreviewItem) => QuestionImportPreviewItem
  ) => {
    setEditableQuestions((prev) =>
      prev.map((item, itemIndex) => (itemIndex === index ? updater(item) : item))
    );
  };

  const addAnswer = (index: number) => {
    updateEditableQuestion(index, (current) => ({
      ...current,
      answers: [...current.answers, { content: "", correct: false }],
    }));
  };

  const removeAnswer = (index: number, answerIndex: number) => {
    updateEditableQuestion(index, (current) => ({
      ...current,
      answers: current.answers.filter((_, idx) => idx !== answerIndex),
    }));
  };

  const onCommitPreviewQuestions = async () => {
    if (editableQuestions.length === 0) {
      onMessage("Không có question để lưu");
      return;
    }
    setCommitting(true);
    setCommitNotice("");
    try {
      const result = await commitQuestionsImport({ apiBase, token }, editableQuestions);
      const hint = result.errors.length > 0 ? ` Ví dụ lỗi: ${result.errors.slice(0, 3).join(" | ")}` : "";
      onMessage(
        `Lưu question: ${result.importedCount} thành công, ${result.skippedCount} bị skip, ${result.errorCount} lỗi.${hint}`
      );
      if (result.importedCount > 0) {
        setCommitNotice(
          `Commit thành công ${result.importedCount} question. Skip ${result.skippedCount}, lỗi ${result.errorCount}.`
        );
      }
      if (result.importedCount > 0 && result.errorCount === 0) {
        setImportPreview(null);
        setEditableQuestions([]);
      }
      if (isPositiveInt(lessonId)) {
        setQuestionLoading(true);
        listQuestionsByLesson({ apiBase, token }, Number(lessonId))
          .then((items) => setQuestions(items))
          .catch((error) => onMessage(`Không tải được câu hỏi theo lesson: ${toUiError(error)}`))
          .finally(() => setQuestionLoading(false));
      }
    } catch (error) {
      onMessage(`Lưu danh sách question thất bại: ${toUiError(error)}`);
    } finally {
      setCommitting(false);
    }
  };

  return (
    <div className="panel-grid">
      <section className="panel">
        <h3>Import Question theo Topic / Lesson</h3>
        <div className="list-toolbar" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn topic
            </span>
            <select
              value={topicSelectValue}
              onChange={(e) => {
                const nextTopicId = e.target.value ? Number(e.target.value) : "";
                setTopicId(nextTopicId);
                setLessonId("");
                setQuestions([]);
              }}
            >
              <option value="">-- Chọn topic --</option>
              {topics.map((topic) => (
                <option key={topic.id} value={topic.id}>
                  #{topic.id} · {topic.name}
                </option>
              ))}
            </select>
          </label>

          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn lesson đích
            </span>
            <select
              value={lessonSelectValue}
              onChange={(e) => setLessonId(e.target.value ? Number(e.target.value) : "")}
              disabled={!isPositiveInt(topicId)}
            >
              <option value="">-- Chọn lesson --</option>
              {filteredLessons.map((lesson) => (
                <option key={lesson.id} value={lesson.id}>
                  #{lesson.id} · Topic {lesson.topicId} · {lesson.title}
                </option>
              ))}
            </select>
          </label>

          <span className="note" style={{ fontWeight: 600 }}>
            Upload Excel (.xlsx / .xls) để preview danh sách question import
          </span>
          <span className="note">
            Cột gợi ý: lessonId · content · type · repeatable · mediaUrl · answer1Content..answer4Correct
          </span>
          <span className="note">
            Template gồm 4 dòng mẫu: ONE_SELECTION, LISTEN_AND_ARRANGE_SENTENCE,
            TRANSLATE_AND_ARRANGE_SENTENCE, SPEAKING_ASSESSMENT.
          </span>

          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={loading}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                void onPickFile(file);
              }
              e.target.value = "";
            }}
          />

          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            <button type="button" onClick={downloadTemplate}>
              Template Excel
            </button>
            <button
              type="button"
              disabled={loading}
              onClick={() => {
                setLoading(true);
                Promise.all([listAllTopics({ apiBase, token }), listAllLessons({ apiBase, token })])
                  .then(([topicItems, lessonItems]) => {
                    setTopics(topicItems);
                    setLessons(lessonItems);
                    onMessage("Đã tải lại danh sách topic và lesson");
                  })
                  .catch((error) => onMessage(`Không tải được topic/lesson: ${toUiError(error)}`))
                  .finally(() => setLoading(false));
              }}
            >
              Tải lại topic + lesson
            </button>
          </div>
        </div>
      </section>

      <section className="panel">
        <h3>Danh sách câu hỏi / Preview file</h3>
        {commitNotice && (
          <p className="note" style={{ marginBottom: "10px", color: "#10b981", fontWeight: 600 }}>
            {commitNotice}
          </p>
        )}
        <p className="note" style={{ marginBottom: "8px" }}>
          Câu hỏi trong lesson đã chọn: <strong>{questions.length}</strong>
        </p>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Content</th>
              <th>Type</th>
              <th>Repeatable</th>
              <th>Số đáp án</th>
            </tr>
          </thead>
          <tbody>
            {!isPositiveInt(lessonId) ? (
              <tr>
                <td colSpan={5}>Chọn lesson để xem danh sách câu hỏi</td>
              </tr>
            ) : questionLoading ? (
              <tr>
                <td colSpan={5}>Đang tải danh sách câu hỏi...</td>
              </tr>
            ) : questions.length === 0 ? (
              <tr>
                <td colSpan={5}>Lesson này hiện chưa có câu hỏi</td>
              </tr>
            ) : (
              questions.map((question) => (
                <tr key={question.id}>
                  <td>{question.id}</td>
                  <td>{question.content}</td>
                  <td>{question.type}</td>
                  <td>{question.repeatable ? "Có" : "Không"}</td>
                  <td>{question.answers?.length || 0}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        <h3 style={{ marginTop: "18px" }}>Questions sẽ import (editable)</h3>
        <p className="note" style={{ marginBottom: "8px" }}>
          File đang preview: <strong>{fileName || "-"}</strong>
        </p>
        {!importPreview ? (
          <p className="note">Upload file để nhận preview từ backend.</p>
        ) : (
          <div style={{ display: "grid", gap: "10px" }}>
            <p className="note">
              Hợp lệ: <strong>{importPreview.validCount}</strong> · Skip:{" "}
              <strong>{importPreview.skippedCount}</strong> · Lỗi:{" "}
              <strong>{importPreview.errorCount}</strong>
            </p>
            {importPreview.skippedLessonIds.length > 0 && (
              <p className="note">
                Lesson bị skip vì đã có question:{" "}
                <strong>{importPreview.skippedLessonIds.join(", ")}</strong>
              </p>
            )}
            {importPreview.errors.length > 0 && (
              <p className="note">Lỗi preview: {importPreview.errors.slice(0, 5).join(" | ")}</p>
            )}

            {editableQuestions.length === 0 ? (
              <p className="note">Không có question hợp lệ để import.</p>
            ) : (
              <>
                <div style={{ display: "flex", justifyContent: "space-between", gap: "8px", flexWrap: "wrap" }}>
                  <p className="note" style={{ margin: 0 }}>
                    Bạn có thể chỉnh trực tiếp trước khi lưu thật vào DB.
                  </p>
                  <button type="button" onClick={() => void onCommitPreviewQuestions()} disabled={committing || loading}>
                    {committing ? "Đang lưu..." : "Lưu danh sách preview"}
                  </button>
                </div>
                {editableQuestions.map((question, index) => (
                  <div key={`${question.sourceRow}-${index}`} className="detail-box">
                    <p className="note" style={{ marginBottom: "8px" }}>
                      Dòng Excel #{question.sourceRow}
                    </p>
                    <div className="form-grid">
                      <select
                        value={String(question.lessonId)}
                        onChange={(e) =>
                          updateEditableQuestion(index, (current) => ({
                            ...current,
                            lessonId: Number(e.target.value || "0"),
                          }))
                        }
                      >
                        {lessons.map((lesson) => (
                          <option key={lesson.id} value={lesson.id}>
                            #{lesson.id} · Topic {lesson.topicId} · {lesson.title}
                          </option>
                        ))}
                      </select>
                      <p className="note">
                        Lesson hiện tại: <strong>{lessonLabelById.get(question.lessonId) || `#${question.lessonId}`}</strong>
                      </p>
                      <input
                        value={question.content}
                        onChange={(e) =>
                          updateEditableQuestion(index, (current) => ({
                            ...current,
                            content: e.target.value,
                          }))
                        }
                        placeholder="Nội dung câu hỏi"
                      />
                      <select
                        value={question.type}
                        onChange={(e) =>
                          updateEditableQuestion(index, (current) => ({
                            ...current,
                            type: e.target.value as QuestionImportPreviewItem["type"],
                          }))
                        }
                      >
                        {QUESTION_TYPES.map((type) => (
                          <option key={type} value={type}>
                            {type}
                          </option>
                        ))}
                      </select>
                      <label style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                        <input
                          type="checkbox"
                          checked={question.repeatable}
                          onChange={(e) =>
                            updateEditableQuestion(index, (current) => ({
                              ...current,
                              repeatable: e.target.checked,
                            }))
                          }
                        />
                        Repeatable
                      </label>
                      <input
                        value={question.mediaUrl || ""}
                        onChange={(e) =>
                          updateEditableQuestion(index, (current) => ({
                            ...current,
                            mediaUrl: e.target.value,
                          }))
                        }
                        placeholder="Media URL (nếu có)"
                      />
                    </div>

                    <div style={{ marginTop: "8px", display: "grid", gap: "8px" }}>
                      <p className="note" style={{ margin: 0 }}>
                        Đáp án ({question.answers.length})
                      </p>
                      {question.answers.map((answer, answerIndex) => (
                        <div
                          key={`${index}-${answerIndex}`}
                          style={{ display: "grid", gridTemplateColumns: "1fr auto auto", gap: "8px" }}
                        >
                          <input
                            value={answer.content}
                            onChange={(e) =>
                              updateEditableQuestion(index, (current) => ({
                                ...current,
                                answers: current.answers.map((item, itemIndex) =>
                                  itemIndex === answerIndex
                                    ? { ...item, content: e.target.value }
                                    : item
                                ),
                              }))
                            }
                            placeholder={`Đáp án ${answerIndex + 1}`}
                          />
                          <label style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                            <input
                              type="checkbox"
                              checked={answer.correct}
                              onChange={(e) =>
                                updateEditableQuestion(index, (current) => ({
                                  ...current,
                                  answers: current.answers.map((item, itemIndex) =>
                                    itemIndex === answerIndex
                                      ? { ...item, correct: e.target.checked }
                                      : item
                                  ),
                                }))
                              }
                            />
                            Correct
                          </label>
                          <button type="button" className="danger" onClick={() => removeAnswer(index, answerIndex)}>
                            Xóa đáp án
                          </button>
                        </div>
                      ))}
                      <button type="button" onClick={() => addAnswer(index)}>
                        + Thêm đáp án
                      </button>
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>
        )}
      </section>
    </div>
  );
}
