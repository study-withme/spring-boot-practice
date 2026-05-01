# 댓글(Comment) 도메인

게시글(`Post`)에 달리는 **댓글·대댓글(최대 2단)** 을 **레이어드 아키텍처**로 구현한 문서입니다.  
`Post`·`User`와 **다대일(`@ManyToOne`)** 로 연결되며, 부모 댓글(`parent`)로 트리 깊이를 표현합니다. 구현 코드는 `practice/` 모듈의 `com.test.practice.comment` 패키지에 있습니다.

## 이 구현에서 한 일

- REST API로 **댓글 생성·2단 트리 목록 조회·수정·삭제(소프트 삭제)** 제공 (경로는 게시글 하위: `/api/posts/{postId}/comments`)
- **Spring Data JPA**로 `Comment` 영속화, `deleteTime`이 `null`인 행만 목록에 포함
- 생성 요청에 **Bean Validation**(`@Valid`, `@NotNull`, `@NotBlank`, `@Size` 등) 적용
- 대댓글은 **루트 댓글에만** 달 수 있도록 서비스에서 검증 (3단 이상 방지)
- 수정·삭제 시 **URL의 `postId`와 댓글 소속 일치**, **`userId`가 작성자인지** 검증 (`IllegalArgumentException`)
- 응답은 **루트 댓글만 `replies`에 대댓글 목록**을 담고, 대댓글 노드의 `replies`는 비어 있음 (`depth` 0 / 1)
- 작성 시점 **닉네임 스냅샷**(`nickname`) 저장 — 이후 유저 닉네임 변경과 무관하게 표시용으로 유지

## 패턴과 구현 방식

| 영역 | 선택 | 설명 |
|------|------|------|
| 전체 구조 | Controller → Service → Repository | HTTP·트랜잭션과 도메인 규칙·DB 접근 분리 |
| 영속성 | JPA 엔티티 + `JpaRepository` | `comments` 테이블, `post_id`·`user_id`·`parent_id` FK |
| 삭제 | 소프트 삭제 | `deleteTime` 설정; 조회는 `deleteTime IS NULL` 조건 |
| 타임스탬프 | `@PrePersist` / `@PreUpdate` | `createTime`·`updateTime` 자동 설정 |
| 트리 표현 | 자기 참조 `Comment parent` | `parent == null` → 루트, 한 단계 자식만 허용(서비스 검증) |
| 목록 조립 | 메모리에서 그룹핑 | 게시글별 전체 미삭제 댓글 조회 후 루트 순서 유지·대댓글을 `replies`에 매핑 |
| API 경계 | Request / Response DTO | `CommentCreateRequest`, `CommentUpdateRequest`, `CommentResponse` |
| 실패 처리 | `IllegalArgumentException` + 메시지 | 없는 글·유저·댓글, 게시글 불일치, 작성자 불일치, 3단 답글 시도 등 |

## 왜 이렇게 구현했는지

- **게시글 하위 리소스 URL**: REST 관점에서 “특정 글의 댓글”이 자연스럽고, 목록 조회 시 `postId`가 경로에 고정됩니다.
- **2단만 허용**: UI·권한·알림 복잡도를 제한하고, 무한 중첩 스레드 없이도 일반적인 Q&A·게시판 요구를 충족합니다.
- **닉네임 스냅샷**: 과거 댓글 표시가 현재 프로필과 섞이지 않게 합니다.
- **소프트 삭제**: 게시글 도메인과 동일한 삭제 정책을 맞추어 조회 쿼리를 일관되게 유지합니다.

## 패키지 구조 (요약)

```
practice/src/main/java/com/test/practice/comment/
├── controller/CommentController.java
├── service/CommentService.java
├── repository/CommentRepository.java
├── entity/Comment.java
└── dto/
    ├── CommentCreateRequest.java
    ├── CommentUpdateRequest.java
    └── CommentResponse.java
```

## API 개요

기본 경로: `/api/posts/{postId}/comments`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/posts/{postId}/comments` | 댓글 또는 대댓글 생성 (`CommentCreateRequest` body, `@Valid`) |
| GET | `/api/posts/{postId}/comments` | 해당 게시글의 **2단 트리** 목록 (루트 + 각 루트의 `replies`, 작성순) |
| PUT | `/api/posts/{postId}/comments/{commentId}?userId={userId}` | 수정 (`CommentUpdateRequest` body, 작성자 `userId` 필요) |
| DELETE | `/api/posts/{postId}/comments/{commentId}?userId={userId}` | 소프트 삭제 (`deleteTime` 설정, body 없음) |

## 요청·응답 예시 (JSON)

아래는 **Spring Boot 기본 JSON 직렬화**(Jackson, `LocalDateTime` → ISO-8601 문자열)를 가정한 예시입니다. 필드명은 실제 DTO·응답 클래스와 동일합니다.

### POST `/api/posts/1/comments` — 루트 댓글 작성

**요청 body** (`CommentCreateRequest`)

```json
{
  "userId": 1,
  "content": "첫 댓글입니다."
}
```

`parentId`를 생략하거나 `null`이면 게시글에 직접 달린 루트 댓글입니다.

**응답** `200 OK` (`CommentResponse`, 루트이므로 `replies`는 빈 배열)

```json
{
  "id": 10,
  "content": "첫 댓글입니다.",
  "userId": 1,
  "postId": 1,
  "nickname": "닉네임",
  "parentId": null,
  "depth": 0,
  "createTime": "2026-05-01T10:00:00",
  "updateTime": "2026-05-01T10:00:00",
  "replies": []
}
```

### POST `/api/posts/1/comments` — 대댓글 작성

**요청 body** — `parentId`는 **반드시 루트 댓글 ID**여야 합니다.

```json
{
  "userId": 2,
  "parentId": 10,
  "content": "대댓글입니다."
}
```

**응답** `200 OK` — 대댓글 단일 노드 (`replies` 비어 있음, `depth` 1)

```json
{
  "id": 11,
  "content": "대댓글입니다.",
  "userId": 2,
  "postId": 1,
  "nickname": "다른유저",
  "parentId": 10,
  "depth": 1,
  "createTime": "2026-05-01T10:05:00",
  "updateTime": "2026-05-01T10:05:00",
  "replies": []
}
```

### GET `/api/posts/1/comments` — 2단 트리 목록

**응답** `200 OK` (`CommentResponse[]`) — 각 루트의 `replies`에 해당 루트의 대댓글만 포함됩니다.

```json
[
  {
    "id": 10,
    "content": "첫 댓글입니다.",
    "userId": 1,
    "postId": 1,
    "nickname": "닉네임",
    "parentId": null,
    "depth": 0,
    "createTime": "2026-05-01T10:00:00",
    "updateTime": "2026-05-01T10:00:00",
    "replies": [
      {
        "id": 11,
        "content": "대댓글입니다.",
        "userId": 2,
        "postId": 1,
        "nickname": "다른유저",
        "parentId": 10,
        "depth": 1,
        "createTime": "2026-05-01T10:05:00",
        "updateTime": "2026-05-01T10:05:00",
        "replies": []
      }
    ]
  }
]
```

### PUT `/api/posts/1/comments/10?userId=1` — 수정

**요청 body** (`CommentUpdateRequest`)

```json
{
  "content": "수정된 댓글 내용입니다."
}
```

**응답** `200 OK` — `CommentResponse` (단일 노드 직렬화와 동일; 루트면 `replies`는 빈 배열)

### DELETE `/api/posts/1/comments/10?userId=1` — 소프트 삭제

**응답** `200 OK`, **body 없음** (`void`). 이후 목록 조회에서 제외됩니다.

---

## 검증·에러 메시지 (요약)

서비스에서 `IllegalArgumentException`으로 던지는 대표 메시지입니다. (HTTP 상태는 기본적으로 500에 가깝게 동작할 수 있으니, 운영 시 `@ControllerAdvice`로 매핑하는 것이 좋습니다.)

| 상황 | 메시지 예 |
|------|-----------|
| 없는 게시글 | `존재하지 않는 게시글입니다.` |
| 없는 사용자 | `존재하지 않는 사용자입니다.` |
| 없는 부모 댓글 | `존재하지 않는 댓글입니다.` |
| 다른 글의 댓글에 답글 | `다른 게시글의 댓글에는 답글을 달 수 없습니다.` |
| 대댓글에 또 답글 | `대댓글에는 답글을 달 수 없습니다. (최대 2단계)` |
| 수정·삭제 시 URL `postId` 불일치 | `해당 게시글의 댓글이 아닙니다.` |
| 수정·삭제 시 작성자 아님 | `댓글 작성자만 수정/삭제할 수 있습니다.` |

Bean Validation 실패 시(예: 내용 공백, 300자 초과)는 기본적으로 **400** 계열 응답으로 처리할 수 있습니다.

## 현재 구현의 한계 · 보강 여지

- **인증·인가 없음**: `userId`를 쿼리 파라미터·바디로 받습니다. 실서비스에서는 로그인 주체와 통일하는 것이 안전합니다.
- **루트 삭제 시 대댓글 정책**: 루트만 소프트 삭제하면 대댓글은 DB에 남을 수 있습니다. “글 삭제 시 하위 일괄 삭제” 등 정책은 선택적으로 보강할 수 있습니다.
- **전역 예외 처리**: `IllegalArgumentException`을 404/403 등으로 매핑하면 클라이언트가 다루기 쉽습니다.
- **단위 테스트**: `CommentService` 전용 테스트를 추가하면 생성·트리 조립·2단 제한·권한 검증을 고정할 수 있습니다.

## 다음에 다루면 좋은 것들

- `CommentServiceTest` 및 `@WebMvcTest`로 API·실패 케이스 보강
- 루트 삭제와 대댓글 일관성(연쇄 소프트 삭제 등)
- SpringDoc 등 API 문서화

---

[← 메인 README로](README.md) · [게시글 도메인](POSTREADME.md) · [회원 도메인](USERREADME.md)
