<h1>미래융합대학 물품대여사업 프로젝트</h1>

### 0. 서비스 실행명령어
  - front -> frontend폴더로 이동후 npm run build -> npm run start
  - back -> backend폴더로 이동후 ./gradlew build -x test -> java -jar build/libs/inha-borrow-backend.jar


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
| 설정 변경   | `chore/`    | `chore/gradle-dependencies`   |

-   기능 개발이나 리팩토링시 같이 테스트도 수행합니다.

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


---

### 5. 🖥️ Backend 개발 규칙

#### 5.1 계층 간 데이터 전달 규칙

- Controller와 Service는 **DTO로만 소통합니다.**
- Service와 Repository는 **Entity로만 소통합니다.**

즉, 계층 간 데이터 전달 구조는 다음과 같습니다.

```
Controller -> Service : DTO
Service -> Repository : Entity
```

이 규칙을 통해 계층 간 책임을 명확하게 분리하고,
Entity가 외부 API 응답으로 직접 노출되는 것을 방지합니다.

---

#### 5.2 메서드 작성 순서 (CRUD)

각 **Controller / Service / Repository** 클래스 내부의 메서드는
다음 **CRUD 순서**로 작성합니다.

1. 생성 (Create)
2. 조회 (Read)
3. 수정 (Update)
4. 삭제 (Delete)

또한 각 구간은 가독성을 위해 **주석으로 구분합니다.**

예시

```java
// ==================== Create ====================

// ==================== Read ====================

// ==================== Update ====================

// ==================== Delete ====================
```

---

#### 5.3 메서드 네이밍 규칙

메서드는 다음 **접두어 규칙**을 따릅니다.

| 기능 | 접두어 |
|-----|------|
| 생성 | `save` |
| 삭제 | `delete` |
| 수정 | `update` |
| 단건 조회 | `findBy` |
| 전체 조회 | `findAllBy` |

예시

```java
saveBorrower()
deleteBorrower()
updateBorrowerPhoneNumber()
findById()
findAllByDepartment()
```

---

#### 5.4 DTO 네이밍 규칙

DTO는 **메서드 목적이 드러나도록 메서드명과 결합하여 작성합니다.**

예시

```
BorrowerLoginDto
PatchPhoneNumberDto
SavePhoneAccountNumberDto
```

이를 통해 DTO의 사용 목적을 명확하게 파악할 수 있도록 합니다.

---
