# Quy ước cho dev Backend trong nhóm:

- Hạn chế hardcode properties / params, nên cho vào application, hoặc tối thiểu là Constant
- Không log sql cho JPA để hạn chế log dài, nếu cần bật thì chỉnh config ở local, nhưng không push lên git
- Tốt nhất nên dùng class DTO thay cho record, vì có thể trong tương lai sẽ cần set field cho nó

### Liquibase:

- Mọi thay đổi liên quan tới DB cần update changelog vào để mọi người cùng up-to-date nếu muốn chạy local

- Nếu đã up changelog lên mà muốn có sửa đổi, tạo changelog mới chứ không sửa changelog cũ (vì nếu sửa file sẽ cho ra sai mã băm của file và không chạy được đâu)

- Nơi đặt file changelog: resources/db/changelog/ddl

**Quy ước đặt tên file changelog:**

`<Ngày tạo file>-<author>-<id>`

  + Trong đó:
    + Ngày tạo file: định dạng yyyy-mm-dd
    + Author: VD: thanhnn
    + Id: số định danh, bắt đầu từ 01, dùng để phân biệt nếu một người up nhiều changelog trong cùng ngày
VD: `2026-02-07-thanhnn-01.sql`

**Quy ước nội dung changelog:**

``` sql
-- liquibase formatted sql

-- changeset <author>:<Tóm tắt mục đích changelog>
-- Bổ sung comment nếu cần cho dễ hiểu
<Nội dung sql cần chạy>
```

VD:
``` sql
-- liquibase formatted sql

-- changeset thanhnn:add field abc to table def
alter table def
    add abc int null;
```