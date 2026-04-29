# 게시글(Post) 도메인

게시글 **작성·목록·단건·작성자별 조회·수정·삭제(소프트 삭제)** 를 **레이어드 아키텍처**로 구현한 문서입니다.  
작성자는 `User`와 **다대일(`@ManyToOne`)** 로 연결됩니다. 구현 코드는 `practice/` 모듈의 `com.test.practice.post` 패키지에 있습니다.

## 이 구현에서 한 일

- REST API로 게시글 **생성·전체 조회·단건 조회·특정 사용자 게시글 목록·수정·삭제** 제공
- **Spring Data JPA**로 `Post` 엔티티 영속화, `deleteTime`이 `null`인 행만 “노출 목록”으로 조회하는 **소프트 삭제** 패턴
- 생성 요청에 **Bean Validation**(`@Valid`, `@NotBlank`, `@Size` 등) 적용
- 수정 시 **작성자 `userId` 일치 여부** 검증 (`IllegalArgumentException`)
- **서비스 단위 테스트(Mockito)** 로 작성·조회·수정·삭제 시나리오 검증 (`PostServiceTest`)

## 패턴과 구현 방식

| 영역 | 선택 | 설명 |
|------|------|------|
| 전체 구조 | Controller → Service → Repository | HTTP·트랜잭션과 도메인 규칙·DB 접근 분리 |
| 영속성 | JPA 엔티티 + `JpaRepository` | `posts` 테이블, `user_id` FK로 작성자 연결 |
| 삭제 | 소프트 삭제 | `deleteTime` 설정; 목록·단건 조회는 `deleteTime IS NULL` 조건 쿼리 사용 |
| 타임스탬프 | `@PrePersist` / `@PreUpdate` | 생성 시 `createTime`, 수정 시 `updateTime` 자동 설정 |
| API 경계 | Request / Response DTO | `PostCreateRequest`, `PostUpdateRequest`, `PostResponse`로 스키마 분리 |
| 응답 조립 | `PostResponse(Post)` | 게시글 + 작성자 `userId`, `username`, `nickname`까지 한 응답에 포함 |
| 실패 처리 | `IllegalArgumentException` + 메시지 | 없는 유저·없는 글·작성자 불일치 등 (전역 예외 처리로 정리 가능) |

## 왜 이렇게 구현했는지

- **소프트 삭제**: 물리 삭제보다 복구·감사·연관 데이터 정합성을 다루기 쉽고, “삭제된 글 제외 조회”를 리포지토리 메서드로 명확히 표현할 수 있습니다.
- **수정 시 작성자 검증**: `userId`를 쿼리 파라미터로 받아 게시글의 `user`와 비교함으로써, 다른 사용자가 내용을 바꾸는 시나리오를 막습니다.
- **DTO + 생성 시 검증**: 제목·본문 길이 등 API 계약을 컨트롤러 진입 단계에서 걸러 사용자 경험과 서비스 코드를 단순하게 유지합니다.

## 패키지 구조 (요약)

```
practice/src/main/java/com/test/practice/post/
├── controller/PostController.java
├── service/PostService.java
├── repository/PostRepository.java
├── entity/Post.java
└── dto/
    ├── PostCreateRequest.java
    ├── PostUpdateRequest.java
    └── PostResponse.java
```

## API 개요

기본 경로: `/api/posts`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/posts` | 게시글 작성 (`PostCreateRequest` body, `@Valid`) |
| GET | `/api/posts` | 삭제되지 않은 게시글 전체 조회 |
| GET | `/api/posts/{postId}` | 단건 조회 (삭제된 글은 없음으로 처리) |
| GET | `/api/posts/user/{userId}` | 특정 사용자의 삭제되지 않은 게시글 목록 |
| PUT | `/api/posts/{postId}?userId={userId}` | 수정 (`PostUpdateRequest` body, 작성자 `userId` 필요) |
| DELETE | `/api/posts/{postId}` | 소프트 삭제 (`deleteTime` 설정) |

## 요청·응답 예시 (JSON)

아래는 **Spring Boot 기본 JSON 직렬화**(Jackson, `LocalDateTime` → ISO-8601 문자열)를 가정한 예시입니다. 필드명은 실제 DTO·응답 클래스와 동일합니다.

### POST `/api/posts` — 게시글 작성

**요청 body** (`PostCreateRequest`)

```json
{
  "userId": 1,
  "title": "첫 번째 글",
  "content": "본문 내용입니다."
}
```

**응답** `200 OK` (`PostResponse`, 저장 직후 `id`·`createTime` 등이 채워짐)

```json
{
  "id": 1,
  "title": "첫 번째 글",
  "content": "본문 내용입니다.",
  "createTime": "2026-04-29T14:30:00",
  "updateTime": null,
  "deleteTime": null,
  "viewCount": 0,
  "userId": 1,
  "username": "testuser",
  "nickname": "닉네임"
}
```

### GET `/api/posts` — 목록

**응답** `200 OK` (`PostResponse[]`, 삭제되지 않은 글만)

```json
[
  {
    "id": 1,
    "title": "첫 번째 글",
    "content": "본문 내용입니다.",
    "createTime": "2026-04-29T14:30:00",
    "updateTime": null,
    "deleteTime": null,
    "viewCount": 0,
    "userId": 1,
    "username": "testuser",
    "nickname": "닉네임"
  }
]
```

### GET `/api/posts/{postId}` — 단건

**응답** `200 OK` — 본문은 목록의 한 요소와 동일한 형태의 단일 객체입니다.

### GET `/api/posts/user/{userId}` — 특정 사용자 글 목록

**응답** `200 OK` — `PostResponse[]` (해당 사용자·미삭제 글만). 배열 형태는 위 “목록” 예시와 같습니다.

### PUT `/api/posts/{postId}?userId=1` — 수정

**요청 body** (`PostUpdateRequest`)

```json
{
  "title": "수정된 제목",
  "content": "수정된 본문입니다."
}
```

**응답** `200 OK` — `PostResponse` (같은 스키마, `updateTime`이 갱신된 값으로 나올 수 있음)

```json
{
  "id": 1,
  "title": "수정된 제목",
  "content": "수정된 본문입니다.",
  "createTime": "2026-04-29T14:30:00",
  "updateTime": "2026-04-29T15:00:00",
  "deleteTime": null,
  "viewCount": 0,
  "userId": 1,
  "username": "testuser",
  "nickname": "닉네임"
}
```

### DELETE `/api/posts/{postId}` — 소프트 삭제

**응답** `200 OK`, **body 없음** (`void`). DB에서는 `deleteTime`이 설정됩니다.

---

## 테스트

<img width="445" height="256" alt="PostServiceTest 통과 화면 자리 — 실제 캡처로 교체" src="https://placehold.co/445x256/252836/b8c0d4?text=PostServiceTest+%7C+4+passed" />

- `practice/src/test/java/com/test/practice/post/service/PostServiceTest.java`
- 게시글 작성 성공, 단건 조회 성공, 수정 성공, 삭제 성공(삭제 후 `deleteTime` 설정 여부)
- 로컬 확인: `practice` 모듈에서  
  `.\gradlew test --tests com.test.practice.post.service.PostServiceTest`  
  → 예시 로그:

```text
> Task :test
BUILD SUCCESSFUL in 2s
```

`USERREADME.md`처럼 **실제 IDE·Gradle 통과 화면**을 쓰려면, 캡처를 GitHub 이슈·PR 등에 올린 뒤 위 `<img>`의 `src`를 `https://github.com/user-attachments/assets/...` 주소로 바꾸면 됩니다.

## 현재 구현의 한계 · 보강 여지

- 삭제 API는 **작성자 검증이 없음** — 누구나 `postId`만 알면 소프트 삭제 가능합니다. 수정과 동일하게 `userId` 또는 인증 주체와 맞추는 것이 좋습니다.
- **조회수(`viewCount`)** 필드와 `increaseViewCount()`는 엔티티에 있으나, 컨트롤러·서비스에서 호출하는 API는 아직 없습니다.
- `PostUpdateRequest`에는 생성과 같은 수준의 Bean Validation이 없습니다 — 필요하면 제목·본문 규칙을 맞추거나 `@Valid`를 보강할 수 있습니다.
- 삭제 시 `findById`만 사용하므로, 이미 소프트 삭제된 글에 대한 동작 정책(재삭제 방지 등)은 선택적으로 정리할 수 있습니다.
- 인증·인가(JWT·세션 등)가 없어 `userId`를 클라이언트가 직접 넘기는 형태입니다 — 실서비스에서는 로그인 사용자 기준으로 바꾸는 것이 안전합니다.

## 다음에 다루면 좋은 것들

- 삭제·조회수 증가에 **권한·검증** 통일, **전역 예외 응답**(`@ControllerAdvice`)
- `PostServiceTest`에 실패 케이스(없는 글, 작성자 불일치 수정 등) 보강
- `@WebMvcTest` 컨트롤러 테스트, `@DataJpaTest` 등 통합 테스트
- API 문서화(SpringDoc 등)

---

[← 메인 README로](README.md) · [회원 도메인](USERREADME.md)
