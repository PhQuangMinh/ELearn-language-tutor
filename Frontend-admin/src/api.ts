import type {
  ApiResponse,
  CloudinaryUpload,
  FlashCard,
  ImportResult,
  Lesson,
  QuestionImportCommitResponse,
  PageResponse,
  QuestionDetail,
  QuestionImportPreviewResponse,
  Scenario,
  Topic,
  Word,
} from "./types";

type Credentials = {
  email: string;
  password: string;
};

type SessionConfig = {
  apiBase: string;
  token: string;
};

const safeFetch = async (url: string, init?: RequestInit): Promise<Response> => {
  try {
    return await fetch(url, init);
  } catch {
    throw new Error(
      `Không kết nối được backend tại ${url}. Kiểm tra backend đã chạy đúng port và CORS đã mở cho frontend.`
    );
  }
};

const parseJson = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }
  return response.json() as Promise<T>;
};

const withHeaders = (token: string, extra?: HeadersInit): HeadersInit => ({
  ...(extra || {}),
  ...(token ? { Authorization: `Bearer ${token}` } : {}),
});

export const login = async (apiBase: string, credentials: Credentials): Promise<string> => {
  const response = await safeFetch(`${apiBase}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(credentials),
  });
  const data = await parseJson<ApiResponse<{ token: string }>>(response);
  const token = data.data?.token;
  if (!token) {
    throw new Error("Không lấy được token");
  }
  return token;
};

const getPaged = async <T>(config: SessionConfig, path: string): Promise<PageResponse<T>> => {
  const response = await safeFetch(`${config.apiBase}${path}`, {
    headers: withHeaders(config.token),
  });
  const data = await parseJson<ApiResponse<PageResponse<T>>>(response);
  return data.data;
};

const postJson = async <T>(config: SessionConfig, path: string, body: unknown): Promise<T> => {
  const response = await safeFetch(`${config.apiBase}${path}`, {
    method: "POST",
    headers: withHeaders(config.token, { "Content-Type": "application/json" }),
    body: JSON.stringify(body),
  });
  const data = await parseJson<ApiResponse<T>>(response);
  return data.data;
};

const putJson = async <T>(config: SessionConfig, path: string, body: unknown): Promise<T> => {
  const response = await safeFetch(`${config.apiBase}${path}`, {
    method: "PUT",
    headers: withHeaders(config.token, { "Content-Type": "application/json" }),
    body: JSON.stringify(body),
  });
  const data = await parseJson<ApiResponse<T>>(response);
  return data.data;
};

const del = async (config: SessionConfig, path: string): Promise<void> => {
  const response = await safeFetch(`${config.apiBase}${path}`, {
    method: "DELETE",
    headers: withHeaders(config.token),
  });
  await parseJson<ApiResponse<unknown>>(response);
};

export const uploadImage = async (
  config: SessionConfig,
  file: File,
  folder = "elearn/admin"
): Promise<CloudinaryUpload> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("folder", folder);

  const response = await safeFetch(`${config.apiBase}/api/admin/media/images`, {
    method: "POST",
    headers: withHeaders(config.token),
    body: formData,
  });

  const data = await parseJson<ApiResponse<CloudinaryUpload>>(response);
  return data.data;
};

export const listTopics = (config: SessionConfig, page: number, size: number) =>
  getPaged<Topic>(config, `/api/admin/topics?page=${page}&size=${size}&sort=id,desc`);

/** Loads every topic page for admin dropdowns. */
export const listAllTopics = async (config: SessionConfig): Promise<Topic[]> => {
  const pageSize = 200;
  const first = await listTopics(config, 0, pageSize);
  const all: Topic[] = [...first.content];
  for (let p = 1; p < first.totalPages; p++) {
    const next = await listTopics(config, p, pageSize);
    all.push(...next.content);
  }
  return all;
};

export const createTopic = (config: SessionConfig, payload: Omit<Topic, "id">) =>
  postJson<Topic>(config, "/api/admin/topics", payload);

export const updateTopic = (config: SessionConfig, id: number, payload: Partial<Omit<Topic, "id">>) =>
  putJson<Topic>(config, `/api/admin/topics/${id}`, payload);

export const deleteTopic = (config: SessionConfig, id: number) => del(config, `/api/admin/topics/${id}`);

export const bulkImportTopics = (config: SessionConfig, topics: any[]) =>
  postJson<Topic[]>(config, "/api/admin/topics/import", topics);

export const listLessons = (config: SessionConfig, page: number, size: number) =>
  getPaged<Lesson>(config, `/api/admin/lessons?page=${page}&size=${size}&sort=id,desc`);

/** Loads every lesson page for admin dropdowns (topic + title). */
export const listAllLessons = async (config: SessionConfig): Promise<Lesson[]> => {
  const pageSize = 200;
  const first = await listLessons(config, 0, pageSize);
  const all: Lesson[] = [...first.content];
  for (let p = 1; p < first.totalPages; p += 1) {
    const next = await listLessons(config, p, pageSize);
    all.push(...next.content);
  }
  return all;
};

export const createLesson = (config: SessionConfig, payload: Omit<Lesson, "id">) =>
  postJson<Lesson>(config, "/api/admin/lessons", payload);

export const updateLesson = (
  config: SessionConfig,
  id: number,
  payload: Partial<Omit<Lesson, "id">>
) => putJson<Lesson>(config, `/api/admin/lessons/${id}`, payload);

export const deleteLesson = (config: SessionConfig, id: number) =>
  del(config, `/api/admin/lessons/${id}`);

export const bulkImportLessons = (config: SessionConfig, lessons: any[]) =>
  postJson<Lesson[]>(config, "/api/admin/lessons/import", lessons);

export const listScenarios = (config: SessionConfig, page: number, size: number) =>
  getPaged<Scenario>(config, `/api/admin/scenarios?page=${page}&size=${size}&sort=id,desc`);

export const createScenario = (config: SessionConfig, payload: Omit<Scenario, "id">) =>
  postJson<Scenario>(config, "/api/admin/scenarios", payload);

export const updateScenario = (
  config: SessionConfig,
  id: number,
  payload: Partial<Omit<Scenario, "id">>
) => putJson<Scenario>(config, `/api/admin/scenarios/${id}`, payload);

export const deleteScenario = (config: SessionConfig, id: number) =>
  del(config, `/api/admin/scenarios/${id}`);

export const bulkImportScenarios = (config: SessionConfig, scenarios: any[]) =>
  postJson<Scenario[]>(config, "/api/admin/scenarios/import", scenarios);

export const listTopicWords = (config: SessionConfig, topicId: number) => {
  const response = safeFetch(`${config.apiBase}/api/admin/topics/${topicId}/words`, {
    headers: withHeaders(config.token),
  });
  return response.then((r) => parseJson<ApiResponse<Word[]>>(r)).then((d) => d.data);
};

export const createTopicWord = (config: SessionConfig, topicId: number, payload: Omit<Word, "id">) =>
  postJson<Word>(config, `/api/admin/topics/${topicId}/words`, payload);

export const updateTopicWord = (
  config: SessionConfig,
  topicId: number,
  wordId: number,
  payload: Omit<Word, "id">
) => putJson<Word>(config, `/api/admin/topics/${topicId}/words/${wordId}`, payload);

export const deleteTopicWord = (config: SessionConfig, topicId: number, wordId: number) =>
  del(config, `/api/admin/topics/${topicId}/words/${wordId}`);

export const listTopicFlashCards = (config: SessionConfig, topicId: number) => {
  const response = safeFetch(`${config.apiBase}/api/admin/topics/${topicId}/flashcards`, {
    headers: withHeaders(config.token),
  });
  return response.then((r) => parseJson<ApiResponse<FlashCard[]>>(r)).then((d) => d.data);
};

export const createTopicFlashCard = (
  config: SessionConfig,
  topicId: number,
  payload: {
    dictionaryWordId?: number | null;
    word?: string | null;
    pronunciation?: string | null;
    meaning?: string | null;
    type?: Word["type"] | null;
    example: string;
    imageUrl: string;
    imageName?: string | null;
    imageSize?: number | null;
  }
) => postJson<FlashCard>(config, `/api/admin/topics/${topicId}/flashcards`, payload);

export const updateTopicFlashCard = (
  config: SessionConfig,
  topicId: number,
  flashCardId: number,
  payload: {
    dictionaryWordId?: number | null;
    word?: string | null;
    pronunciation?: string | null;
    meaning?: string | null;
    type?: Word["type"] | null;
    example?: string | null;
    imageUrl?: string | null;
    imageName?: string | null;
    imageSize?: number | null;
  }
) => putJson<FlashCard>(config, `/api/admin/topics/${topicId}/flashcards/${flashCardId}`, payload);

export const deleteTopicFlashCard = (config: SessionConfig, topicId: number, flashCardId: number) =>
  del(config, `/api/admin/topics/${topicId}/flashcards/${flashCardId}`);

const postMultipart = async <T>(config: SessionConfig, path: string, file: File): Promise<T> => {
  const formData = new FormData();
  formData.append("file", file);
  const response = await safeFetch(`${config.apiBase}${path}`, {
    method: "POST",
    headers: withHeaders(config.token),
    body: formData,
  });
  const data = await parseJson<ApiResponse<T>>(response);
  return data.data;
};

export const importWordsExcel = (config: SessionConfig, topicId: number, file: File) =>
  postMultipart<ImportResult>(
    config,
    `/api/admin/import/words?topicId=${encodeURIComponent(String(topicId))}`,
    file
  );

export const importFlashCardsExcel = (config: SessionConfig, topicId: number, file: File) =>
  postMultipart<ImportResult>(
    config,
    `/api/admin/import/flashcards?topicId=${encodeURIComponent(String(topicId))}`,
    file
  );

export const importQuestionsExcel = (config: SessionConfig, lessonId: number, file: File) =>
  postMultipart<ImportResult>(
    config,
    `/api/admin/import/questions?lessonId=${encodeURIComponent(String(lessonId))}`,
    file
  );

export const previewQuestionsExcel = (config: SessionConfig, file: File) =>
  postMultipart<QuestionImportPreviewResponse>(config, "/api/admin/import/questions/preview", file);

export const commitQuestionsImport = (
  config: SessionConfig,
  payload: QuestionImportPreviewResponse["questions"]
) => postJson<QuestionImportCommitResponse>(config, "/api/admin/import/questions/commit", payload);

export const listQuestionsByLesson = async (
  config: SessionConfig,
  lessonId: number
): Promise<QuestionDetail[]> => {
  const response = await safeFetch(`${config.apiBase}/api/lessons/${lessonId}/questions`, {
    headers: withHeaders(config.token),
  });
  return parseJson<QuestionDetail[]>(response);
};
