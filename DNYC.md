# Định nghĩa yêu cầu App học tiếng Anh

## 1. Thông tin người dùng

### 1.1. Thông tin cơ bản người dùng
**Chức năng**
- Lưu trữ thông tin định danh người học:
  - User ID
  - Tên hiển thị
  - Email / phương thức đăng nhập
  - Trình độ tiếng Anh (tự chọn hoặc đánh giá ban đầu)
- Cho phép chỉnh sửa thông tin cá nhân cơ bản
- Đồng bộ thông tin người dùng trên nhiều thiết bị

**Mục đích sử dụng**
- Thống kê:
  - Theo dõi tiến độ học
  - Tổng hợp báo cáo học tập

---

### 1.2. Streak học tập

**Định nghĩa**
- Streak = số ngày liên tiếp người dùng có hoạt động luyện tiếng Anh hợp lệ
- Hoạt động hợp lệ bao gồm:
  - Phiên nói tiếng Anh với AI
  - Thời lượng speaking ≥ *X* giây (config được)

**Chức năng**
- Tự động kiểm tra hoạt động mỗi ngày
- Tăng streak khi:
  - Trong ngày có ít nhất 1 hoạt động hợp lệ
- Reset streak khi:
  - Người dùng không có hoạt động hợp lệ trong hơn 1 ngày liên tiếp
- Lưu lịch sử streak theo ngày

**Hiển thị**
- Streak hiện tại
- Streak cao nhất từng đạt được

---

### 1.3. Đếm số lần “mở miệng nói tiếng Anh”

**Định nghĩa**
- Một lần nói = một phiên nói tiếng Anh được ghi nhận:
  - Speech-to-text thành công
  - Thời lượng ≥ *Y* giây (config được)

**Chức năng**
- Tự động tăng counter khi bắt đầu phiên nói hợp lệ
- Không tính:
  - Phiên bị hủy giữa chừng
  - Phiên nói quá ngắn

**Mục đích**
- Đánh giá mức độ chủ động, “dám nói”
- Là chỉ số khích lệ tinh thần cho người học mới

---

### 1.4. Đếm số giờ nói tiếng Anh

**Chức năng**
- Ghi nhận chính xác tổng thời gian nói tiếng Anh:
  - Theo từng phiên
  - Cộng dồn theo ngày / tuần / tháng
- Chỉ tính thời gian người dùng thực sự nói (không tính thời gian chờ)

**Hiển thị**
- Tổng thời gian nói (giây / phút / giờ)
- Biểu đồ tiến độ theo thời gian

---

## 2. Nói chuyện với AI (AI Speaking Module)

### 2.1. Mục tiêu chức năng
- Luyện nói tiếng Anh trong các tình huống thực tế
- Giảm áp lực nói bằng AI thay vì người thật
- Học theo tiến trình rõ ràng, không bị “nói lan man”

**Vai trò của AI**
- Người đối thoại: phản hồi tự nhiên như người thật
- Người hướng dẫn: gợi ý khi người học bí
- Người sửa lỗi: chỉ ra lỗi phát âm, từ vựng, ngữ pháp

---

### 2.2. Cấu trúc nội dung tổng thể

- **Topic** (Chủ đề lớn)
  - Ví dụ: Travel, Work, Daily Life
- **Lesson** (Bài học cụ thể)
  - Ví dụ: At the airport, Job interview
- **Scenario** (Tình huống nói chuyện)
  - Bối cảnh cụ thể
  - Vai trò rõ ràng cho AI & người học

**Chức năng**
- Cho phép mở rộng nội dung theo cấp độ
- Có thể tái sử dụng scenario cho nhiều trình độ khác nhau

---

### 2.3. Luồng chức năng nói chuyện với AI

1. Người dùng chọn **Topic → Lesson**
2. Hệ thống hiển thị:
   - Mô tả bối cảnh
   - Mục tiêu bài học
   - Vai trò của AI & người học
3. Người dùng bắt đầu nói
4. AI xử lý:
   - Speech-to-text
   - Hiểu ngữ cảnh hội thoại
   - Phản hồi phù hợp
5. Trong quá trình nói:
   - Gợi ý câu trả lời khi người dùng im lặng
   - Cho phép nhắc lại / nói chậm
6. Kết thúc lesson:
   - Tổng kết nội dung đã nói
   - Feedback:
     - Phát âm
     - Từ vựng
     - Độ trôi chảy
   - Lưu kết quả vào hồ sơ người dùng

---

## 3. Module đọc tin tức

### 3.1. Mục tiêu chức năng
- Giúp người học tiếp xúc tiếng Anh đời thực hằng ngày
- Kết hợp kỹ năng:
  - Đọc
  - Hiểu
  - Nói
  - Từ vựng

---

### 3.2. Phạm vi chức năng

**Nguồn dữ liệu**
- Lấy tin tức tiếng Anh từ bên thứ ba:
  - News API
  - RSS feeds

**Chức năng đọc**
- Hiển thị nội dung bài báo
- Highlight từ vựng quan trọng
- Hỗ trợ text-to-speech

**Hỗ trợ AI**
- Tóm tắt bài báo theo trình độ
- Giải thích:
  - Từ vựng khó
  - Cấu trúc câu
- Gợi ý câu hỏi thảo luận / nói lại nội dung

---

## 4. Module học từ vựng

### 4.1. Mục tiêu chức năng
- Giúp người học ghi nhớ và sử dụng từ vựng chủ động
- Học từ:
  - Ngữ cảnh thực tế
  - Lặp lại có kiểm soát

---

### 4.2. Phạm vi chức năng

**Nguồn từ vựng**
- Từ vựng có sẵn theo chủ đề
- Từ vựng trích xuất từ:
  - AI speaking
  - Bài đọc tin tức

**Collection**
- Cho phép tạo nhiều bộ sưu tập từ vựng
- Thêm / xóa / chỉnh sửa từ

**Flashcard**
- Mặt trước:
  - Từ vựng
  - Phát âm
- Mặt sau:
  - Nghĩa
  - Ví dụ
- Hỗ trợ:
  - Nghe phát âm
  - Đánh dấu đã nhớ / chưa nhớ

**Theo dõi tiến độ**
- Số từ đã học
- Tỷ lệ nhớ từ
- Lịch ôn tập gợi ý
## 5. Tính năng đề xuất(Optional)
- Sẽ cho người dùng nhập 1 chủ đề đoạn văn bất kỳ
- Sử dụng AI để sinh ra các câu liên quan để chủ đề đó. Người dùng sẽ nhập vào bằng tiếng anh -> sử dụng AI để kiểm tra tính đúng đắn của câu dịch
- Link test: https://english.datpmt.com/sentence/beginner/personal-communication