# Flashcard API

Tai lieu nay mo ta API Flashcard hien tai cho mobile.

- Ngay cap nhat: 2026-03-06
- Base path: `/api/flashcards`

---

## Trang thai hien tai ve topic

API flashcard da ho tro loc theo topic qua query param `topicId`.

- Khong truyen `topicId`: lay full danh sach
- Co truyen `topicId`: chi lay flashcard thuoc topic do

---

## Response wrapper chung: `ApiResponse<T>`

Tat ca API flashcard tra theo format:

```json
{
  "success": true,
  "message": "Thanh cong",
  "data": {},
  "errorCode": null
}
```

---

### GET `/api/flashcards` - Lay danh sach flashcard

API nay dung khi mo man hinh danh sach flashcard de lay du lieu (co hoac khong loc theo topic).

## Endpoint

- **Method**: `GET`
- **Path**: `/api/flashcards`

## Query parameters

- `topicId` (`number`, optional): ID topic can loc flashcard.

## Request body

- **Khong co**

## Response

- **200 OK**
- **Content-Type**: `application/json`
- **Body**: `ApiResponse<FlashCardResponse[]>`

### Response body (mau)

```json
{
  "success": true,
  "message": "Thanh cong",
  "data": [
    {
      "id": 1,
      "word": "apple",
      "pronunciation": "/ˈæp.əl/",
      "meaning": "qua tao",
      "example": "I eat an apple every day.",
      "imageUrl": "https://example.com/images/apple.png"
    },
    {
      "id": 2,
      "word": "book",
      "pronunciation": "/bʊk/",
      "meaning": "quyen sach",
      "example": "This book is interesting.",
      "imageUrl": "https://example.com/images/book.png"
    }
  ],
  "errorCode": null
}
```

### Vi du goi API

- Lay full: `GET /api/flashcards`
- Lay theo topic: `GET /api/flashcards?topicId=2`

---

### GET `/api/flashcards/{id}` - Lay chi tiet 1 flashcard

API nay dung khi mo man hinh chi tiet flashcard.

## Endpoint

- **Method**: `GET`
- **Path**: `/api/flashcards/{id}`

## Path parameters

- `id` (`number`, required): ID cua flashcard.

## Request body

- **Khong co**

## Response

- **200 OK**
- **Content-Type**: `application/json`
- **Body**: `ApiResponse<FlashCardResponse>`

### Response body (mau)

```json
{
  "success": true,
  "message": "Thanh cong",
  "data": {
    "id": 1,
    "word": "apple",
    "pronunciation": "/ˈæp.əl/",
    "meaning": "qua tao",
    "example": "I eat an apple every day.",
    "imageUrl": "https://example.com/images/apple.png"
  },
  "errorCode": null
}
```

---

## Schema

### `ApiResponse<T>`

- `success` (`boolean`)
- `message` (`string`)
- `data` (`T`)
- `errorCode` (`string | null`)

### `FlashCardResponse`

- `id` (`number`)
- `word` (`string | null`)
- `pronunciation` (`string | null`)
- `meaning` (`string | null`)
- `example` (`string | null`)
- `imageUrl` (`string | null`)

---

## Error format

### Vi du: flashcard khong ton tai

```json
{
  "success": false,
  "message": "Khong tim thay flashcard",
  "data": null,
  "errorCode": "FLASHCARD_NOT_FOUND"
}
```

### HTTP status co the gap

- `400`: Business error (vi du `FLASHCARD_NOT_FOUND`)
- `401`: Unauthorized
- `403`: Forbidden
- `500`: Internal error
