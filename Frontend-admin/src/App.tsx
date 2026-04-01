import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import {
  createLesson,
  createScenario,
  createTopic,
  deleteLesson,
  deleteScenario,
  deleteTopic,
  listLessons,
  listScenarios,
  listTopics,
  login,
  updateLesson,
  updateScenario,
  updateTopic,
  uploadImage,
} from "./api";
import type { Lesson, PageResponse, Scenario, Topic } from "./types";

type Tab = "topics" | "lessons" | "scenarios";

const PAGE_SIZE = 10;
const DEFAULT_API_BASE = "http://localhost:8080";
const API_BASE = (import.meta.env.VITE_API_BASE || DEFAULT_API_BASE).trim();

type PagedState<T> = {
  items: T[];
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
};

const initialPagedState = <T,>(): PagedState<T> => ({
  items: [],
  page: 0,
  size: PAGE_SIZE,
  totalPages: 0,
  totalElements: 0,
});

const LESSON_TYPES: Lesson["type"][] = ["LISTENING", "PRACTICING", "VOCABULARY"];

const trimOrEmpty = (value?: string | null) => (value || "").trim();
const isBlank = (value?: string | null) => trimOrEmpty(value).length === 0;
const exceeds = (value: string | null | undefined, max: number) => (value || "").length > max;
const isPositiveInt = (value: unknown) => Number.isInteger(value) && Number(value) > 0;

const ensureUrlIfProvided = (value?: string | null): boolean => {
  const raw = trimOrEmpty(value);
  if (!raw) {
    return true;
  }
  try {
    new URL(raw);
    return true;
  } catch {
    return false;
  }
};

const toUiError = (error: unknown) => {
  const raw = error instanceof Error ? error.message : "Loi khong xac dinh";
  if (raw.includes("Khong ket noi duoc backend")) {
    return `${raw} (Hien tai FE dang tro toi ${API_BASE})`;
  }
  return raw;
};

const validateLogin = (email: string, password: string): string | null => {
  const emailValue = trimOrEmpty(email);
  const passwordValue = trimOrEmpty(password);

  if (!emailValue) {
    return "Email khong duoc de trong";
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue)) {
    return "Email khong dung dinh dang";
  }
  if (!passwordValue) {
    return "Password khong duoc de trong";
  }
  return null;
};

const validateTopicForm = (form: Partial<Topic>): string | null => {
  if (isBlank(form.name)) {
    return "Ten topic khong duoc de trong";
  }
  if (exceeds(form.name, 255)) {
    return "Ten topic toi da 255 ky tu";
  }
  if (isBlank(form.description)) {
    return "Mo ta topic khong duoc de trong";
  }
  if (exceeds(form.description, 500)) {
    return "Mo ta topic toi da 500 ky tu";
  }
  if (exceeds(form.imageUrl, 1000)) {
    return "Image URL topic toi da 1000 ky tu";
  }
  if (!ensureUrlIfProvided(form.imageUrl)) {
    return "Image URL topic khong hop le";
  }
  return null;
};

const validateLessonForm = (form: Partial<Lesson>): string | null => {
  if (!isPositiveInt(form.topicId)) {
    return "Topic ID phai la so nguyen duong";
  }
  if (isBlank(form.title)) {
    return "Title lesson khong duoc de trong";
  }
  if (exceeds(form.title, 255)) {
    return "Title lesson toi da 255 ky tu";
  }
  if (!LESSON_TYPES.includes((form.type || "") as Lesson["type"])) {
    return "Loai lesson khong hop le";
  }
  if (exceeds(form.imageUrl, 1000)) {
    return "Image URL lesson toi da 1000 ky tu";
  }
  if (!ensureUrlIfProvided(form.imageUrl)) {
    return "Image URL lesson khong hop le";
  }
  if (form.parentId != null && form.parentId !== undefined && !isPositiveInt(form.parentId)) {
    return "Parent Lesson ID phai la so nguyen duong";
  }
  return null;
};

const validateScenarioForm = (form: Partial<Scenario>): string | null => {
  if (!isPositiveInt(form.topicId)) {
    return "Topic ID phai la so nguyen duong";
  }
  if (!isPositiveInt(form.lessonId)) {
    return "Lesson ID phai la so nguyen duong";
  }
  if (isBlank(form.title)) {
    return "Title scenario khong duoc de trong";
  }
  if (exceeds(form.title, 255)) {
    return "Title scenario toi da 255 ky tu";
  }
  if (isBlank(form.description)) {
    return "Mo ta scenario khong duoc de trong";
  }
  if (exceeds(form.description, 500)) {
    return "Mo ta scenario toi da 500 ky tu";
  }
  if (isBlank(form.aiRole)) {
    return "AI Role khong duoc de trong";
  }
  if (exceeds(form.aiRole, 50)) {
    return "AI Role toi da 50 ky tu";
  }
  if (isBlank(form.userRole)) {
    return "User Role khong duoc de trong";
  }
  if (exceeds(form.userRole, 50)) {
    return "User Role toi da 50 ky tu";
  }
  if (exceeds(form.tasks, 500)) {
    return "Tasks toi da 500 ky tu";
  }
  if (exceeds(form.openningMessage, 500)) {
    return "Openning message toi da 500 ky tu";
  }
  if (exceeds(form.suggestion, 500)) {
    return "Suggestion toi da 500 ky tu";
  }
  if (exceeds(form.translation, 500)) {
    return "Translation toi da 500 ky tu";
  }
  return null;
};

export function App() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState(localStorage.getItem("admin-token") || "");
  const [tab, setTab] = useState<Tab>("topics");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  const [topicState, setTopicState] = useState<PagedState<Topic>>(initialPagedState<Topic>());
  const [lessonState, setLessonState] = useState<PagedState<Lesson>>(initialPagedState<Lesson>());
  const [scenarioState, setScenarioState] = useState<PagedState<Scenario>>(initialPagedState<Scenario>());

  const [topicForm, setTopicForm] = useState<Partial<Topic>>({});
  const [lessonForm, setLessonForm] = useState<Partial<Lesson>>({ type: "LISTENING" });
  const [scenarioForm, setScenarioForm] = useState<Partial<Scenario>>({});
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [selectedLesson, setSelectedLesson] = useState<Lesson | null>(null);
  const [selectedScenario, setSelectedScenario] = useState<Scenario | null>(null);
  const [topicQuery, setTopicQuery] = useState("");
  const [lessonQuery, setLessonQuery] = useState("");
  const [scenarioQuery, setScenarioQuery] = useState("");

  const config = useMemo(() => ({ apiBase: API_BASE, token }), [token]);

  const topics = topicState.items;
  const lessons = lessonState.items;
  const scenarios = scenarioState.items;

  const normalize = (value: unknown) => String(value ?? "").toLowerCase();

  const filteredTopics = useMemo(() => {
    const q = normalize(topicQuery).trim();
    if (!q) {
      return topics;
    }
    return topics.filter((item) =>
      [item.id, item.name, item.description, item.imageUrl].some((part) => normalize(part).includes(q))
    );
  }, [topics, topicQuery]);

  const filteredLessons = useMemo(() => {
    const q = normalize(lessonQuery).trim();
    if (!q) {
      return lessons;
    }
    return lessons.filter((item) =>
      [item.id, item.topicId, item.title, item.type, item.imageUrl, item.parentId].some((part) =>
        normalize(part).includes(q)
      )
    );
  }, [lessons, lessonQuery]);

  const filteredScenarios = useMemo(() => {
    const q = normalize(scenarioQuery).trim();
    if (!q) {
      return scenarios;
    }
    return scenarios.filter((item) =>
      [
        item.id,
        item.topicId,
        item.lessonId,
        item.title,
        item.description,
        item.aiRole,
        item.userRole,
        item.tasks,
        item.openningMessage,
        item.suggestion,
        item.translation,
      ].some((part) => normalize(part).includes(q))
    );
  }, [scenarios, scenarioQuery]);

  const notify = (text: string) => setMessage(text);

  const requireAuth = () => {
    if (!token) {
      throw new Error("Ban can dang nhap admin");
    }
  };

  const toPagedState = <T,>(pageData: PageResponse<T>): PagedState<T> => ({
    items: pageData.content,
    page: pageData.number,
    size: pageData.size,
    totalPages: pageData.totalPages,
    totalElements: pageData.totalElements,
  });

  const loadTopics = useCallback(
    async (page = topicState.page) => {
      const data = await listTopics(config, page, PAGE_SIZE);
      setTopicState(toPagedState(data));
    },
    [config, topicState.page]
  );

  const loadLessons = useCallback(
    async (page = lessonState.page) => {
      const data = await listLessons(config, page, PAGE_SIZE);
      setLessonState(toPagedState(data));
    },
    [config, lessonState.page]
  );

  const loadScenarios = useCallback(
    async (page = scenarioState.page) => {
      const data = await listScenarios(config, page, PAGE_SIZE);
      setScenarioState(toPagedState(data));
    },
    [config, scenarioState.page]
  );

  const refreshAll = useCallback(async () => {
    requireAuth();
    setLoading(true);
    try {
      const [topicPage, lessonPage, scenarioPage] = await Promise.all([
        listTopics(config, topicState.page, PAGE_SIZE),
        listLessons(config, lessonState.page, PAGE_SIZE),
        listScenarios(config, scenarioState.page, PAGE_SIZE),
      ]);
      setTopicState(toPagedState(topicPage));
      setLessonState(toPagedState(lessonPage));
      setScenarioState(toPagedState(scenarioPage));
      notify("Da tai du lieu Topic, Lesson, Scenario");
    } catch (error) {
      notify(`Khong the tai du lieu: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  }, [config, token, topicState.page, lessonState.page, scenarioState.page]);

  useEffect(() => {
    if (!token) {
      return;
    }
    refreshAll().catch((error) => notify(`Khong the tai du lieu: ${toUiError(error)}`));
    // Only trigger initial load after login/token restore.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  const onLogin = async (event: FormEvent) => {
    event.preventDefault();
    const loginError = validateLogin(email, password);
    if (loginError) {
      notify(loginError);
      return;
    }

    setLoading(true);
    try {
      const nextToken = await login(API_BASE, {
        email: trimOrEmpty(email),
        password: trimOrEmpty(password),
      });
      setToken(nextToken);
      localStorage.setItem("admin-token", nextToken);
      notify("Dang nhap thanh cong");
    } catch (error) {
      notify(`Dang nhap that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken("");
    setTopicState(initialPagedState<Topic>());
    setLessonState(initialPagedState<Lesson>());
    setScenarioState(initialPagedState<Scenario>());
    setSelectedTopic(null);
    setSelectedLesson(null);
    setSelectedScenario(null);
    localStorage.removeItem("admin-token");
    notify("Da dang xuat");
  };

  const renderPagination = (
    entity: "topics" | "lessons" | "scenarios",
    state: PagedState<Topic | Lesson | Scenario>
  ) => {
    const canGoPrev = state.page > 0;
    const canGoNext = state.page + 1 < state.totalPages;

    const onChangePage = async (nextPage: number) => {
      setLoading(true);
      try {
        if (entity === "topics") {
          await loadTopics(nextPage);
        } else if (entity === "lessons") {
          await loadLessons(nextPage);
        } else {
          await loadScenarios(nextPage);
        }
      } catch (error) {
        notify(`Loi phan trang: ${toUiError(error)}`);
      } finally {
        setLoading(false);
      }
    };

    return (
      <div className="pagination">
        <div className="pagination-info">
          <span>Tong: {state.totalElements}</span>
          <span>
            Trang {state.totalPages === 0 ? 0 : state.page + 1}/{state.totalPages}
          </span>
        </div>
        <div className="pagination-actions">
          <button type="button" onClick={() => onChangePage(state.page - 1)} disabled={!canGoPrev || loading}>
            Previous
          </button>
          <button type="button" onClick={() => onChangePage(state.page + 1)} disabled={!canGoNext || loading}>
            Next
          </button>
        </div>
      </div>
    );
  };

  const onTopicSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();

    const validationError = validateTopicForm(topicForm);
    if (validationError) {
      notify(validationError);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        name: trimOrEmpty(topicForm.name),
        description: trimOrEmpty(topicForm.description),
        imageUrl: trimOrEmpty(topicForm.imageUrl) || null,
      };

      if (topicForm.id) {
        await updateTopic(config, topicForm.id, payload);
        notify("Cap nhat Topic thanh cong");
      } else {
        await createTopic(config, payload);
        notify("Tao Topic thanh cong");
      }
      setTopicForm({});
      setSelectedTopic(null);
      await refreshAll();
    } catch (error) {
      notify(`Loi luu Topic: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onLessonSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();

    const validationError = validateLessonForm(lessonForm);
    if (validationError) {
      notify(validationError);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        topicId: Number(lessonForm.topicId),
        title: trimOrEmpty(lessonForm.title),
        type: (lessonForm.type || "LISTENING") as Lesson["type"],
        imageUrl: trimOrEmpty(lessonForm.imageUrl) || null,
        parentId: lessonForm.parentId ? Number(lessonForm.parentId) : null,
      };

      if (lessonForm.id) {
        await updateLesson(config, lessonForm.id, payload);
        notify("Cap nhat Lesson thanh cong");
      } else {
        await createLesson(config, payload);
        notify("Tao Lesson thanh cong");
      }
      setLessonForm({ type: "LISTENING" });
      setSelectedLesson(null);
      await refreshAll();
    } catch (error) {
      notify(`Loi luu Lesson: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onScenarioSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();

    const validationError = validateScenarioForm(scenarioForm);
    if (validationError) {
      notify(validationError);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        topicId: Number(scenarioForm.topicId),
        lessonId: Number(scenarioForm.lessonId),
        title: trimOrEmpty(scenarioForm.title),
        description: trimOrEmpty(scenarioForm.description),
        aiRole: trimOrEmpty(scenarioForm.aiRole),
        userRole: trimOrEmpty(scenarioForm.userRole),
        tasks: trimOrEmpty(scenarioForm.tasks) || null,
        openningMessage: trimOrEmpty(scenarioForm.openningMessage) || null,
        suggestion: trimOrEmpty(scenarioForm.suggestion) || null,
        translation: trimOrEmpty(scenarioForm.translation) || null,
      };

      if (scenarioForm.id) {
        await updateScenario(config, scenarioForm.id, payload);
        notify("Cap nhat Scenario thanh cong");
      } else {
        await createScenario(config, payload);
        notify("Tao Scenario thanh cong");
      }
      setScenarioForm({});
      setSelectedScenario(null);
      await refreshAll();
    } catch (error) {
      notify(`Loi luu Scenario: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onUploadForTopic = async (file: File) => {
    requireAuth();
    try {
      const upload = await uploadImage(config, file, "elearn/topics");
      setTopicForm((prev) => ({ ...prev, imageUrl: upload.secureUrl }));
      notify(`Da upload Topic image: ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload Topic image that bai: ${toUiError(error)}`);
    }
  };

  const onUploadForLesson = async (file: File) => {
    requireAuth();
    try {
      const upload = await uploadImage(config, file, "elearn/lessons");
      setLessonForm((prev) => ({ ...prev, imageUrl: upload.secureUrl }));
      notify(`Da upload Lesson image: ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload Lesson image that bai: ${toUiError(error)}`);
    }
  };

  const onDeleteTopic = async (topic: Topic) => {
    if (!window.confirm(`Ban chac chan muon xoa Topic #${topic.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteTopic(config, topic.id);
      if (selectedTopic?.id === topic.id) {
        setSelectedTopic(null);
      }
      notify("Xoa Topic thanh cong");
      await refreshAll();
    } catch (error) {
      notify(`Xoa Topic that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onDeleteLesson = async (lesson: Lesson) => {
    if (!window.confirm(`Ban chac chan muon xoa Lesson #${lesson.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteLesson(config, lesson.id);
      if (selectedLesson?.id === lesson.id) {
        setSelectedLesson(null);
      }
      notify("Xoa Lesson thanh cong");
      await refreshAll();
    } catch (error) {
      notify(`Xoa Lesson that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onDeleteScenario = async (scenario: Scenario) => {
    if (!window.confirm(`Ban chac chan muon xoa Scenario #${scenario.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteScenario(config, scenario.id);
      if (selectedScenario?.id === scenario.id) {
        setSelectedScenario(null);
      }
      notify("Xoa Scenario thanh cong");
      await refreshAll();
    } catch (error) {
      notify(`Xoa Scenario that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const renderTopicTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Danh sach Topic</h3>
          <button onClick={() => { setSelectedTopic(null); setTopicForm({}); }} style={{ padding: "0.5rem 1rem" }}>
            + Tao Topic
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tim nhanh Topic tren trang hien tai..."
            value={topicQuery}
            onChange={(e) => setTopicQuery(e.target.value)}
          />
          <span>
            Hien {filteredTopics.length}/{topics.length} muc
          </span>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Ten</th>
              <th>Mo ta</th>
              <th>Anh</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredTopics.map((topic) => (
              <tr key={topic.id}>
                <td>{topic.id}</td>
                <td>{topic.name}</td>
                <td>{topic.description}</td>
                <td>{topic.imageUrl ? <a href={topic.imageUrl}>link</a> : "-"}</td>
                <td className="actions">
                  <button onClick={() => { setSelectedTopic(topic); setTopicForm({}); }}>View</button>
                  <button onClick={() => { setTopicForm(topic); setSelectedTopic(null); }}>Edit</button>
                  <button className="danger" onClick={() => onDeleteTopic(topic)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {renderPagination("topics", topicState)}
      </section>

      <section className="panel">
        {selectedTopic && !topicForm.id ? (
          <div>
            <div className="detail-box">
              <h4>Chi tiet Topic #{selectedTopic.id}</h4>
              <p><strong>Ten:</strong> {selectedTopic.name}</p>
              <p><strong>Mo ta:</strong> {selectedTopic.description}</p>
              <p>
                <strong>Image:</strong>{" "}
                {selectedTopic.imageUrl ? <a href={selectedTopic.imageUrl}>Mo anh</a> : "Khong co"}
              </p>
              {selectedTopic.imageUrl && (
                <img className="image-preview" src={selectedTopic.imageUrl} alt={selectedTopic.name} />
              )}
            </div>
          </div>
        ) : (
          <>
            <h3>{topicForm.id ? "Cap nhat Topic" : "Tao Topic"}</h3>
            <form onSubmit={onTopicSubmit} className="form-grid">
              <input
                placeholder="Ten topic"
                value={topicForm.name || ""}
                onChange={(e) => setTopicForm((prev) => ({ ...prev, name: e.target.value }))}
                maxLength={255}
                required
              />
              <textarea
                placeholder="Mo ta"
                value={topicForm.description || ""}
                onChange={(e) => setTopicForm((prev) => ({ ...prev, description: e.target.value }))}
                maxLength={500}
                required
              />
              <input
                placeholder="Image URL"
                value={topicForm.imageUrl || ""}
                onChange={(e) => setTopicForm((prev) => ({ ...prev, imageUrl: e.target.value }))}
                maxLength={1000}
              />
              {topicForm.imageUrl && (
                <img className="image-preview" src={topicForm.imageUrl} alt="Topic preview" />
              )}
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) {
                    onUploadForTopic(file).catch((err: Error) => notify(err.message));
                  }
                }}
              />
              <button type="submit" disabled={loading}>
                {topicForm.id ? "Luu cap nhat" : "Tao moi"}
              </button>
              <button type="button" onClick={() => setTopicForm({})}>
                Reset
              </button>
            </form>
          </>
        )}
      </section>
    </div>
  );

  const renderLessonTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Danh sach Lesson</h3>
          <button onClick={() => { setSelectedLesson(null); setLessonForm({ type: "LISTENING" }); }} style={{ padding: "0.5rem 1rem" }}>
            + Tao Lesson
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tim nhanh Lesson tren trang hien tai..."
            value={lessonQuery}
            onChange={(e) => setLessonQuery(e.target.value)}
          />
          <span>
            Hien {filteredLessons.length}/{lessons.length} muc
          </span>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Topic</th>
              <th>Title</th>
              <th>Type</th>
              <th>Anh</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredLessons.map((lesson) => (
              <tr key={lesson.id}>
                <td>{lesson.id}</td>
                <td>{lesson.topicId}</td>
                <td>{lesson.title}</td>
                <td>{lesson.type}</td>
                <td>{lesson.imageUrl ? <a href={lesson.imageUrl}>link</a> : "-"}</td>
                <td className="actions">
                  <button onClick={() => { setSelectedLesson(lesson); setLessonForm({}); }}>View</button>
                  <button onClick={() => { setLessonForm(lesson); setSelectedLesson(null); }}>Edit</button>
                  <button className="danger" onClick={() => onDeleteLesson(lesson)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {renderPagination("lessons", lessonState)}
      </section>

      <section className="panel">
        {selectedLesson && !lessonForm.id ? (
          <div>
            <div className="detail-box">
              <h4>Chi tiet Lesson #{selectedLesson.id}</h4>
              <p><strong>Topic ID:</strong> {selectedLesson.topicId}</p>
              <p><strong>Title:</strong> {selectedLesson.title}</p>
              <p><strong>Type:</strong> {selectedLesson.type}</p>
              <p><strong>Parent:</strong> {selectedLesson.parentId || "Khong co"}</p>
              <p>
                <strong>Image:</strong>{" "}
                {selectedLesson.imageUrl ? <a href={selectedLesson.imageUrl}>Mo anh</a> : "Khong co"}
              </p>
              {selectedLesson.imageUrl && (
                <img className="image-preview" src={selectedLesson.imageUrl} alt={selectedLesson.title} />
              )}
            </div>
          </div>
        ) : (
          <>
            <h3>{lessonForm.id ? "Cap nhat Lesson" : "Tao Lesson"}</h3>
            <form onSubmit={onLessonSubmit} className="form-grid">
              <input
                type="number"
                min={1}
                placeholder="Topic ID"
                value={lessonForm.topicId || ""}
                onChange={(e) =>
                  setLessonForm((prev) => ({ ...prev, topicId: e.target.value ? Number(e.target.value) : undefined }))
                }
                required
              />
              <input
                placeholder="Title"
                value={lessonForm.title || ""}
                onChange={(e) => setLessonForm((prev) => ({ ...prev, title: e.target.value }))}
                maxLength={255}
                required
              />
              <select
                value={lessonForm.type || "LISTENING"}
                onChange={(e) =>
                  setLessonForm((prev) => ({ ...prev, type: e.target.value as Lesson["type"] }))
                }
              >
                <option value="LISTENING">LISTENING</option>
                <option value="PRACTICING">PRACTICING</option>
                <option value="VOCABULARY">VOCABULARY</option>
              </select>
              <input
                placeholder="Image URL"
                value={lessonForm.imageUrl || ""}
                onChange={(e) => setLessonForm((prev) => ({ ...prev, imageUrl: e.target.value }))}
                maxLength={1000}
              />
              {lessonForm.imageUrl && (
                <img className="image-preview" src={lessonForm.imageUrl} alt="Lesson preview" />
              )}
              <input
                type="file"
                accept="image/*"
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) {
                    onUploadForLesson(file).catch((err: Error) => notify(err.message));
                  }
                }}
              />
              <input
                type="number"
                min={1}
                placeholder="Parent Lesson ID"
                value={lessonForm.parentId || ""}
                onChange={(e) =>
                  setLessonForm((prev) => ({
                    ...prev,
                    parentId: e.target.value ? Number(e.target.value) : null,
                  }))
                }
              />
              <button type="submit" disabled={loading}>
                {lessonForm.id ? "Luu cap nhat" : "Tao moi"}
              </button>
              <button type="button" onClick={() => setLessonForm({ type: "LISTENING" })}>
                Reset
              </button>
            </form>
          </>
        )}
      </section>
    </div>
  );

  const renderScenarioTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Danh sach Scenario</h3>
          <button onClick={() => { setSelectedScenario(null); setScenarioForm({}); }} style={{ padding: "0.5rem 1rem" }}>
            + Tao Scenario
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tim nhanh Scenario tren trang hien tai..."
            value={scenarioQuery}
            onChange={(e) => setScenarioQuery(e.target.value)}
          />
          <span>
            Hien {filteredScenarios.length}/{scenarios.length} muc
          </span>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Topic</th>
              <th>Lesson</th>
              <th>Title</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredScenarios.map((scenario) => (
              <tr key={scenario.id}>
                <td>{scenario.id}</td>
                <td>{scenario.topicId}</td>
                <td>{scenario.lessonId}</td>
                <td>{scenario.title}</td>
                <td className="actions">
                  <button onClick={() => { setSelectedScenario(scenario); setScenarioForm({}); }}>View</button>
                  <button onClick={() => { setScenarioForm(scenario); setSelectedScenario(null); }}>Edit</button>
                  <button className="danger" onClick={() => onDeleteScenario(scenario)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {renderPagination("scenarios", scenarioState)}
      </section>

      <section className="panel">
        {selectedScenario && !scenarioForm.id ? (
          <div>
            <div className="detail-box">
              <h4>Chi tiet Scenario #{selectedScenario.id}</h4>
              <p><strong>Topic ID:</strong> {selectedScenario.topicId}</p>
              <p><strong>Lesson ID:</strong> {selectedScenario.lessonId}</p>
              <p><strong>Title:</strong> {selectedScenario.title}</p>
              <p><strong>Description:</strong> {selectedScenario.description}</p>
              <p><strong>AI Role:</strong> {selectedScenario.aiRole}</p>
              <p><strong>User Role:</strong> {selectedScenario.userRole}</p>
              <p><strong>Tasks:</strong> {selectedScenario.tasks || "-"}</p>
              <p><strong>Openning:</strong> {selectedScenario.openningMessage || "-"}</p>
              <p><strong>Suggestion:</strong> {selectedScenario.suggestion || "-"}</p>
              <p><strong>Translation:</strong> {selectedScenario.translation || "-"}</p>
            </div>
          </div>
        ) : (
          <>
            <h3>{scenarioForm.id ? "Cap nhat Scenario" : "Tao Scenario"}</h3>
            <form onSubmit={onScenarioSubmit} className="form-grid">
              <input
                type="number"
                min={1}
                placeholder="Topic ID"
                value={scenarioForm.topicId || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, topicId: e.target.value ? Number(e.target.value) : undefined }))
                }
                required
              />
              <input
                type="number"
                min={1}
                placeholder="Lesson ID"
                value={scenarioForm.lessonId || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, lessonId: e.target.value ? Number(e.target.value) : undefined }))
                }
                required
              />
              <input
                placeholder="Title"
                value={scenarioForm.title || ""}
                onChange={(e) => setScenarioForm((prev) => ({ ...prev, title: e.target.value }))}
                maxLength={255}
                required
              />
              <textarea
                placeholder="Description"
                value={scenarioForm.description || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, description: e.target.value }))
                }
                maxLength={500}
                required
              />
              <input
                placeholder="AI Role"
                value={scenarioForm.aiRole || ""}
                onChange={(e) => setScenarioForm((prev) => ({ ...prev, aiRole: e.target.value }))}
                maxLength={50}
                required
              />
              <input
                placeholder="User Role"
                value={scenarioForm.userRole || ""}
                onChange={(e) => setScenarioForm((prev) => ({ ...prev, userRole: e.target.value }))}
                maxLength={50}
                required
              />
              <textarea
                placeholder="Tasks"
                value={scenarioForm.tasks || ""}
                onChange={(e) => setScenarioForm((prev) => ({ ...prev, tasks: e.target.value }))}
                maxLength={500}
              />
              <textarea
                placeholder="Openning message"
                value={scenarioForm.openningMessage || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, openningMessage: e.target.value }))
                }
                maxLength={500}
              />
              <textarea
                placeholder="Suggestion"
                value={scenarioForm.suggestion || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, suggestion: e.target.value }))
                }
                maxLength={500}
              />
              <textarea
                placeholder="Translation"
                value={scenarioForm.translation || ""}
                onChange={(e) =>
                  setScenarioForm((prev) => ({ ...prev, translation: e.target.value }))
                }
                maxLength={500}
              />
              <button type="submit" disabled={loading}>
                {scenarioForm.id ? "Luu cap nhat" : "Tao moi"}
              </button>
              <button type="button" onClick={() => setScenarioForm({})}>
                Reset
              </button>
            </form>
          </>
        )}
      </section>
    </div>
  );

  if (!token) {
    return (
      <div className="login-page">
        <section className="login-card">
          <div className="login-head">
            <p className="eyebrow">E-Learning Admin</p>
            <h1>Dang nhap he thong quan tri</h1>
            <p className="note">He thong quan tri Topic, Lesson, Scenario</p>
          </div>
          <form onSubmit={onLogin} className="form-grid">
            <label>
              Email
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="admin@email.com"
                type="email"
                required
              />
            </label>
            <label>
              Password
              <input
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
                type="password"
                required
              />
            </label>
            <button type="submit" disabled={loading}>
              {loading ? "Dang dang nhap..." : "Dang nhap"}
            </button>
          </form>
          <p className="note">{message || "Nhap thong tin tai khoan admin BE de dang nhap."}</p>
        </section>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="brand">E-Learn Admin</div>
        <p className="side-label">Quan tri noi dung</p>
        <nav className="menu">
          <button className={tab === "topics" ? "active" : ""} onClick={() => setTab("topics")}>
            Topic
          </button>
          <button className={tab === "lessons" ? "active" : ""} onClick={() => setTab("lessons")}>
            Lesson
          </button>
          <button className={tab === "scenarios" ? "active" : ""} onClick={() => setTab("scenarios")}>
            Scenario
          </button>
        </nav>
      </aside>

      <main className="dashboard-main">
        <header className="topbar">
          <div>
            <p className="eyebrow">Dashboard</p>
            <h1>Topic - Lesson - Scenario</h1>
          </div>
          <div className="top-actions">
            <button onClick={() => refreshAll()} disabled={loading}>
              Refresh
            </button>
            <button onClick={logout}>Logout</button>
          </div>
        </header>

        <section className="stats-grid">
          <article className="stat-card">
            <p>Topic</p>
            <strong>{topicState.totalElements}</strong>
          </article>
          <article className="stat-card">
            <p>Lesson</p>
            <strong>{lessonState.totalElements}</strong>
          </article>
          <article className="stat-card">
            <p>Scenario</p>
            <strong>{scenarioState.totalElements}</strong>
          </article>
        </section>

        {tab === "topics" && renderTopicTab()}
        {tab === "lessons" && renderLessonTab()}
        {tab === "scenarios" && renderScenarioTab()}

        <p className="note status-note">{message}</p>
      </main>
    </div>
  );
}
