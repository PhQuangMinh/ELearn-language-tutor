# 🚀 ELearn Language Tutor

<p align="left">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-blue" alt="Architecture">
  <img src="https://img.shields.io/badge/DI-Hilt-0F9D58" alt="Hilt">
  <img src="https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black" alt="Firebase">
</p>

Ứng dụng Android hỗ trợ học tiếng Anh theo hướng ngắn gọn và có AI đồng hành. Từ onboarding, bài học, từ vựng, flashcard, speaking.

---

## 🎯 1. Chức năng

ELearn Language Tutor tập trung vào 3 mục tiêu:

- ⚡ Học nhanh mỗi ngày với các bài ngắn, câu hỏi tương tác, theo dõi tiến độ.
- 📚 Mở rộng vốn từ với Vocabulary + Flashcard và phát âm trực tiếp bằng TTS Android.
- 🎙️ Luyện giao tiếp với AI conversation, hỗ trợ nghe/ghi âm/cải thiện câu trả lời.
- 🖼️ Nhắc người dùng học hàng ngày
- 🏠 Theo dõi Streak của người dùng

---

## 🌟 2) Tính năng cụ thể

### 🧭 Onboarding & Authentication

- 🖼️ Onboarding.
- 📨 Đăng nhập, đăng ký, quên mật khẩu, nhập mã xác thực, đặt mật khẩu mới.

### 🏠 Home & Learning Journey

- 📊 Home tổng quan tiến độ học.
- 🧩 Điều hướng theo tab: `Lesson`, `Vocabulary`, `Speaking`, `Profile`.

### 🧠 Lesson Module

- ✅ Hỗ trợ nhiều dạng câu hỏi:
  - Multiple choice
  - Arrange words
  - Speaking assessment
- 💬 Màn hình feedback đúng/sai.
- 🏁 Kết thúc lesson có màn tổng kết điểm và trạng thái hoàn thành.
- 🔥 Theo dõi streak học tập (duy trì chuỗi ngày học).

### 📖 Vocabulary Module

- 🗂️ Theo chủ đề (`Topic`), phân loại theo từ loại (`Noun`, `Verb`, `Adjective`).
- 🔢 Phân trang danh sách từ.
- 📝 Mỗi card hiển thị:
  - Word
  - Meaning
  - Pronunciation
  - Definition
  - Example
- 🃏 Chế độ Flashcard để học và ôn từ nhanh.
- 🔊 Nút loa phát âm từng từ bằng **TextToSpeech của Android**.

### 🤖 Speaking Module (AI)

- 💡 AI conversation theo ngữ cảnh bài học.
- 🎤 Gửi tin nhắn text hoặc nói qua micro.
- 🔁 AI phản hồi theo hội thoại, có nút nghe lại bằng TTS.
- ✨ Tính năng “Improve” để AI gợi ý câu diễn đạt tốt hơn + giải thích.

### 👤 Profile & Settings

- 🛠️ Xem/chỉnh thông tin cá nhân.
- 🖼️ Đổi avatar.
- 🔑 Đổi mật khẩu.
- 📈 Theo dõi streak hiện tại và streak dài nhất.
- 🌙 Toggle Dark mode.

---

## 🏗️ 3) Kiến trúc & Tổ chức mã nguồn

Project áp dụng tư duy tách lớp rõ ràng:

- 🎨 **Presentation**: Fragment/Adapter/UI state
- 🧩 **Domain**: Model, UseCase, Repository contract
- 💾 **Data**: DTO, Mapper, Repository implementation, Remote/Local source

### 🖼️ Sơ đồ kiến trúc hệ thống

```text
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐ │
│  │ Fragment /  │◄──►│  ViewModel  │◄──►│    UI State     │ │
│  │  Activity   │    │             │    │ (StateFlow)     │ │
│  └─────────────┘    └─────────────┘    └─────────────────┘ │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                       Domain Layer                          │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐ │
│  │  Use Cases  │◄──►│   Entities  │◄──►│  Repositories   │ │
│  └─────────────┘    └─────────────┘    └─────────────────┘ │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                        Data Layer                           │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐ │
│  │ Repository  │◄──►│   Remote    │◄──►│      Local      │ │
│  │    Impl     │    │ Data Source │    │   Data Source   │ │
│  └─────────────┘    └─────────────┘    └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 📊 Sơ đồ luồng dữ liệu

```text
┌───────────┐    ┌───────────┐    ┌───────────┐    ┌───────────┐
│           │    │           │    │           │    │           │
│  UI Layer │───►│ ViewModel │───►│ Use Cases │───►│Repository │
│           │    │           │    │           │    │           │
└───────────┘    └───────────┘    └───────────┘    └───────────┘
       ▲                                                  │
       │                                                  │
       │                                                  ▼
       │                                           ┌───────────┐
       │                                           │           │
       └───────────────────────────────────────────┤  Sources  │
                                                   │           │
                                                   └───────────┘
```

## 🛠️ 4) Công nghệ chính

- 🟣 **Kotlin**
- 📦 **Android Jetpack** (ViewModel, Navigation Component, ViewBinding, Lifecycle, RecyclerView)
- 🔄 **Coroutines + Flow** cho async/state stream
- 🧪 **Hilt (DI)**
- 🌐 **Retrofit/OkHttp/Gson** cho networking
- 🔥 **Firebase** cho thông báo nhắc nhở học tập
- 🔊 **TextToSpeech Android** cho phát âm
- 🌐 **AI** cho phần speak
---

## 🗂️ 5) Cấu trúc thư mục (chi tiết)

```text
ELearn-language-tutor/
├── Mobile/                                   # Android project root
│   ├── app/                                  # Module ứng dụng chính
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/nhom2/elearnlanguage/
│   │   │   │   │   ├── data/                # DTO, mapper, repository impl, local/remote source
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   ├── mapper/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── source/
│   │   │   │   │   ├── di/                  # Hilt modules (Network, Repository...)
│   │   │   │   │   ├── domain/              # Model, Repository contract, UseCase
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── repository/
│   │   │   │   │   │   └── usecase/
│   │   │   │   │   ├── presentation/ui/     # UI layer
│   │   │   │   │   │   ├── auth/            # Login, Register, Forgot password
│   │   │   │   │   │   ├── onboarding/      # Onboarding flow
│   │   │   │   │   │   ├── main_app/
│   │   │   │   │   │   │   ├── home/        # Home + tab (lesson/vocabulary/speaking/profile)
│   │   │   │   │   │   │   ├── lesson/      # Lesson flow + adapters
│   │   │   │   │   │   │   ├── lessonlist/
│   │   │   │   │   │   │   └── vocabulary/  # Flashcard & vocabulary detail
│   │   │   │   │   ├── push/                # Firebase Messaging service
│   │   │   │   │   ├── receiver/            # Broadcast receiver (alarm/reminder)
│   │   │   │   │   └── ELearnApplication.kt # Application class
│   │   │   │   ├── res/                     # layouts, drawables, values, navigation...
│   │   │   │   └── AndroidManifest.xml
│   │   │   ├── androidTest/                 # Instrumentation tests
│   │   │   └── test/                        # Unit tests
│   │   ├── build.gradle.kts                 # Build config module app
│   │   ├── google-services.json             # Firebase config
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts                     # Build config cấp project (Mobile)
│   └── settings.gradle.kts                  # Include modules
├── README.md
```

---

## ▶️ 6) Hướng dẫn chạy project

### 📌 Yêu cầu

- Android Studio mới (khuyến nghị Hedgehog trở lên)
- JDK 17 (hoặc theo cấu hình Gradle project)
- Android SDK phù hợp với `compileSdk` trong module `Mobile/app`

### 🧭 Các bước

1. Clone repository:

```bash
git clone https://github.com/<username>/ELearn-language-tutor.git
cd ELearn-language-tutor
```

2. Mở thư mục `Mobile` bằng Android Studio.
3. Chờ Gradle sync xong.
4. Chạy app trên emulator hoặc thiết bị thật.

---

## 🧪 7) User flow gợi ý để demo đầy đủ chức năng

1. Mở app -> đi qua onboarding.
2. Đăng nhập/đăng ký.
3. Vào Home và mở Lesson:
   - làm bài
   - xem feedback
   - hoàn thành lesson
4. Vào Vocabulary:
   - đổi tab Noun/Verb/Adjective
   - chuyển trang
   - bấm loa nghe phát âm
   - chuyển sang Flashcard
5. Vào Speaking:
   - chat với AI
   - nghe lại phản hồi
   - thử tính năng Improve
6. Vào Profile:
   - xem streak
   - đổi thông tin
   - bật/tắt dark mode

---