export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
};

export type PageResponse<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
};

export type Topic = {
  id: number;
  name: string;
  description: string;
  imageUrl?: string | null;
};

export type Lesson = {
  id: number;
  topicId: number;
  title: string;
  type: "LISTENING" | "PRACTICING" | "VOCABULARY";
  imageUrl?: string | null;
  parentId?: number | null;
};

export type Word = {
  id: number;
  word: string;
  pronunciation: string;
  meaning: string;
  type: "VERB" | "ADJECTIVE" | "NOUN";
};

export type FlashCard = {
  id: number;
  word: string | null;
  pronunciation: string | null;
  meaning: string | null;
  example: string;
  imageUrl: string | null;
};

export type Scenario = {
  id: number;
  topicId: number;
  lessonId: number;
  title: string;
  description: string;
  aiRole: string;
  userRole: string;
  tasks?: string | null;
  openningMessage?: string | null;
  suggestion?: string | null;
  translation?: string | null;
};

export type CloudinaryUpload = {
  secureUrl: string;
  publicId?: string;
  format?: string;
  width?: number;
  height?: number;
  bytes?: number;
  originalFilename?: string;
};
