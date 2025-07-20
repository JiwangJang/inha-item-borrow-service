<h1>미래융합대학 물품대여사업 프로젝트</h1>

### 1. 🧠 네이밍 규칙 (변수, 함수, 클래스 등)

| 항목     | 규칙                      | 예시                                       |
| -------- | ------------------------- | ------------------------------------------ |
| 변수명   | camelCase 사용            | `userName`, `itemList`, `isLoggedIn`       |
| 함수명   | camelCase + 동사 시작     | `getUserInfo()`, `createPost()`            |
| 클래스명 | PascalCase 사용           | `UserService`, `ItemController`            |
| 상수명   | UPPER_SNAKE_CASE 사용     | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`       |
| 패키지명 | 소문자 + 도메인 역순 구조 | `com.inha.item.borrow.user`                |
| 파일명   | 클래스 기준 (PascalCase)  | `UserController.java`, `LoginService.java` |

> ⚠️ 줄임말, 축약어, 모호한 약자 지양 (`usr`, `itm` 이런 거 금지)

---

### 2. 🔀 Git 브랜치 전략 (Git Flow)

-   기본 브랜치:

    -   `main`: 실제 운영 배포 코드
    -   `dev`: 개발 통합 브랜치 (여기서 기능 브랜치 분기)

-   브랜치 명명 규칙:

| 브랜치 타입 | 접두어      | 예시                          |
| ----------- | ----------- | ----------------------------- |
| 기능 개발   | `feature/`  | `feature/login-api`           |
| 버그 수정   | `fix/`      | `fix/signup-validation-error` |
| 리팩토링    | `refactor/` | `refactor/user-service`       |
| 문서 작성   | `docs/`     | `docs/readme-convention`      |
| 테스트 코드 | `test/`     | `test/user-service-test`      |
| 설정 변경   | `chore/`    | `chore/gradle-dependencies`   |

-   Git Flow 흐름:
    1. `dev` 브랜치에서 기능 브랜치 분기
    2. 기능 개발 후 → `dev`로 PR 및 리뷰
    3. 배포 시점에 `dev` → `main` 머지

---

### 3. ✅ Git 커밋 메시지 규칙 (Conventional Commits)

**타입 종류**

| 타입       | 의미                           | 예시                                |
| ---------- | ------------------------------ | ----------------------------------- |
| `feat`     | 새로운 기능 추가               | `feat: 로그인 기능 구현`            |
| `fix`      | 버그 수정                      | `fix: 로그인 시 비밀번호 오류 수정` |
| `refactor` | 코드 리팩토링 (동작 변화 없음) | `refactor: UserService 리팩토링`    |
| `docs`     | 문서 수정                      | `docs: README에 API 설명 추가`      |
| `test`     | 테스트 코드 추가/수정          | `test: UserController 테스트 작성`  |
| `chore`    | 기타 설정, 빌드, CI 작업 등    | `chore: GitHub Actions 설정 추가`   |
| `style`    | 포맷, 들여쓰기, 세미콜론 등    | `style: 코드 포맷 일관화`           |

> 💡 메시지는 명확하게! "수정함", "해결" 이런 건 절대 금지. 팀원도 봐야 하니까.

---

### 4. 📎 Pull Request 규칙

-   PR 제목:  
    `[#이슈번호] 타입: 간단한 설명`

    예시:  
    `[#21] feat: 대여 신청 API 구현`

-   PR 설명 포함 항목:
    -   구현한 기능 요약
    -   관련 이슈 번호
    -   테스트 여부
    -   기타 참고사항

---

### 🔒 규칙 위반 시

-   코드 리뷰에서 병합 보류
-   커밋 메시지/브랜치명/PR 제목 전부 리젝 사유 가능
-   린트 설정 위반 시 CI 실패 처리 가능성 있음

---

### ✋ 협업 팁

-   작은 기능 단위로 자주 커밋하라
-   커밋 메시지엔 반드시 의미를 담아라
-   머지 전에 꼭 팀원 리뷰 받자
-   PR 열 때마다 README나 API 명세 업데이트 체크해라
