# Quy ước làm việc với Git trong nhóm:

#### Lưu ý:

- Hạn chế tối đa việc `push -f` do reset nhánh hoặc rebase mà liên quan tới các nhánh quan trọng (dev / main / ...). Nếu cần thì phải báo trước để tránh conflict

## Tên nhánh:
`<Username Github> / <Linear issue code>`

VD: `NgNhatThanh/NHO-10`


## Commit message:

```
<type>[<Linear issue code>]: <title>
scopes: <scopes>

break:
- ... 
change:
- ...
new:
- ...
```

Trong đó:

- `type`: vai trò của commit, thuộc các loại sau
  + `fix`: fixbug
  + `feat`: bổ sung tính năng, yêu cầu mới
  + `refactor`: sửa đổi cấu trúc code, không ảnh hưởng logic / hiệu năng
  + `perf`: chỉnh sửa liên quan tới cải tiến hiệu năng
  + `chore`: sửa chút ít, không ảnh hưởng tới luồng / hiệu năng
  + `doc`: bổ sung / điều chỉnh tài liệu

- `title`: tóm tắt ngắn gọn và nội dung trong commit, có thể lấy tiêu đề issue trên Linear

- `scopes`: các commitId của commit có liên quan tới commit này, nếu không có thì bỏ qua
  + VD: commit dùng để fixbug do lỗi của commit trước đó

- `break`: tóm tắt các thay đổi liên quan mà không tương thích ngược

- `change`: tóm tắt các sửa đổi logic, vẫn đảm bảo tương thích ngược

- `new`: tóm tắt các bổ sung


VD:
``` git
feat(NHO-13): Khởi tạo csdl và các lớp thực thể

new:
- Add liquibase cho migration
- Tạo các lớp thực thể
change:
- Dùng .env thay vì hardcode trong application.yaml
- Đổi sang mariadb thay cho mysql
- Bỏ một số field không cần thiết của user
```