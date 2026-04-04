import type {
  ApiResponse,
  CloudinaryUpload,
  FlashCard,
  ImportResult,
  Lesson,
  PageResponse,
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
      `Khong ket noi duoc backend tai ${url}. Kiem tra backend da chay dung port va CORS da mo cho frontend.`
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
    throw new Error("Khong lay duoc token");
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

export const createTopic = (config: SessionConfig, payload: Omit<Topic, "id">) =>
  postJson<Topic>(config, "/api/admin/topics", payload);

export const updateTopic = (config: SessionConfig, id: number, payload: Partial<Omit<Topic, "id">>) =>
  putJson<Topic>(config, `/api/admin/topics/${id}`, payload);

export const deleteTopic = (config: SessionConfig, id: number) => del(config, `/api/admin/topics/${id}`);

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

export const listLessonWords = (config: SessionConfig, lessonId: number) => {
  const response = safeFetch(`${config.apiBase}/api/admin/lessons/${lessonId}/words`, {
    headers: withHeaders(config.token),
  });
  return response.then((r) => parseJson<ApiResponse<Word[]>>(r)).then((d) => d.data);
};

export const createLessonWord = (config: SessionConfig, lessonId: number, payload: Omit<Word, "id">) =>
  postJson<Word>(config, `/api/admin/lessons/${lessonId}/words`, payload);

export const updateLessonWord = (
  config: SessionConfig,
  lessonId: number,
  wordId: number,
  payload: Omit<Word, "id">
) => putJson<Word>(config, `/api/admin/lessons/${lessonId}/words/${wordId}`, payload);

export const deleteLessonWord = (config: SessionConfig, lessonId: number, wordId: number) =>
  del(config, `/api/admin/lessons/${lessonId}/words/${wordId}`);

export const listLessonFlashCards = (config: SessionConfig, lessonId: number) => {
  const response = safeFetch(`${config.apiBase}/api/admin/lessons/${lessonId}/flashcards`, {
    headers: withHeaders(config.token),
  });
  return response.then((r) => parseJson<ApiResponse<FlashCard[]>>(r)).then((d) => d.data);
};

export const createLessonFlashCard = (
  config: SessionConfig,
  lessonId: number,
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
) => postJson<FlashCard>(config, `/api/admin/lessons/${lessonId}/flashcards`, payload);

export const updateLessonFlashCard = (
  config: SessionConfig,
  lessonId: number,
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
) => putJson<FlashCard>(config, `/api/admin/lessons/${lessonId}/flashcards/${flashCardId}`, payload);

export const deleteLessonFlashCard = (config: SessionConfig, lessonId: number, flashCardId: number) =>
  del(config, `/api/admin/lessons/${lessonId}/flashcards/${flashCardId}`);

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

export const importWordsExcel = (config: SessionConfig, lessonId: number, file: File) =>
  postMultipart<ImportResult>(
    config,
    `/api/admin/import/words?lessonId=${encodeURIComponent(String(lessonId))}`,
    file
  );

export const importFlashCardsExcel = (config: SessionConfig, lessonId: number, file: File) =>
  postMultipart<ImportResult>(
    config,
    `/api/admin/import/flashcards?lessonId=${encodeURIComponent(String(lessonId))}`,
    file
  );
