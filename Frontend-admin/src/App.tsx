import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import * as XLSX from "xlsx";
import {
  bulkImportLessons,
  bulkImportScenarios,
  bulkImportTopics,
  createLesson,
  createScenario,
  createTopic,
  createTopicFlashCard,
  createTopicWord,
  deleteLesson,
  deleteTopicFlashCard,
  deleteTopicWord,
  deleteScenario,
  deleteTopic,
  importFlashCardsExcel,
  importWordsExcel,
  listAllLessons,
  listAllTopics,
  listTopicFlashCards,
  listTopicWords,
  listLessons,
  listScenarios,
  listTopics,
  login,
  updateLesson,
  updateTopicFlashCard,
  updateTopicWord,
  updateScenario,
  updateTopic,
  uploadImage,
} from "./api";
import type { FlashCard, Lesson, PageResponse, Scenario, Topic, Word } from "./types";
import { QuestionImportTab } from "./QuestionImportTab";

type Tab = "topics" | "lessons" | "scenarios" | "words" | "flashcards" | "questions-import";

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
const WORD_TYPES: Word["type"][] = ["NOUN", "VERB", "ADJECTIVE"];

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

const parseCSV = (csv: string): string[][] => {
  const rows: string[][] = [];
  let currentRow: string[] = [];
  let currentCell = "";
  let inQuotes = false;

  for (let i = 0; i < csv.length; i += 1) {
    const char = csv[i];
    const next = csv[i + 1];

    if (char === '"') {
      if (inQuotes && next === '"') {
        currentCell += '"';
        i += 1;
      } else {
        inQuotes = !inQuotes;
      }
      continue;
    }

    if (char === "," && !inQuotes) {
      currentRow.push(currentCell.trim());
      currentCell = "";
      continue;
    }

    if ((char === "\n" || char === "\r") && !inQuotes) {
      if (char === "\r" && next === "\n") {
        i += 1;
      }
      currentRow.push(currentCell.trim());
      currentCell = "";
      if (currentRow.some((cell) => cell.length > 0)) {
        rows.push(currentRow);
      }
      currentRow = [];
      continue;
    }

    currentCell += char;
  }

  currentRow.push(currentCell.trim());
  if (currentRow.some((cell) => cell.length > 0)) {
    rows.push(currentRow);
  }

  return rows;
};

const parseSpreadsheet = (arrayBuffer: ArrayBuffer): string[][] => {
  const workbook = XLSX.read(arrayBuffer, { type: "array" });
  const firstSheetName = workbook.SheetNames[0];
  if (!firstSheetName) {
    return [];
  }
  const worksheet = workbook.Sheets[firstSheetName];
  const rows = XLSX.utils.sheet_to_json<(string | number | boolean | null)[]>(worksheet, {
    header: 1,
    raw: false,
    defval: "",
  });
  return rows
    .map((row) => row.map((cell) => String(cell ?? "").trim()))
    .filter((row) => row.some((cell) => cell.length > 0));
};

const readImportRows = async (file: File): Promise<string[][]> => {
  const lower = file.name.toLowerCase();
  if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
    const buffer = await file.arrayBuffer();
    return parseSpreadsheet(buffer);
  }
  const text = await file.text();
  return parseCSV(text);
};

const TOPIC_TEMPLATE_CSV = [
  "name,description,imageUrl",
  'English Basics,"Introduction to core English communication",https://images.unsplash.com/photo-1456283174360-12bccda6dda9?w=1200',
].join("\n");

const toUiError = (error: unknown) => {
  const raw = error instanceof Error ? error.message : "Lỗi không xác định";
  if (raw.includes("Không kết nối được backend")) {
    return `${raw} (Hiện tại FE đang trỏ tới ${API_BASE})`;
  }
  return raw;
};

const validateLogin = (email: string, password: string): string | null => {
  const emailValue = trimOrEmpty(email);
  const passwordValue = trimOrEmpty(password);

  if (!emailValue) {
    return "Email không được để trống";
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailValue)) {
    return "Email không đúng định dạng";
  }
  if (!passwordValue) {
    return "Mật khẩu không được để trống";
  }
  return null;
};

const validateTopicForm = (form: Partial<Topic>): string | null => {
  if (isBlank(form.name)) {
    return "Tên topic không được để trống";
  }
  if (exceeds(form.name, 255)) {
    return "Tên topic tối đa 255 ký tự";
  }
  if (isBlank(form.description)) {
    return "Mô tả topic không được để trống";
  }
  if (exceeds(form.description, 500)) {
    return "Mô tả topic tối đa 500 ký tự";
  }
  if (exceeds(form.imageUrl, 1000)) {
    return "Image URL topic tối đa 1000 ký tự";
  }
  if (!ensureUrlIfProvided(form.imageUrl)) {
    return "Image URL topic không hợp lệ";
  }
  return null;
};

const validateLessonForm = (form: Partial<Lesson>): string | null => {
  if (!isPositiveInt(form.topicId)) {
    return "Topic ID phải là số nguyên dương";
  }
  if (isBlank(form.title)) {
    return "Tiêu đề lesson không được để trống";
  }
  if (exceeds(form.title, 255)) {
    return "Tiêu đề lesson tối đa 255 ký tự";
  }
  if (!LESSON_TYPES.includes((form.type || "") as Lesson["type"])) {
    return "Loại lesson không hợp lệ";
  }
  if (exceeds(form.imageUrl, 1000)) {
    return "Image URL lesson tối đa 1000 ký tự";
  }
  if (!ensureUrlIfProvided(form.imageUrl)) {
    return "Image URL lesson không hợp lệ";
  }
  if (form.parentId != null && form.parentId !== undefined && !isPositiveInt(form.parentId)) {
    return "Parent Lesson ID phải là số nguyên dương";
  }
  return null;
};

const validateScenarioForm = (form: Partial<Scenario>): string | null => {
  if (!isPositiveInt(form.topicId)) {
    return "Topic ID phải là số nguyên dương";
  }
  if (!isPositiveInt(form.lessonId)) {
    return "Lesson ID phải là số nguyên dương";
  }
  if (isBlank(form.title)) {
    return "Tiêu đề scenario không được để trống";
  }
  if (exceeds(form.title, 255)) {
    return "Tiêu đề scenario tối đa 255 ký tự";
  }
  if (isBlank(form.description)) {
    return "Mô tả scenario không được để trống";
  }
  if (exceeds(form.description, 500)) {
    return "Mô tả scenario tối đa 500 ký tự";
  }
  if (isBlank(form.aiRole)) {
    return "AI Role không được để trống";
  }
  if (exceeds(form.aiRole, 50)) {
    return "AI Role tối đa 50 ký tự";
  }
  if (isBlank(form.userRole)) {
    return "User Role không được để trống";
  }
  if (exceeds(form.userRole, 50)) {
    return "User Role tối đa 50 ký tự";
  }
  if (exceeds(form.tasks, 500)) {
    return "Tasks tối đa 500 ký tự";
  }
  if (exceeds(form.openningMessage, 500)) {
    return "Openning message tối đa 500 ký tự";
  }
  if (exceeds(form.suggestion, 500)) {
    return "Suggestion tối đa 500 ký tự";
  }
  if (exceeds(form.translation, 500)) {
    return "Translation tối đa 500 ký tự";
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

  const [wordTopicId, setWordTopicId] = useState<number | "">("");
  const [words, setWords] = useState<Word[]>([]);
  const [wordForm, setWordForm] = useState<Partial<Omit<Word, "id">>>({ type: "NOUN" });
  const [editingWordId, setEditingWordId] = useState<number | null>(null);
  const [wordQuery, setWordQuery] = useState("");

  const [flashTopicId, setFlashTopicId] = useState<number | "">("");
  const [flashCards, setFlashCards] = useState<FlashCard[]>([]);
  const [flashForm, setFlashForm] = useState<{
    dictionaryWordId?: number | null;
    word?: string | null;
    pronunciation?: string | null;
    meaning?: string | null;
    type?: Word["type"] | null;
    example?: string | null;
    imageUrl?: string | null;
    imageName?: string | null;
    imageSize?: number | null;
  }>({ type: "NOUN" });
  const [editingFlashId, setEditingFlashId] = useState<number | null>(null);
  const [flashQuery, setFlashQuery] = useState("");
  const [selectedWordIds, setSelectedWordIds] = useState<Set<number>>(() => new Set());
  const [selectedFlashCardIds, setSelectedFlashCardIds] = useState<Set<number>>(() => new Set());
  const [topicsForSelect, setTopicsForSelect] = useState<Topic[]>([]);

  const [topicForm, setTopicForm] = useState<Partial<Topic>>({});
  const [lessonForm, setLessonForm] = useState<Partial<Lesson>>({ type: "LISTENING" });
  const [scenarioForm, setScenarioForm] = useState<Partial<Scenario>>({});
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [selectedLesson, setSelectedLesson] = useState<Lesson | null>(null);
  const [selectedScenario, setSelectedScenario] = useState<Scenario | null>(null);
  const [topicQuery, setTopicQuery] = useState("");
  const [lessonQuery, setLessonQuery] = useState("");
  const [scenarioQuery, setScenarioQuery] = useState("");
  const topicImportInputRef = useRef<HTMLInputElement | null>(null);
  const lessonImportInputRef = useRef<HTMLInputElement | null>(null);
  const scenarioImportInputRef = useRef<HTMLInputElement | null>(null);

  const config = useMemo(() => ({ apiBase: API_BASE, token }), [token]);

  const topics = topicState.items;
  const lessons = lessonState.items;
  const scenarios = scenarioState.items;

  const buildLessonTemplateCsv = useCallback(() => {
    const sampleTopicId = topics[0]?.id ?? 1;
    return [
      "topicId,title,type,imageUrl,parentId",
      `${sampleTopicId},Welcome to English,LISTENING,https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=1200,`,
    ].join("\n");
  }, [topics]);

  const buildScenarioTemplateCsv = useCallback(() => {
    const sampleLesson = lessons[0];
    const sampleTopicId = sampleLesson?.topicId ?? topics[0]?.id ?? 1;
    const sampleLessonId = sampleLesson?.id ?? 1;
    return [
      "topicId,lessonId,title,description,aiRole,userRole,tasks,openningMessage,suggestion,translation",
      `${sampleTopicId},${sampleLessonId},Welcome Dialogue,"Basic greeting and self-introduction",Tutor,Learner,"Introduce yourself","Hello! Nice to meet you.","Use short sentences","Bạn đang tập giới thiệu bản thân"`,
    ].join("\n");
  }, [lessons, topics]);

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

  const filteredWords = useMemo(() => {
    const q = normalize(wordQuery).trim();
    if (!q) return words;
    return words.filter((w) =>
      [w.id, w.word, w.pronunciation, w.meaning, w.type].some((part) => normalize(part).includes(q))
    );
  }, [words, wordQuery]);

  const filteredFlashCards = useMemo(() => {
    const q = normalize(flashQuery).trim();
    if (!q) return flashCards;
    return flashCards.filter((c) =>
      [c.id, c.word, c.pronunciation, c.meaning, c.example, c.imageUrl].some((part) =>
        normalize(part).includes(q)
      )
    );
  }, [flashCards, flashQuery]);

  const allFilteredWordsSelected =
    filteredWords.length > 0 && filteredWords.every((w) => selectedWordIds.has(w.id));
  const allFilteredFlashCardsSelected =
    filteredFlashCards.length > 0 && filteredFlashCards.every((c) => selectedFlashCardIds.has(c.id));

  const notify = (text: string) => setMessage(text);

  const downloadCsvTemplate = (fileName: string, content: string) => {
    const blob = new Blob([content], { type: "text/csv;charset=utf-8;" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  };

  const requireAuth = () => {
    if (!token) {
      throw new Error("Bạn cần đăng nhập admin");
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

  const refreshAll = useCallback(async (silent = false) => {
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
      try {
        const allTopics = await listAllTopics(config);
        setTopicsForSelect(allTopics);
      } catch {
        /* dropdown refresh optional */
      }
      if (!silent) {
        notify("Đã tải dữ liệu Topic, Lesson, Scenario");
      }
    } catch (error) {
      notify(`Không thể tải dữ liệu: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  }, [config, token, topicState.page, lessonState.page, scenarioState.page]);

  const loadWordsForTopic = useCallback(
    async (topicId: number) => {
      requireAuth();
      const data = await listTopicWords(config, topicId);
      setWords(data);
    },
    [config, token]
  );

  const loadFlashCardsForTopic = useCallback(
    async (topicId: number) => {
      requireAuth();
      const data = await listTopicFlashCards(config, topicId);
      setFlashCards(data);
    },
    [config, token]
  );

  const loadTopicsForSelect = useCallback(async () => {
    requireAuth();
    const all = await listAllTopics(config);
    setTopicsForSelect(all);
  }, [config, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    refreshAll().catch((error) => notify(`Không thể tải dữ liệu: ${toUiError(error)}`));
    // Only trigger initial load after login/token restore.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    if (!token || (tab !== "words" && tab !== "flashcards")) {
      return;
    }
    loadTopicsForSelect().catch((e) => notify(`Không tải được danh sách topic: ${toUiError(e)}`));
  }, [token, tab, loadTopicsForSelect]);

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
      notify("Đăng nhập thành công");
    } catch (error) {
      notify(`Đăng nhập thất bại: ${toUiError(error)}`);
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
    setWords([]);
    setFlashCards([]);
    setWordTopicId("");
    setFlashTopicId("");
    setEditingWordId(null);
    setEditingFlashId(null);
    setSelectedWordIds(new Set());
    setSelectedFlashCardIds(new Set());
    setTopicsForSelect([]);
    localStorage.removeItem("admin-token");
    notify("Đã đăng xuất");
  };

  const validateWordForm = (form: Partial<Omit<Word, "id">>): string | null => {
    if (isBlank(form.word)) return "Word không được để trống";
    if (exceeds(form.word, 50)) return "Word tối đa 50 ký tự";
    if (isBlank(form.pronunciation)) return "Pronunciation không được để trống";
    if (exceeds(form.pronunciation, 50)) return "Pronunciation tối đa 50 ký tự";
    if (isBlank(form.meaning)) return "Meaning không được để trống";
    if (exceeds(form.meaning, 255)) return "Meaning tối đa 255 ký tự";
    if (!WORD_TYPES.includes((form.type || "") as Word["type"])) return "Word type không hợp lệ";
    return null;
  };

  const validateFlashForm = (isEdit: boolean): string | null => {
    if (!isPositiveInt(flashTopicId)) return "Chọn topic";
    if (isBlank(flashForm.example)) return "Example không được để trống";
    if (exceeds(flashForm.example, 255)) return "Example tối đa 255 ký tự";
    if (!isEdit) {
      if (isBlank(flashForm.imageUrl)) return "Hãy tải ảnh flashcard (Cloudinary)";
      if (exceeds(flashForm.imageUrl, 1000)) return "Image URL tối đa 1000 ký tự";
      if (!ensureUrlIfProvided(flashForm.imageUrl || "")) return "Ảnh tải lên không hợp lệ";
    } else if (!isBlank(flashForm.imageUrl)) {
      if (exceeds(flashForm.imageUrl, 1000)) return "Image URL tối đa 1000 ký tự";
      if (!ensureUrlIfProvided(flashForm.imageUrl || "")) return "Ảnh tải lên không hợp lệ";
    }

    const hasWordId = isPositiveInt(flashForm.dictionaryWordId);
    if (!hasWordId) {
      const err = validateWordForm({
        word: flashForm.word || "",
        pronunciation: flashForm.pronunciation || "",
        meaning: flashForm.meaning || "",
        type: (flashForm.type || "NOUN") as Word["type"],
      });
      if (err) return `Flashcard — từ: ${err}`;
    }
    return null;
  };

  const onWordSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();
    if (!isPositiveInt(wordTopicId)) {
      notify("Chọn topic");
      return;
    }

    const validationError = validateWordForm(wordForm);
    if (validationError) {
      notify(validationError);
      return;
    }

    setLoading(true);
    try {
      const payload = {
        word: trimOrEmpty(wordForm.word),
        pronunciation: trimOrEmpty(wordForm.pronunciation),
        meaning: trimOrEmpty(wordForm.meaning),
        type: (wordForm.type || "NOUN") as Word["type"],
      };
      if (editingWordId != null) {
        await updateTopicWord(config, Number(wordTopicId), editingWordId, payload);
        notify("Đã cập nhật từ");
        setEditingWordId(null);
      } else {
        await createTopicWord(config, Number(wordTopicId), payload);
        notify("Đã thêm từ vào topic");
      }
      setWordForm({ type: "NOUN" });
      await loadWordsForTopic(Number(wordTopicId));
    } catch (error) {
      notify(`Lưu từ thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onFlashSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();

    const validationError = validateFlashForm(editingFlashId != null);
    if (validationError) {
      notify(validationError);
      return;
    }

    setLoading(true);
    try {
      const topicIdNum = Number(flashTopicId);
      if (editingFlashId != null) {
        const img = trimOrEmpty(flashForm.imageUrl);
        await updateTopicFlashCard(config, topicIdNum, editingFlashId, {
          example: trimOrEmpty(flashForm.example),
          ...(img
            ? {
                imageUrl: img,
                imageName: trimOrEmpty(flashForm.imageName) || null,
                imageSize: flashForm.imageSize ?? null,
              }
            : {}),
          ...(isPositiveInt(flashForm.dictionaryWordId)
            ? { dictionaryWordId: Number(flashForm.dictionaryWordId) }
            : {
                word: trimOrEmpty(flashForm.word),
                pronunciation: trimOrEmpty(flashForm.pronunciation),
                meaning: trimOrEmpty(flashForm.meaning),
                type: (flashForm.type || "NOUN") as Word["type"],
              }),
        });
        notify("Đã cập nhật flashcard");
        setEditingFlashId(null);
      } else {
        await createTopicFlashCard(config, topicIdNum, {
          dictionaryWordId: flashForm.dictionaryWordId ? Number(flashForm.dictionaryWordId) : null,
          word: trimOrEmpty(flashForm.word),
          pronunciation: trimOrEmpty(flashForm.pronunciation),
          meaning: trimOrEmpty(flashForm.meaning),
          type: (flashForm.type || "NOUN") as Word["type"],
          example: trimOrEmpty(flashForm.example),
          imageUrl: trimOrEmpty(flashForm.imageUrl),
          imageName: trimOrEmpty(flashForm.imageName) || null,
          imageSize: flashForm.imageSize ?? null,
        });
        notify("Đã tạo flashcard theo topic");
      }
      setFlashForm({ type: "NOUN", dictionaryWordId: null });
      await loadFlashCardsForTopic(topicIdNum);
    } catch (error) {
      notify(`Lưu flashcard thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const lessonSelectValue = (id: number | "") => (id === "" ? "" : String(id));

  const onWordTopicChange = (value: string) => {
    const id = value === "" ? "" : Number(value);
    setWordTopicId(id);
    setSelectedWordIds(new Set());
    if (isPositiveInt(id)) {
      setLoading(true);
      loadWordsForTopic(Number(id))
        .then(() => notify("Đã tải từ vựng"))
        .catch((e) => notify(toUiError(e)))
        .finally(() => setLoading(false));
    } else {
      setWords([]);
    }
  };

  const onFlashTopicChange = (value: string) => {
    const id = value === "" ? "" : Number(value);
    setFlashTopicId(id);
    setSelectedFlashCardIds(new Set());
    if (isPositiveInt(id)) {
      setLoading(true);
      loadFlashCardsForTopic(Number(id))
        .then(() => notify("Đã tải flashcard"))
        .catch((e) => notify(toUiError(e)))
        .finally(() => setLoading(false));
    } else {
      setFlashCards([]);
    }
  };

  const toggleWordRowSelected = (wordId: number) => {
    setSelectedWordIds((prev) => {
      const next = new Set(prev);
      if (next.has(wordId)) {
        next.delete(wordId);
      } else {
        next.add(wordId);
      }
      return next;
    });
  };

  const toggleSelectAllFilteredWords = () => {
    setSelectedWordIds((prev) => {
      const next = new Set(prev);
      if (allFilteredWordsSelected) {
        filteredWords.forEach((w) => next.delete(w.id));
      } else {
        filteredWords.forEach((w) => next.add(w.id));
      }
      return next;
    });
  };

  const bulkDeleteSelectedWords = async () => {
    requireAuth();
    if (!isPositiveInt(wordTopicId)) {
      notify("Chọn topic trước.");
      return;
    }
    const ids = [...selectedWordIds];
    if (ids.length === 0) {
      return;
    }
    if (
      !confirm(
        `Xóa ${ids.length} từ đã chọn? Các flashcard gắn các từ này trong topic cũng sẽ bị xóa.`
      )
    ) {
      return;
    }
    setLoading(true);
    try {
      const results = await Promise.allSettled(
        ids.map((id) => deleteTopicWord(config, Number(wordTopicId), id))
      );
      const failed = results.filter((r) => r.status === "rejected").length;
      if (failed > 0) {
        notify(`Đã xóa ${ids.length - failed}/${ids.length} từ. ${failed} lỗi.`);
      } else {
        notify(`Đã xóa ${ids.length} từ.`);
      }
      setSelectedWordIds(new Set());
      setEditingWordId(null);
      setWordForm({ type: "NOUN" });
      await loadWordsForTopic(Number(wordTopicId));
    } catch (error) {
      notify(`Xóa hàng loạt thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const toggleFlashCardRowSelected = (flashId: number) => {
    setSelectedFlashCardIds((prev) => {
      const next = new Set(prev);
      if (next.has(flashId)) {
        next.delete(flashId);
      } else {
        next.add(flashId);
      }
      return next;
    });
  };

  const toggleSelectAllFilteredFlashCards = () => {
    setSelectedFlashCardIds((prev) => {
      const next = new Set(prev);
      if (allFilteredFlashCardsSelected) {
        filteredFlashCards.forEach((c) => next.delete(c.id));
      } else {
        filteredFlashCards.forEach((c) => next.add(c.id));
      }
      return next;
    });
  };

  const bulkDeleteSelectedFlashCards = async () => {
    requireAuth();
    if (!isPositiveInt(flashTopicId)) {
      notify("Chọn topic trước.");
      return;
    }
    const ids = [...selectedFlashCardIds];
    if (ids.length === 0) {
      return;
    }
    if (!confirm(`Xóa ${ids.length} flashcard đã chọn?`)) {
      return;
    }
    setLoading(true);
    try {
      const results = await Promise.allSettled(
        ids.map((id) => deleteTopicFlashCard(config, Number(flashTopicId), id))
      );
      const failed = results.filter((r) => r.status === "rejected").length;
      if (failed > 0) {
        notify(`Đã xóa ${ids.length - failed}/${ids.length} flashcard. ${failed} lỗi.`);
      } else {
        notify(`Đã xóa ${ids.length} flashcard.`);
      }
      setSelectedFlashCardIds(new Set());
      setEditingFlashId(null);
      setFlashForm({ type: "NOUN", dictionaryWordId: null });
      await loadFlashCardsForTopic(Number(flashTopicId));
    } catch (error) {
      notify(`Xóa hàng loạt thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const runWordExcelImport = async (file: File) => {
    requireAuth();
    if (!isPositiveInt(wordTopicId)) {
      notify("Chọn topic trước khi import (topic áp dụng cho toàn bộ dòng trong file)");
      return;
    }
    setLoading(true);
    try {
      const r = await importWordsExcel(config, Number(wordTopicId), file);
      const hint =
        r.errors?.length > 0 ? ` Ví dụ lỗi: ${r.errors.slice(0, 3).join(" | ")}` : "";
      notify(`Import vocabulary: ${r.successCount} thành công, ${r.errorCount} lỗi.${hint}`);
      if (isPositiveInt(wordTopicId)) {
        await loadWordsForTopic(Number(wordTopicId));
      }
    } catch (error) {
      notify(`Import vocabulary thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const runFlashExcelImport = async (file: File) => {
    requireAuth();
    if (!isPositiveInt(flashTopicId)) {
      notify("Chọn topic trước khi import (topic áp dụng cho toàn bộ dòng trong file)");
      return;
    }
    setLoading(true);
    try {
      const r = await importFlashCardsExcel(config, Number(flashTopicId), file);
      const hint =
        r.errors?.length > 0 ? ` Ví dụ lỗi: ${r.errors.slice(0, 3).join(" | ")}` : "";
      notify(`Import flashcard: ${r.successCount} thành công, ${r.errorCount} lỗi.${hint}`);
      if (isPositiveInt(flashTopicId)) {
        await loadFlashCardsForTopic(Number(flashTopicId));
      }
    } catch (error) {
      notify(`Import flashcard thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const renderWordsTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Từ vựng theo topic</h3>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            <button
              type="button"
              onClick={() => {
                const fallback = selectedTopic?.id;
                if (fallback) {
                  onWordTopicChange(String(fallback));
                } else {
                  notify("Chọn topic ở tab Topic trước (View)");
                }
              }}
              style={{ padding: "0.5rem 1rem" }}
            >
              Dùng topic đang chọn (tab Topic)
            </button>
            <button
              type="button"
              onClick={() => {
                loadTopicsForSelect().catch((e) => notify(toUiError(e)));
              }}
              disabled={loading}
              style={{ padding: "0.5rem 1rem" }}
            >
              Tải lại danh sách topic
            </button>
          </div>
        </div>
        <div className="list-toolbar">
          <label style={{ flex: 1, display: "grid", gap: "6px", minWidth: 0 }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn topic
            </span>
            <select
              value={lessonSelectValue(wordTopicId)}
              onChange={(e) => onWordTopicChange(e.target.value)}
            >
              <option value="">-- Chọn topic --</option>
              {topicsForSelect.map((t) => (
                <option key={t.id} value={t.id}>
                  #{t.id} · {t.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tìm nhanh từ..."
            value={wordQuery}
            onChange={(e) => setWordQuery(e.target.value)}
          />
          <span>Hiện {filteredWords.length}/{words.length} mục</span>
        </div>
        <div className="list-toolbar" style={{ gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
          <button
            type="button"
            className="danger"
            disabled={loading || selectedWordIds.size === 0 || !isPositiveInt(wordTopicId)}
            onClick={() => void bulkDeleteSelectedWords()}
          >
            Xóa đã chọn ({selectedWordIds.size})
          </button>
        </div>
        <div className="list-toolbar" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
          <span className="note" style={{ fontWeight: 600 }}>
            Import Excel (.xlsx / .xls) — mỗi dòng một từ vào topic đang chọn ở trên
          </span>
          <span className="note">
            Hàng 1 (header): word · pronunciation · meaning · type (NOUN | VERB | ADJECTIVE)
          </span>
          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={loading || !isPositiveInt(wordTopicId)}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                void runWordExcelImport(file);
              }
              e.target.value = "";
            }}
          />
        </div>
        <table>
          <thead>
            <tr>
              <th style={{ width: 44 }}>
                <input
                  type="checkbox"
                  title="Chọn tất cả trong danh sách lọc"
                  checked={allFilteredWordsSelected}
                  onChange={toggleSelectAllFilteredWords}
                  disabled={loading || filteredWords.length === 0}
                />
              </th>
              <th>ID</th>
              <th>Word</th>
              <th>Pronunciation</th>
              <th>Meaning</th>
              <th>Type</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filteredWords.map((w) => (
              <tr key={w.id}>
                <td>
                  <input
                    type="checkbox"
                    checked={selectedWordIds.has(w.id)}
                    onChange={() => toggleWordRowSelected(w.id)}
                    disabled={loading}
                  />
                </td>
                <td>{w.id}</td>
                <td>{w.word}</td>
                <td>{w.pronunciation}</td>
                <td>{w.meaning}</td>
                <td>{w.type}</td>
                <td style={{ whiteSpace: "nowrap" }}>
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      setEditingWordId(w.id);
                      setWordForm({
                        word: w.word,
                        pronunciation: w.pronunciation,
                        meaning: w.meaning,
                        type: w.type,
                      });
                      notify("Đang sửa từ — bấm Cập nhật từ để lưu");
                    }}
                  >
                    Sửa
                  </button>{" "}
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      if (!isPositiveInt(wordTopicId)) {
                        notify("Chọn topic");
                        return;
                      }
                      if (
                        !confirm(
                          `Xóa từ "${w.word}"? Các flashcard gắn từ này trong topic cũng sẽ bị xóa.`
                        )
                      ) {
                        return;
                      }
                      setLoading(true);
                      deleteTopicWord(config, Number(wordTopicId), w.id)
                        .then(() => {
                          notify("Đã xóa từ");
                          setSelectedWordIds((prev) => {
                            const next = new Set(prev);
                            next.delete(w.id);
                            return next;
                          });
                          if (editingWordId === w.id) {
                            setEditingWordId(null);
                            setWordForm({ type: "NOUN" });
                          }
                          return loadWordsForTopic(Number(wordTopicId));
                        })
                        .catch((err) => notify(`Xóa từ thất bại: ${toUiError(err)}`))
                        .finally(() => setLoading(false));
                    }}
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h3>{editingWordId != null ? "Sửa từ trong topic" : "Thêm từ vào topic"}</h3>
        <form onSubmit={onWordSubmit} className="form-grid">
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn topic
            </span>
            <select
              value={lessonSelectValue(wordTopicId)}
              onChange={(e) => onWordTopicChange(e.target.value)}
              required
            >
              <option value="">-- Chọn topic --</option>
              {topicsForSelect.map((t) => (
                <option key={t.id} value={t.id}>
                  #{t.id} · {t.name}
                </option>
              ))}
            </select>
          </label>
          <input
            placeholder="Word"
            value={wordForm.word || ""}
            onChange={(e) => setWordForm((p) => ({ ...p, word: e.target.value }))}
            maxLength={50}
            required
          />
          <input
            placeholder="Pronunciation"
            value={wordForm.pronunciation || ""}
            onChange={(e) => setWordForm((p) => ({ ...p, pronunciation: e.target.value }))}
            maxLength={50}
            required
          />
          <input
            placeholder="Meaning"
            value={wordForm.meaning || ""}
            onChange={(e) => setWordForm((p) => ({ ...p, meaning: e.target.value }))}
            maxLength={255}
            required
          />
          <select
            value={wordForm.type || "NOUN"}
            onChange={(e) => setWordForm((p) => ({ ...p, type: e.target.value as Word["type"] }))}
          >
            <option value="NOUN">NOUN</option>
            <option value="VERB">VERB</option>
            <option value="ADJECTIVE">ADJECTIVE</option>
          </select>
          <button type="submit" disabled={loading}>
            {editingWordId != null ? "Cập nhật từ" : "Thêm từ"}
          </button>
          <button
            type="button"
            onClick={() => {
              setWordForm({ type: "NOUN" });
              setEditingWordId(null);
            }}
          >
            {editingWordId != null ? "Hủy sửa" : "Reset"}
          </button>
        </form>
      </section>
    </div>
  );

  const renderFlashCardsTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Flashcard theo topic</h3>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            <button
              type="button"
              onClick={() => {
                const fallback = selectedTopic?.id;
                if (fallback) {
                  onFlashTopicChange(String(fallback));
                } else {
                  notify("Chọn topic ở tab Topic trước (View)");
                }
              }}
              style={{ padding: "0.5rem 1rem" }}
            >
              Dùng topic đang chọn (tab Topic)
            </button>
            <button
              type="button"
              onClick={() => {
                loadTopicsForSelect().catch((e) => notify(toUiError(e)));
              }}
              disabled={loading}
              style={{ padding: "0.5rem 1rem" }}
            >
              Tải lại danh sách topic
            </button>
          </div>
        </div>
        <div className="list-toolbar">
          <label style={{ flex: 1, display: "grid", gap: "6px", minWidth: 0 }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn topic
            </span>
            <select
              value={lessonSelectValue(flashTopicId)}
              onChange={(e) => onFlashTopicChange(e.target.value)}
            >
              <option value="">-- Chọn topic --</option>
              {topicsForSelect.map((t) => (
                <option key={t.id} value={t.id}>
                  #{t.id} · {t.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tìm nhanh flashcard..."
            value={flashQuery}
            onChange={(e) => setFlashQuery(e.target.value)}
          />
          <span>Hiện {filteredFlashCards.length}/{flashCards.length} mục</span>
        </div>
        <div className="list-toolbar" style={{ gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
          <button
            type="button"
            className="danger"
            disabled={loading || selectedFlashCardIds.size === 0 || !isPositiveInt(flashTopicId)}
            onClick={() => void bulkDeleteSelectedFlashCards()}
          >
            Xóa đã chọn ({selectedFlashCardIds.size})
          </button>
        </div>
        <div className="list-toolbar" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
          <span className="note" style={{ fontWeight: 600 }}>
            Import Excel (.xlsx / .xls) — vào topic đang chọn; không cần cột ảnh (backend gắn ảnh placeholder)
          </span>
          <span className="note">
            Hàng 1 (header): word · pronunciation · meaning · type (NOUN | VERB | ADJECTIVE) · example
          </span>
          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={loading || !isPositiveInt(flashTopicId)}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                void runFlashExcelImport(file);
              }
              e.target.value = "";
            }}
          />
        </div>
        <table>
          <thead>
            <tr>
              <th style={{ width: 44 }}>
                <input
                  type="checkbox"
                  title="Chọn tất cả trong danh sách lọc"
                  checked={allFilteredFlashCardsSelected}
                  onChange={toggleSelectAllFilteredFlashCards}
                  disabled={loading || filteredFlashCards.length === 0}
                />
              </th>
              <th>ID</th>
              <th>Word</th>
              <th>Meaning</th>
              <th>Example</th>
              <th>Image</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filteredFlashCards.map((c) => (
              <tr key={c.id}>
                <td>
                  <input
                    type="checkbox"
                    checked={selectedFlashCardIds.has(c.id)}
                    onChange={() => toggleFlashCardRowSelected(c.id)}
                    disabled={loading}
                  />
                </td>
                <td>{c.id}</td>
                <td>{c.word || "-"}</td>
                <td>{c.meaning || "-"}</td>
                <td>{c.example}</td>
                <td>{c.imageUrl ? <a href={c.imageUrl}>link</a> : "-"}</td>
                <td style={{ whiteSpace: "nowrap" }}>
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      setEditingFlashId(c.id);
                      setFlashForm({
                        dictionaryWordId: null,
                        word: c.word || "",
                        pronunciation: c.pronunciation || "",
                        meaning: c.meaning || "",
                        type: "NOUN",
                        example: c.example,
                        imageUrl: c.imageUrl || "",
                        imageName: null,
                        imageSize: null,
                      });
                      notify("Đang sửa flashcard — ảnh tùy chọn khi cập nhật");
                    }}
                  >
                    Sửa
                  </button>{" "}
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      if (!isPositiveInt(flashTopicId)) {
                        notify("Chọn topic");
                        return;
                      }
                      if (!confirm(`Xóa flashcard #${c.id}?`)) {
                        return;
                      }
                      setLoading(true);
                      deleteTopicFlashCard(config, Number(flashTopicId), c.id)
                        .then(() => {
                          notify("Đã xóa flashcard");
                          setSelectedFlashCardIds((prev) => {
                            const next = new Set(prev);
                            next.delete(c.id);
                            return next;
                          });
                          if (editingFlashId === c.id) {
                            setEditingFlashId(null);
                            setFlashForm({ type: "NOUN", dictionaryWordId: null });
                          }
                          return loadFlashCardsForTopic(Number(flashTopicId));
                        })
                        .catch((err) => notify(`Xóa flashcard thất bại: ${toUiError(err)}`))
                        .finally(() => setLoading(false));
                    }}
                  >
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h3>{editingFlashId != null ? "Sửa flashcard" : "Tạo flashcard (thuộc topic)"}</h3>
        <form onSubmit={onFlashSubmit} className="form-grid">
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chọn topic
            </span>
            <select
              value={lessonSelectValue(flashTopicId)}
              onChange={(e) => onFlashTopicChange(e.target.value)}
              required
            >
              <option value="">-- Chọn topic --</option>
              {topicsForSelect.map((t) => (
                <option key={t.id} value={t.id}>
                  #{t.id} · {t.name}
                </option>
              ))}
            </select>
          </label>
          <input
            type="number"
            min={1}
            placeholder="Dictionary Word ID (nếu có)"
            value={flashForm.dictionaryWordId || ""}
            onChange={(e) =>
              setFlashForm((p) => ({ ...p, dictionaryWordId: e.target.value ? Number(e.target.value) : null }))
            }
          />
          <input
            placeholder="Word (bỏ qua nếu dùng Word ID)"
            value={flashForm.word || ""}
            onChange={(e) => setFlashForm((p) => ({ ...p, word: e.target.value }))}
            maxLength={50}
          />
          <input
            placeholder="Pronunciation"
            value={flashForm.pronunciation || ""}
            onChange={(e) => setFlashForm((p) => ({ ...p, pronunciation: e.target.value }))}
            maxLength={50}
          />
          <input
            placeholder="Meaning"
            value={flashForm.meaning || ""}
            onChange={(e) => setFlashForm((p) => ({ ...p, meaning: e.target.value }))}
            maxLength={255}
          />
          <select
            value={flashForm.type || "NOUN"}
            onChange={(e) => setFlashForm((p) => ({ ...p, type: e.target.value as Word["type"] }))}
          >
            <option value="NOUN">NOUN</option>
            <option value="VERB">VERB</option>
            <option value="ADJECTIVE">ADJECTIVE</option>
          </select>
          <input
            placeholder="Example"
            value={flashForm.example || ""}
            onChange={(e) => setFlashForm((p) => ({ ...p, example: e.target.value }))}
            maxLength={255}
            required
          />
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              {editingFlashId != null
                ? "Ảnh flashcard (tùy chọn khi sửa — upload Cloudinary)"
                : "Ảnh flashcard (upload lên Cloudinary)"}
            </span>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) {
                  onUploadForFlashcard(file).catch((err: Error) => notify(err.message));
                }
                e.target.value = "";
              }}
            />
          </label>
          {flashForm.imageUrl && (
            <img className="image-preview" src={flashForm.imageUrl} alt="Flashcard preview" />
          )}
          <button type="submit" disabled={loading}>
            {editingFlashId != null ? "Cập nhật flashcard" : "Tạo flashcard"}
          </button>
          <button
            type="button"
            onClick={() => {
              setFlashForm({ type: "NOUN", dictionaryWordId: null });
              setEditingFlashId(null);
            }}
          >
            {editingFlashId != null ? "Hủy sửa" : "Reset"}
          </button>
        </form>
      </section>
    </div>
  );

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
        notify(`Lỗi phân trang: ${toUiError(error)}`);
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
        notify("Cập nhật Topic thành công");
      } else {
        await createTopic(config, payload);
        notify("Tạo Topic thành công");
      }
      setTopicForm({});
      setSelectedTopic(null);
      await refreshAll(true);
    } catch (error) {
      notify(`Lỗi lưu Topic: ${toUiError(error)}`);
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
        notify("Cập nhật Lesson thành công");
      } else {
        await createLesson(config, payload);
        notify("Tạo Lesson thành công");
      }
      setLessonForm({ type: "LISTENING" });
      setSelectedLesson(null);
      await refreshAll();
    } catch (error) {
      notify(`Lỗi lưu Lesson: ${toUiError(error)}`);
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
        notify("Cập nhật Scenario thành công");
      } else {
        await createScenario(config, payload);
        notify("Tạo Scenario thành công");
      }
      setScenarioForm({});
      setSelectedScenario(null);
      await refreshAll();
    } catch (error) {
      notify(`Lỗi lưu Scenario: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onBulkImportTopics = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Đang đọc file Topic...");
      const rows = await readImportRows(file);
      const dataRows = rows.filter((row) => row.length >= 2 && row[0].toLowerCase() !== "name");
      const topicReqs = dataRows
        .map((row) => ({
          name: trimOrEmpty(row[0]),
          description: trimOrEmpty(row[1]),
          imageUrl: trimOrEmpty(row[2]) || null,
        }))
        .filter((row) => row.name && row.description);
      const skipped = dataRows.length - topicReqs.length;
      
      if (topicReqs.length === 0) {
        notify("Không có dữ liệu hợp lệ trong file");
        return;
      }

      const confirmed = window.confirm(
        `Đã đọc được ${topicReqs.length} topic hợp lệ${skipped > 0 ? `, bỏ qua ${skipped} dòng` : ""}.\nBạn có muốn xác nhận lưu vào hệ thống không?`
      );
      if (!confirmed) {
        notify("Đã hủy lưu topic import.");
        return;
      }

      notify(`Đang import ${topicReqs.length} topic...`);
      const result = await bulkImportTopics(config, topicReqs);
      notify(`Đã import ${result.length} topic${skipped > 0 ? `, bỏ qua ${skipped} dòng không hợp lệ` : ""}`);
      await refreshAll();
    } catch (error) {
      notify(`Import Topic thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onBulkImportLessons = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Đang đọc file Lesson...");
      if (topicState.totalElements === 0) {
        notify("Không có Topic nào. Hãy import/tạo Topic trước");
        return;
      }
      const rows = await readImportRows(file);
      const dataRows = rows.filter((row) => row.length >= 3 && row[0].toLowerCase() !== "topicid");
      const lessonReqs = dataRows
        .map((row) => ({
          topicId: Number(row[0]),
          title: trimOrEmpty(row[1]),
          type: (row[2] || "LISTENING") as Lesson["type"],
          imageUrl: trimOrEmpty(row[3]) || null,
          parentId: row[4] ? Number(row[4]) : null,
        }))
        .filter((row) => isPositiveInt(row.topicId) && row.title.length > 0);
      const skipped = dataRows.length - lessonReqs.length;

      if (lessonReqs.length === 0) {
        notify("Không có dữ liệu hợp lệ trong file");
        return;
      }

      const confirmed = window.confirm(
        `Đã đọc được ${lessonReqs.length} lesson hợp lệ${skipped > 0 ? `, bỏ qua ${skipped} dòng` : ""}.\nBạn có muốn xác nhận lưu vào hệ thống không?`
      );
      if (!confirmed) {
        notify("Đã hủy lưu lesson import.");
        return;
      }

      notify(`Đang import ${lessonReqs.length} lesson...`);
      const result = await bulkImportLessons(config, lessonReqs);
      notify(`Đã import ${result.length} lesson${skipped > 0 ? `, bỏ qua ${skipped} dòng không hợp lệ` : ""}`);
      await refreshAll(true);
    } catch (error) {
      notify(`Import Lesson thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onBulkImportScenarios = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Đang đọc file Scenario...");
      if (lessonState.totalElements === 0) {
        notify("Không có Lesson nào. Hãy import/tạo Lesson trước");
        return;
      }
      const allLessons = await listAllLessons(config);
      const lessonTopicMap = new Map<number, number>();
      allLessons.forEach((lesson) => {
        if (isPositiveInt(lesson.id) && isPositiveInt(lesson.topicId)) {
          lessonTopicMap.set(Number(lesson.id), Number(lesson.topicId));
        }
      });
      const rows = await readImportRows(file);
      const dataRows = rows.filter((row) => row.length >= 6 && row[0].toLowerCase() !== "topicid");
      const scenarioReqs = dataRows
        .map((row) => {
          const lessonId = Number(row[1]);
          const mappedTopicId = lessonTopicMap.get(lessonId);
          const rawTopicId = Number(row[0]);
          return {
            topicId: mappedTopicId ?? rawTopicId,
            lessonId,
            title: trimOrEmpty(row[2]),
            description: trimOrEmpty(row[3]),
            aiRole: trimOrEmpty(row[4]),
            userRole: trimOrEmpty(row[5]),
            tasks: trimOrEmpty(row[6]) || null,
            openningMessage: trimOrEmpty(row[7]) || null,
            suggestion: trimOrEmpty(row[8]) || null,
            translation: trimOrEmpty(row[9]) || null,
          };
        })
        .filter(
          (row) =>
            isPositiveInt(row.topicId) &&
            isPositiveInt(row.lessonId) &&
            row.title.length > 0 &&
            row.description.length > 0 &&
            row.aiRole.length > 0 &&
            row.userRole.length > 0
        );
      const skipped = dataRows.length - scenarioReqs.length;

      if (scenarioReqs.length === 0) {
        notify("Không có dữ liệu hợp lệ trong file");
        return;
      }

      const confirmed = window.confirm(
        `Đã đọc được ${scenarioReqs.length} scenario hợp lệ${skipped > 0 ? `, bỏ qua ${skipped} dòng` : ""}.\nBạn có muốn xác nhận lưu vào hệ thống không?`
      );
      if (!confirmed) {
        notify("Đã hủy lưu scenario import.");
        return;
      }

      notify(`Đang import ${scenarioReqs.length} scenario...`);
      const result = await bulkImportScenarios(config, scenarioReqs);
      notify(`Đã import ${result.length} scenario${skipped > 0 ? `, bỏ qua ${skipped} dòng không hợp lệ` : ""}`);
      await refreshAll(true);
    } catch (error) {
      notify(`Import Scenario thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onUploadForTopic = async (file: File) => {
    requireAuth();
    try {
      const upload = await uploadImage(config, file, "elearn/topics");
      setTopicForm((prev) => ({ ...prev, imageUrl: upload.secureUrl }));
      notify(`Đã upload ảnh Topic: ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload ảnh Topic thất bại: ${toUiError(error)}`);
    }
  };

  const onUploadForLesson = async (file: File) => {
    requireAuth();
    try {
      const upload = await uploadImage(config, file, "elearn/lessons");
      setLessonForm((prev) => ({ ...prev, imageUrl: upload.secureUrl }));
      notify(`Đã upload ảnh Lesson: ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload ảnh Lesson thất bại: ${toUiError(error)}`);
    }
  };

  const onUploadForFlashcard = async (file: File) => {
    requireAuth();
    try {
      const upload = await uploadImage(config, file, "elearn/flashcards");
      setFlashForm((prev) => ({
        ...prev,
        imageUrl: upload.secureUrl,
        imageName: upload.originalFilename ?? file.name,
        imageSize: upload.bytes ?? file.size,
      }));
      notify(`Đã upload ảnh flashcard (Cloudinary): ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload ảnh flashcard thất bại: ${toUiError(error)}`);
    }
  };

  const onDeleteTopic = async (topic: Topic) => {
    if (!window.confirm(`Bạn chắc chắn muốn xóa Topic #${topic.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteTopic(config, topic.id);
      if (selectedTopic?.id === topic.id) {
        setSelectedTopic(null);
      }
      notify("Xóa Topic thành công");
      await refreshAll();
    } catch (error) {
      notify(`Xóa Topic thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onDeleteLesson = async (lesson: Lesson) => {
    if (!window.confirm(`Bạn chắc chắn muốn xóa Lesson #${lesson.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteLesson(config, lesson.id);
      if (selectedLesson?.id === lesson.id) {
        setSelectedLesson(null);
      }
      notify("Xóa Lesson thành công");
      await refreshAll();
    } catch (error) {
      notify(`Xóa Lesson thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onDeleteScenario = async (scenario: Scenario) => {
    if (!window.confirm(`Bạn chắc chắn muốn xóa Scenario #${scenario.id}?`)) {
      return;
    }
    setLoading(true);
    try {
      await deleteScenario(config, scenario.id);
      if (selectedScenario?.id === scenario.id) {
        setSelectedScenario(null);
      }
      notify("Xóa Scenario thành công");
      await refreshAll();
    } catch (error) {
      notify(`Xóa Scenario thất bại: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const renderTopicTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Danh sách Topic</h3>
          <button onClick={() => { setSelectedTopic(null); setTopicForm({}); }} style={{ padding: "0.5rem 1rem" }}>
            + Tạo Topic
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tìm nhanh Topic trên trang hiện tại..."
            value={topicQuery}
            onChange={(e) => setTopicQuery(e.target.value)}
          />
          <span>
            Hiện {filteredTopics.length}/{topics.length} mục
          </span>
        </div>
        <div style={{ display: "flex", gap: "0.5rem", marginBottom: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => topicImportInputRef.current?.click()}
          >
            📥 Import CSV
          </button>
          <input
            ref={topicImportInputRef}
            type="file"
            accept=".csv,.txt,.xlsx,.xls"
            style={{ display: "none" }}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                onBulkImportTopics(file).catch(() => {});
                e.target.value = "";
              }
            }}
          />
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => downloadCsvTemplate("topics-import-template.csv", TOPIC_TEMPLATE_CSV)}
          >
            Template CSV
          </button>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Ten</th>
              <th>Mô tả</th>
              <th>Ảnh</th>
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
              <h4>Chi tiết Topic #{selectedTopic.id}</h4>
              <p><strong>Tên:</strong> {selectedTopic.name}</p>
              <p><strong>Mô tả:</strong> {selectedTopic.description}</p>
              <p>
                <strong>Ảnh:</strong>{" "}
                {selectedTopic.imageUrl ? <a href={selectedTopic.imageUrl}>Mở ảnh</a> : "Không có"}
              </p>
              {selectedTopic.imageUrl && (
                <img className="image-preview" src={selectedTopic.imageUrl} alt={selectedTopic.name} />
              )}
            </div>
          </div>
        ) : (
          <>
            <h3>{topicForm.id ? "Cập nhật Topic" : "Tạo Topic"}</h3>
            <form onSubmit={onTopicSubmit} className="form-grid">
              <input
                placeholder="Tên topic"
                value={topicForm.name || ""}
                onChange={(e) => setTopicForm((prev) => ({ ...prev, name: e.target.value }))}
                maxLength={255}
                required
              />
              <textarea
                placeholder="Mô tả"
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
                {topicForm.id ? "Lưu cập nhật" : "Tạo mới"}
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
          <h3 style={{ margin: 0 }}>Danh sách Lesson</h3>
          <button onClick={() => { setSelectedLesson(null); setLessonForm({ type: "LISTENING" }); }} style={{ padding: "0.5rem 1rem" }}>
            + Tạo Lesson
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tìm nhanh Lesson trên trang hiện tại..."
            value={lessonQuery}
            onChange={(e) => setLessonQuery(e.target.value)}
          />
          <span>
            Hiện {filteredLessons.length}/{lessons.length} mục
          </span>
        </div>
        <div style={{ display: "flex", gap: "0.5rem", marginBottom: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => lessonImportInputRef.current?.click()}
          >
            📥 Import CSV
          </button>
          <input
            ref={lessonImportInputRef}
            type="file"
            accept=".csv,.txt,.xlsx,.xls"
            style={{ display: "none" }}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                onBulkImportLessons(file).catch(() => {});
                e.target.value = "";
              }
            }}
          />
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => downloadCsvTemplate("lessons-import-template.csv", buildLessonTemplateCsv())}
          >
            Template CSV
          </button>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Topic</th>
              <th>Title</th>
              <th>Type</th>
              <th>Ảnh</th>
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
              <h4>Chi tiết Lesson #{selectedLesson.id}</h4>
              <p><strong>Topic ID:</strong> {selectedLesson.topicId}</p>
              <p><strong>Tiêu đề:</strong> {selectedLesson.title}</p>
              <p><strong>Loại:</strong> {selectedLesson.type}</p>
              <p><strong>Parent:</strong> {selectedLesson.parentId || "Không có"}</p>
              <p>
                <strong>Ảnh:</strong>{" "}
                {selectedLesson.imageUrl ? <a href={selectedLesson.imageUrl}>Mở ảnh</a> : "Không có"}
              </p>
              {selectedLesson.imageUrl && (
                <img className="image-preview" src={selectedLesson.imageUrl} alt={selectedLesson.title} />
              )}
            </div>
          </div>
        ) : (
          <>
            <h3>{lessonForm.id ? "Cập nhật Lesson" : "Tạo Lesson"}</h3>
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
                placeholder="Tiêu đề"
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
                {lessonForm.id ? "Lưu cập nhật" : "Tạo mới"}
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
          <h3 style={{ margin: 0 }}>Danh sách Scenario</h3>
          <button onClick={() => { setSelectedScenario(null); setScenarioForm({}); }} style={{ padding: "0.5rem 1rem" }}>
            + Tạo Scenario
          </button>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tìm nhanh Scenario trên trang hiện tại..."
            value={scenarioQuery}
            onChange={(e) => setScenarioQuery(e.target.value)}
          />
          <span>
            Hiện {filteredScenarios.length}/{scenarios.length} mục
          </span>
        </div>
        <div style={{ display: "flex", gap: "0.5rem", marginBottom: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => scenarioImportInputRef.current?.click()}
          >
            📥 Import CSV
          </button>
          <input
            ref={scenarioImportInputRef}
            type="file"
            accept=".csv,.txt,.xlsx,.xls"
            style={{ display: "none" }}
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) {
                onBulkImportScenarios(file).catch(() => {});
                e.target.value = "";
              }
            }}
          />
          <button
            type="button"
            style={{ padding: "0.4rem 0.8rem", fontSize: "0.9rem" }}
            onClick={() => downloadCsvTemplate("scenarios-import-template.csv", buildScenarioTemplateCsv())}
          >
            Template CSV
          </button>
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
              <h4>Chi tiết Scenario #{selectedScenario.id}</h4>
              <p><strong>Topic ID:</strong> {selectedScenario.topicId}</p>
              <p><strong>Lesson ID:</strong> {selectedScenario.lessonId}</p>
              <p><strong>Tiêu đề:</strong> {selectedScenario.title}</p>
              <p><strong>Mô tả:</strong> {selectedScenario.description}</p>
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
            <h3>{scenarioForm.id ? "Cập nhật Scenario" : "Tạo Scenario"}</h3>
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
                placeholder="Tiêu đề"
                value={scenarioForm.title || ""}
                onChange={(e) => setScenarioForm((prev) => ({ ...prev, title: e.target.value }))}
                maxLength={255}
                required
              />
              <textarea
                placeholder="Mô tả"
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
                {scenarioForm.id ? "Lưu cập nhật" : "Tạo mới"}
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
            <h1>Đăng nhập hệ thống quản trị</h1>
            <p className="note">Hệ thống quản trị Topic, Lesson, Scenario</p>
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
              {loading ? "Đang đăng nhập..." : "Đăng nhập"}
            </button>
          </form>
          <p className="note">{message || "Nhập thông tin tài khoản admin BE để đăng nhập."}</p>
        </section>
      </div>
    );
  }

  return (
    <div className="dashboard-layout">
      <aside className="sidebar">
        <div className="brand">E-Learn Admin</div>
        <p className="side-label">Quản trị nội dung</p>
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
          <button className={tab === "words" ? "active" : ""} onClick={() => setTab("words")}>
            Words
          </button>
          <button className={tab === "flashcards" ? "active" : ""} onClick={() => setTab("flashcards")}>
            Flashcards
          </button>
          <button
            className={tab === "questions-import" ? "active" : ""}
            onClick={() => setTab("questions-import")}
          >
            Question Import
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

        <p className="note status-note" style={{ marginTop: "0.5rem" }}>{message}</p>

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
        {tab === "words" && renderWordsTab()}
        {tab === "flashcards" && renderFlashCardsTab()}
        {tab === "questions-import" && (
          <QuestionImportTab
            apiBase={API_BASE}
            token={token}
            defaultLessonId={selectedLesson?.id || null}
            onMessage={notify}
          />
        )}

      </main>
    </div>
  );
}
