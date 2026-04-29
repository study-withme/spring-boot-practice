# Mini Blog (연습·포트폴리오)

신입 백엔드 개발자로서 **Spring Boot**로 블로그 백엔드를 단계적으로 만들어 가는 개인 프로젝트입니다. 
기능을 조금씩 붙이며 API 설계, 영속성, 예외 처리 등을 익히는 것이 목표입니다.

## 이 레포지토리에서 하는 일

- REST API 기반의 소형 블로그 백엔드 구현
- 혼자 순차적으로 범위를 나누어 커밋·기능 단위로 확장
- 구현·디버깅 과정에서 마주친 이슈를 **트러블슈팅 목록**으로 남기고, 필요 시 외부 글·노트와 연결

## 기술 스택

| 구분 | 사용 |
|------|------|
| 언어·런타임 | Java 25 |
| 프레임워크 | Spring Boot 4.0.2 |
| 빌드 | Gradle |
| 웹 | Spring Web MVC |
| 데이터 | Spring Data JPA |
| 기타 | Lombok, Spring Boot DevTools (개발) |

## 프로젝트 구조

애플리케이션 소스는 `practice/` 모듈에 있습니다.

```
practice/
├── build.gradle
├── src/main/java/.../PracticeApplication.java
└── src/main/resources/application.properties
```

## 트러블슈팅 · 학습 노트

개발 중 겪은 문제와 원인·해결을 모아 두는 목록입니다. 이후 블로그 글이나 위키 링크를 아래에 계속 붙일 예정입니다.

| 날짜 | 주제 | 링크 / 비고 |
|------|------|-------------|
| — | (예: Gradle JVM 설정, JPA lazy 로딩 오류 등) | |

---

*포트폴리오·학습 목적의 비공개·공개 여부는 본인 기준에 맞게 조정하시면 됩니다.*
