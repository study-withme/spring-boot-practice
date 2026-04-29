# 회원(User) 도메인

회원가입·로그인·조회·수정·삭제를 **레이어드 아키텍처**로 최소 구현한 초기 커밋 기준 문서입니다.  
구현 코드는 `practice/` 모듈의 `com.test.practice.user` 패키지에 있습니다.

## 이 커밋에서 한 일

- REST API로 회원 **가입·로그인·단건/전체 조회·수정·삭제** 제공
- **Spring Data JPA**로 `User` 엔티티 영속화, **서비스 단위 테스트(Mockito)** 로 핵심 규칙 검증
- JDK 환경에서 Mockito inline mock 경고를 줄이기 위해 **Gradle `test` 태스크에 Mockito javaagent** 설정 (`practice/build.gradle`)

## 패턴과 구현 방식

| 영역 | 선택 | 설명 |
|------|------|------|
| 전체 구조 | Controller → Service → Repository | HTTP·트랜잭션 경계와 도메인 규칙·DB 접근을 분리 |
| 영속성 | JPA 엔티티 + `JpaRepository` | `User`와 `users` 테이블 매핑, `username` 유니크 등 |
| API 경계 | Request / Response DTO | `SignupRequest`, `LoginRequest`, `UserResponse`로 API 스키마와 엔티티 분리 |
| 응답 조립 | `UserResponse.from(User)` | 엔티티 → API 응답 변환을 한곳에 모음 (비밀번호는 응답에 포함하지 않음) |
| 실패 처리 | `IllegalArgumentException` + 메시지 | 초기 단계에서 규칙 위반을 명시적으로 표현 (추후 전역 예외 처리로 정리 가능) |
| 테스트 | `@ExtendWith(MockitoExtension.class)` | `UserRepository`를 목으로 두고 **비즈니스 규칙만** 빠르게 검증 |

## 왜 이렇게 구현했는지

- **DTO를 둔 이유**: 엔티티를 API에 그대로 노출하면 필드 변경·비밀번호 노출·표현 계층과 도메인이 뒤섞이기 쉽습니다.
- **규칙을 서비스에 둔 이유**: 중복 아이디, 로그인 실패(없는 아이디·비밀번호 불일치) 등은 컨트롤러보다 서비스에서 검증하는 편이 테스트와 재사용에 유리합니다.
- **리포지토리 모킹 단위 테스트**: 초기에는 DB 없이 “우리가 작성한 규칙”이 맞는지 우선 보장하고, 통합 테스트는 이후 단계에서 보강할 수 있습니다.

## 패키지 구조 (요약)

```
practice/src/main/java/com/test/practice/user/
├── controller/UserController.java
├── service/UserService.java
├── repository/UserRepository.java
├── entity/User.java
└── dto/
    ├── SignupRequest.java
    ├── LoginRequest.java
    └── UserResponse.java
```

## API 개요 (초기 구현)

기본 경로: `/api/users`

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/users/signup` | 회원가입 (`SignupRequest` body) — 학습용으로 GET에 body를 둔 상태이며, 이후 POST로 바꾸는 것을 권장 |
| POST | `/api/users/login` | 로그인 (`LoginRequest` body) |
| GET | `/api/users` | 회원 전체 조회 |
| GET | `/api/users/{id}` | 회원 단건 조회 |
| PUT | `/api/users/{id}` | 회원 수정 (`SignupRequest` body) |
| DELETE | `/api/users/{id}` | 회원 삭제 (응답 204 No Content) |

## 테스트
<img width="445" height="256" alt="image" src="https://github.com/user-attachments/assets/6c109bfa-6c44-4f1c-8e0e-a3e1b3e9d208" />
- `practice/src/test/java/com/test/practice/UserServiceTest.java`
- 회원가입 성공/중복 아이디, 로그인 성공·실패, 단건·전체 조회, 수정, 삭제 시나리오

## 초기 커밋의 한계 (의도적으로 남긴 과제)

- 비밀번호 **평문 저장** (해시·검증 미적용)
- **JWT / 세션** 등 인증 상태 유지 없음
- **Bean Validation(`@Valid`)** 및 **전역 예외 응답(`@ControllerAdvice`)** 미적용
- 회원가입 HTTP 메서드가 GET인 점은 REST 관점에서 비권장

## 다음 커밋에서 다루면 좋은 것들

- 비밀번호 **BCrypt** 등으로 저장·검증
- **JWT** 또는 **세션** 기반 인증
- `jakarta.validation` + `@Valid` 요청 검증, 공통 에러 응답
- 회원가입을 **POST**로 변경, API 문서화(SpringDoc 등)
- `@DataJpaTest` / **Testcontainers** 리포지토리 통합 테스트, `@WebMvcTest` 컨트롤러 테스트
- `createdAt` / `updatedAt` **Auditing** 또는 `@PrePersist` / `@PreUpdate`
- 서비스 **실패 케이스** 테스트 보강 (없는 id로 수정·삭제·조회 등)

---

[← 메인 README로](README.md)
