import { FormEvent, useCallback, useEffect, useMemo, useRef, useState } from "react";
import * as XLSX from "xlsx";
import {
  bulkImportLessons,
  bulkImportScenarios,
  bulkImportTopics,
  createLesson,
  createScenario,
  createTopic,
  createLessonFlashCard,
  createLessonWord,
  deleteLesson,
  deleteLessonFlashCard,
  deleteLessonWord,
  deleteScenario,
  deleteTopic,
  importFlashCardsExcel,
  importWordsExcel,
  listAllLessons,
  listLessonFlashCards,
  listLessonWords,
  listLessons,
  listScenarios,
  listTopics,
  login,
  updateLesson,
  updateLessonFlashCard,
  updateLessonWord,
  updateScenario,
  updateTopic,
  uploadImage,
} from "./api";
import type { FlashCard, Lesson, PageResponse, Scenario, Topic, Word } from "./types";

type Tab = "topics" | "lessons" | "scenarios" | "words" | "flashcards";

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

  const [wordLessonId, setWordLessonId] = useState<number | "">("");
  const [words, setWords] = useState<Word[]>([]);
  const [wordForm, setWordForm] = useState<Partial<Omit<Word, "id">>>({ type: "NOUN" });
  const [editingWordId, setEditingWordId] = useState<number | null>(null);
  const [wordQuery, setWordQuery] = useState("");

  const [flashLessonId, setFlashLessonId] = useState<number | "">("");
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
  const [lessonsForSelect, setLessonsForSelect] = useState<Lesson[]>([]);

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
      `${sampleTopicId},${sampleLessonId},Welcome Dialogue,"Basic greeting and self-introduction",Tutor,Learner,"Introduce yourself","Hello! Nice to meet you.","Use short sentences","Ban dang tap gioi thieu ban than"`,
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
        const all = await listAllLessons(config);
        setLessonsForSelect(all);
      } catch {
        /* dropdown refresh optional */
      }
      if (!silent) {
        notify("Da tai du lieu Topic, Lesson, Scenario");
      }
    } catch (error) {
      notify(`Khong the tai du lieu: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  }, [config, token, topicState.page, lessonState.page, scenarioState.page]);

  const loadWordsForLesson = useCallback(
    async (lessonId: number) => {
      requireAuth();
      const data = await listLessonWords(config, lessonId);
      setWords(data);
    },
    [config, token]
  );

  const loadFlashCardsForLesson = useCallback(
    async (lessonId: number) => {
      requireAuth();
      const data = await listLessonFlashCards(config, lessonId);
      setFlashCards(data);
    },
    [config, token]
  );

  const loadLessonsForSelect = useCallback(async () => {
    requireAuth();
    const all = await listAllLessons(config);
    setLessonsForSelect(all);
  }, [config, token]);

  useEffect(() => {
    if (!token) {
      return;
    }
    refreshAll().catch((error) => notify(`Khong the tai du lieu: ${toUiError(error)}`));
    // Only trigger initial load after login/token restore.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    if (!token || (tab !== "words" && tab !== "flashcards")) {
      return;
    }
    loadLessonsForSelect().catch((e) => notify(`Khong tai danh sach lesson: ${toUiError(e)}`));
  }, [token, tab, loadLessonsForSelect]);

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
    setWords([]);
    setFlashCards([]);
    setWordLessonId("");
    setFlashLessonId("");
    setEditingWordId(null);
    setEditingFlashId(null);
    setLessonsForSelect([]);
    localStorage.removeItem("admin-token");
    notify("Da dang xuat");
  };

  const validateWordForm = (form: Partial<Omit<Word, "id">>): string | null => {
    if (isBlank(form.word)) return "Word khong duoc de trong";
    if (exceeds(form.word, 50)) return "Word toi da 50 ky tu";
    if (isBlank(form.pronunciation)) return "Pronunciation khong duoc de trong";
    if (exceeds(form.pronunciation, 50)) return "Pronunciation toi da 50 ky tu";
    if (isBlank(form.meaning)) return "Meaning khong duoc de trong";
    if (exceeds(form.meaning, 255)) return "Meaning toi da 255 ky tu";
    if (!WORD_TYPES.includes((form.type || "") as Word["type"])) return "Word type khong hop le";
    return null;
  };

  const validateFlashForm = (isEdit: boolean): string | null => {
    if (!isPositiveInt(flashLessonId)) return "Chon lesson";
    if (isBlank(flashForm.example)) return "Example khong duoc de trong";
    if (exceeds(flashForm.example, 255)) return "Example toi da 255 ky tu";
    if (!isEdit) {
      if (isBlank(flashForm.imageUrl)) return "Hay upload anh flashcard (Cloudinary)";
      if (exceeds(flashForm.imageUrl, 1000)) return "Image URL toi da 1000 ky tu";
      if (!ensureUrlIfProvided(flashForm.imageUrl || "")) return "Anh upload khong hop le";
    } else if (!isBlank(flashForm.imageUrl)) {
      if (exceeds(flashForm.imageUrl, 1000)) return "Image URL toi da 1000 ky tu";
      if (!ensureUrlIfProvided(flashForm.imageUrl || "")) return "Anh upload khong hop le";
    }

    const hasWordId = isPositiveInt(flashForm.dictionaryWordId);
    if (!hasWordId) {
      const err = validateWordForm({
        word: flashForm.word || "",
        pronunciation: flashForm.pronunciation || "",
        meaning: flashForm.meaning || "",
        type: (flashForm.type || "NOUN") as Word["type"],
      });
      if (err) return `Flashcard word: ${err}`;
    }
    return null;
  };

  const onWordSubmit = async (event: FormEvent) => {
    event.preventDefault();
    requireAuth();
    if (!isPositiveInt(wordLessonId)) {
      notify("Chon lesson");
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
        await updateLessonWord(config, Number(wordLessonId), editingWordId, payload);
        notify("Da cap nhat word");
        setEditingWordId(null);
      } else {
        await createLessonWord(config, Number(wordLessonId), payload);
        notify("Da them word vao lesson");
      }
      setWordForm({ type: "NOUN" });
      await loadWordsForLesson(Number(wordLessonId));
    } catch (error) {
      notify(`Luu word that bai: ${toUiError(error)}`);
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
      const lessonIdNum = Number(flashLessonId);
      if (editingFlashId != null) {
        const img = trimOrEmpty(flashForm.imageUrl);
        await updateLessonFlashCard(config, lessonIdNum, editingFlashId, {
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
        notify("Da cap nhat flashcard");
        setEditingFlashId(null);
      } else {
        await createLessonFlashCard(config, lessonIdNum, {
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
        notify("Da tao flashcard theo lesson");
      }
      setFlashForm({ type: "NOUN", dictionaryWordId: null });
      await loadFlashCardsForLesson(lessonIdNum);
    } catch (error) {
      notify(`Luu flashcard that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const lessonSelectValue = (id: number | "") => (id === "" ? "" : String(id));

  const onWordLessonChange = (value: string) => {
    const id = value === "" ? "" : Number(value);
    setWordLessonId(id);
    if (isPositiveInt(id)) {
      setLoading(true);
      loadWordsForLesson(Number(id))
        .then(() => notify("Da tai vocabulary"))
        .catch((e) => notify(toUiError(e)))
        .finally(() => setLoading(false));
    } else {
      setWords([]);
    }
  };

  const onFlashLessonChange = (value: string) => {
    const id = value === "" ? "" : Number(value);
    setFlashLessonId(id);
    if (isPositiveInt(id)) {
      setLoading(true);
      loadFlashCardsForLesson(Number(id))
        .then(() => notify("Da tai flashcards"))
        .catch((e) => notify(toUiError(e)))
        .finally(() => setLoading(false));
    } else {
      setFlashCards([]);
    }
  };

  const runWordExcelImport = async (file: File) => {
    requireAuth();
    if (!isPositiveInt(wordLessonId)) {
      notify("Chon lesson truoc khi import (lesson duoc ap dung cho toan bo dong trong file)");
      return;
    }
    setLoading(true);
    try {
      const r = await importWordsExcel(config, Number(wordLessonId), file);
      const hint =
        r.errors?.length > 0 ? ` Vi du loi: ${r.errors.slice(0, 3).join(" | ")}` : "";
      notify(`Import vocabulary: ${r.successCount} thanh cong, ${r.errorCount} loi.${hint}`);
      if (isPositiveInt(wordLessonId)) {
        await loadWordsForLesson(Number(wordLessonId));
      }
    } catch (error) {
      notify(`Import vocabulary that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const runFlashExcelImport = async (file: File) => {
    requireAuth();
    if (!isPositiveInt(flashLessonId)) {
      notify("Chon lesson truoc khi import (lesson duoc ap dung cho toan bo dong trong file)");
      return;
    }
    setLoading(true);
    try {
      const r = await importFlashCardsExcel(config, Number(flashLessonId), file);
      const hint =
        r.errors?.length > 0 ? ` Vi du loi: ${r.errors.slice(0, 3).join(" | ")}` : "";
      notify(`Import flashcard: ${r.successCount} thanh cong, ${r.errorCount} loi.${hint}`);
      if (isPositiveInt(flashLessonId)) {
        await loadFlashCardsForLesson(Number(flashLessonId));
      }
    } catch (error) {
      notify(`Import flashcard that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const renderWordsTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Vocabulary theo Lesson</h3>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            <button
              type="button"
              onClick={() => {
                const fallback = selectedLesson?.id;
                if (fallback) {
                  onWordLessonChange(String(fallback));
                } else {
                  notify("Chon lesson o tab Lesson truoc (View)");
                }
              }}
              style={{ padding: "0.5rem 1rem" }}
            >
              Dung lesson dang chon (Lesson tab)
            </button>
            <button
              type="button"
              onClick={() => {
                loadLessonsForSelect().catch((e) => notify(toUiError(e)));
              }}
              disabled={loading}
              style={{ padding: "0.5rem 1rem" }}
            >
              Tai lai danh sach lesson
            </button>
          </div>
        </div>
        <div className="list-toolbar">
          <label style={{ flex: 1, display: "grid", gap: "6px", minWidth: 0 }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chon lesson
            </span>
            <select
              value={lessonSelectValue(wordLessonId)}
              onChange={(e) => onWordLessonChange(e.target.value)}
            >
              <option value="">-- Chon lesson --</option>
              {lessonsForSelect.map((l) => (
                <option key={l.id} value={l.id}>
                  #{l.id} · {l.title} · {l.type} · Topic {l.topicId}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tim nhanh word..."
            value={wordQuery}
            onChange={(e) => setWordQuery(e.target.value)}
          />
          <span>Hien {filteredWords.length}/{words.length} muc</span>
        </div>
        <div className="list-toolbar" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
          <span className="note" style={{ fontWeight: 600 }}>
            Import Excel (.xlsx / .xls) — moi dong mot word vao lesson dang chon o tren
          </span>
          <span className="note">
            Hang 1 (header): word · pronunciation · meaning · type (NOUN | VERB | ADJECTIVE)
          </span>
          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={loading || !isPositiveInt(wordLessonId)}
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
              <th>ID</th>
              <th>Word</th>
              <th>Pronunciation</th>
              <th>Meaning</th>
              <th>Type</th>
              <th>Thao tac</th>
            </tr>
          </thead>
          <tbody>
            {filteredWords.map((w) => (
              <tr key={w.id}>
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
                      notify("Dang sua word — bam Cap nhat word de luu");
                    }}
                  >
                    Sua
                  </button>{" "}
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      if (!isPositiveInt(wordLessonId)) {
                        notify("Chon lesson");
                        return;
                      }
                      if (
                        !confirm(
                          `Xoa word "${w.word}"? Cac flashcard gan word nay trong lesson cung se bi xoa.`
                        )
                      ) {
                        return;
                      }
                      setLoading(true);
                      deleteLessonWord(config, Number(wordLessonId), w.id)
                        .then(() => {
                          notify("Da xoa word");
                          if (editingWordId === w.id) {
                            setEditingWordId(null);
                            setWordForm({ type: "NOUN" });
                          }
                          return loadWordsForLesson(Number(wordLessonId));
                        })
                        .catch((err) => notify(`Xoa word that bai: ${toUiError(err)}`))
                        .finally(() => setLoading(false));
                    }}
                  >
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h3>{editingWordId != null ? "Sua word trong Lesson" : "Them Word vao Lesson"}</h3>
        <form onSubmit={onWordSubmit} className="form-grid">
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chon lesson
            </span>
            <select
              value={lessonSelectValue(wordLessonId)}
              onChange={(e) => onWordLessonChange(e.target.value)}
              required
            >
              <option value="">-- Chon lesson --</option>
              {lessonsForSelect.map((l) => (
                <option key={l.id} value={l.id}>
                  #{l.id} · {l.title} · {l.type} · Topic {l.topicId}
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
            {editingWordId != null ? "Cap nhat word" : "Them word"}
          </button>
          <button
            type="button"
            onClick={() => {
              setWordForm({ type: "NOUN" });
              setEditingWordId(null);
            }}
          >
            {editingWordId != null ? "Huy sua" : "Reset"}
          </button>
        </form>
      </section>
    </div>
  );

  const renderFlashCardsTab = () => (
    <div className="panel-grid">
      <section className="panel">
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1rem" }}>
          <h3 style={{ margin: 0 }}>Flashcards theo Lesson</h3>
          <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
            <button
              type="button"
              onClick={() => {
                const fallback = selectedLesson?.id;
                if (fallback) {
                  onFlashLessonChange(String(fallback));
                } else {
                  notify("Chon lesson o tab Lesson truoc (View)");
                }
              }}
              style={{ padding: "0.5rem 1rem" }}
            >
              Dung lesson dang chon (Lesson tab)
            </button>
            <button
              type="button"
              onClick={() => {
                loadLessonsForSelect().catch((e) => notify(toUiError(e)));
              }}
              disabled={loading}
              style={{ padding: "0.5rem 1rem" }}
            >
              Tai lai danh sach lesson
            </button>
          </div>
        </div>
        <div className="list-toolbar">
          <label style={{ flex: 1, display: "grid", gap: "6px", minWidth: 0 }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chon lesson
            </span>
            <select
              value={lessonSelectValue(flashLessonId)}
              onChange={(e) => onFlashLessonChange(e.target.value)}
            >
              <option value="">-- Chon lesson --</option>
              {lessonsForSelect.map((l) => (
                <option key={l.id} value={l.id}>
                  #{l.id} · {l.title} · {l.type} · Topic {l.topicId}
                </option>
              ))}
            </select>
          </label>
        </div>
        <div className="list-toolbar">
          <input
            placeholder="Tim nhanh flashcard..."
            value={flashQuery}
            onChange={(e) => setFlashQuery(e.target.value)}
          />
          <span>Hien {filteredFlashCards.length}/{flashCards.length} muc</span>
        </div>
        <div className="list-toolbar" style={{ flexDirection: "column", alignItems: "stretch", gap: "8px" }}>
          <span className="note" style={{ fontWeight: 600 }}>
            Import Excel (.xlsx / .xls) — vao lesson dang chon; khong can cot anh (backend gan anh placeholder)
          </span>
          <span className="note">
            Hang 1 (header): word · pronunciation · meaning · type (NOUN | VERB | ADJECTIVE) · example
          </span>
          <input
            type="file"
            accept=".xlsx,.xls"
            disabled={loading || !isPositiveInt(flashLessonId)}
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
              <th>ID</th>
              <th>Word</th>
              <th>Meaning</th>
              <th>Example</th>
              <th>Image</th>
              <th>Thao tac</th>
            </tr>
          </thead>
          <tbody>
            {filteredFlashCards.map((c) => (
              <tr key={c.id}>
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
                      notify("Dang sua flashcard — anh tuy chon khi cap nhat");
                    }}
                  >
                    Sua
                  </button>{" "}
                  <button
                    type="button"
                    disabled={loading}
                    onClick={() => {
                      if (!isPositiveInt(flashLessonId)) {
                        notify("Chon lesson");
                        return;
                      }
                      if (!confirm(`Xoa flashcard #${c.id}?`)) {
                        return;
                      }
                      setLoading(true);
                      deleteLessonFlashCard(config, Number(flashLessonId), c.id)
                        .then(() => {
                          notify("Da xoa flashcard");
                          if (editingFlashId === c.id) {
                            setEditingFlashId(null);
                            setFlashForm({ type: "NOUN", dictionaryWordId: null });
                          }
                          return loadFlashCardsForLesson(Number(flashLessonId));
                        })
                        .catch((err) => notify(`Xoa flashcard that bai: ${toUiError(err)}`))
                        .finally(() => setLoading(false));
                    }}
                  >
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <section className="panel">
        <h3>{editingFlashId != null ? "Sua Flashcard" : "Tao Flashcard (thuoc Lesson)"}</h3>
        <form onSubmit={onFlashSubmit} className="form-grid">
          <label style={{ display: "grid", gap: "6px" }}>
            <span className="note" style={{ fontWeight: 600 }}>
              Chon lesson
            </span>
            <select
              value={lessonSelectValue(flashLessonId)}
              onChange={(e) => onFlashLessonChange(e.target.value)}
              required
            >
              <option value="">-- Chon lesson --</option>
              {lessonsForSelect.map((l) => (
                <option key={l.id} value={l.id}>
                  #{l.id} · {l.title} · {l.type} · Topic {l.topicId}
                </option>
              ))}
            </select>
          </label>
          <input
            type="number"
            min={1}
            placeholder="Dictionary Word ID (neu co)"
            value={flashForm.dictionaryWordId || ""}
            onChange={(e) =>
              setFlashForm((p) => ({ ...p, dictionaryWordId: e.target.value ? Number(e.target.value) : null }))
            }
          />
          <input
            placeholder="Word (bo qua neu dung Word ID)"
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
                ? "Anh flashcard (tuy chon khi sua — upload Cloudinary)"
                : "Anh flashcard (upload len Cloudinary)"}
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
            {editingFlashId != null ? "Cap nhat flashcard" : "Tao flashcard"}
          </button>
          <button
            type="button"
            onClick={() => {
              setFlashForm({ type: "NOUN", dictionaryWordId: null });
              setEditingFlashId(null);
            }}
          >
            {editingFlashId != null ? "Huy sua" : "Reset"}
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
      await refreshAll(true);
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

  const onBulkImportTopics = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Dang doc file Topic...");
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
        notify("Khong co du lieu hop le trong file");
        return;
      }

      notify(`Dang import ${topicReqs.length} topic...`);
      const result = await bulkImportTopics(config, topicReqs);
      notify(`Da import ${result.length} topic${skipped > 0 ? `, bo qua ${skipped} dong khong hop le` : ""}`);
      await refreshAll();
    } catch (error) {
      notify(`Import Topic that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onBulkImportLessons = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Dang doc file Lesson...");
      if (topicState.totalElements === 0) {
        notify("Khong co Topic nao. Hay import/create Topic truoc");
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
        notify("Khong co du lieu hop le trong file");
        return;
      }

      notify(`Dang import ${lessonReqs.length} lesson...`);
      const result = await bulkImportLessons(config, lessonReqs);
      notify(`Da import ${result.length} lesson${skipped > 0 ? `, bo qua ${skipped} dong khong hop le` : ""}`);
      await refreshAll(true);
    } catch (error) {
      notify(`Import Lesson that bai: ${toUiError(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const onBulkImportScenarios = async (file: File) => {
    requireAuth();
    setLoading(true);
    try {
      notify("Dang doc file Scenario...");
      if (lessonState.totalElements === 0) {
        notify("Khong co Lesson nao. Hay import/create Lesson truoc");
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
        notify("Khong co du lieu hop le trong file");
        return;
      }

      notify(`Dang import ${scenarioReqs.length} scenario...`);
      const result = await bulkImportScenarios(config, scenarioReqs);
      notify(`Da import ${result.length} scenario${skipped > 0 ? `, bo qua ${skipped} dong khong hop le` : ""}`);
      await refreshAll(true);
    } catch (error) {
      notify(`Import Scenario that bai: ${toUiError(error)}`);
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
      notify(`Da upload anh flashcard (Cloudinary): ${upload.publicId || "ok"}`);
    } catch (error) {
      notify(`Upload anh flashcard that bai: ${toUiError(error)}`);
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
          <button className={tab === "words" ? "active" : ""} onClick={() => setTab("words")}>
            Words
          </button>
          <button className={tab === "flashcards" ? "active" : ""} onClick={() => setTab("flashcards")}>
            Flashcards
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

      </main>
    </div>
  );
}
