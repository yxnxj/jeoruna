## 1️⃣ 커밋 Convention

- [[Git] git 커밋 컨벤션 (Git Commit Message Convention) (tistory.com)](https://aroundlena.tistory.com/55?category=740019)
- [[Git] Commit Message Convention (velog.io)](https://velog.io/@archivvonjang/Git-Commit-Message-Convention)

### ✔️ Commit Type

| Tag Name | Description |
| --- | --- |
| Feat | 새로운 기능을 추가 |
| Fix | 버그 수정 |
| Design | CSS 등 사용자 UI 디자인 변경 |
| !BREAKING CHANGE | 커다란 API 변경의 경우 |
| !HOTFIX | 급하게 치명적인 버그를 고쳐야하는 경우 |
| Style | 코드 포맷 변경, 세미 콜론 누락, 코드 수정이 없는 경우 |
| Refactor | 프로덕션 코드 리팩토링 |
| Comment | 필요한 주석 추가 및 변경 |
| Docs | 문서 수정 |
| Test | 테스트 코드, 리펙토링 테스트 코드 추가, Production Code(실제로 사용하는 코드) 변경 없음 |
| Chore | 빌드 업무 수정, 패키지 매니저 수정, 패키지 관리자 구성 등 업데이트, Production Code 변경 없음 |
| Rename | 파일 혹은 폴더명을 수정하거나 옮기는 작업만인 경우 |
| Remove | 파일을 삭제하는 작업만 수행한 경우 |

### ✔️ Subject

- 제목은 **50글자** 이내로 작성한다.
- 첫글자는 대문자로 작성한다.
- 마침표 및 특수기호는 사용하지 않는다.
- 영문으로 작성하는 경우 동사(원형)을 가장 앞에 명령어로 작성한다.
- 과거시제는 사용하지 않는다.
    
    ```
    ex)
    Fixed --> Fix
    Added --> Add
    Modified --> Modify
    ```
    
- 간결하고 요점적으로 즉, 개조식 구문으로 작성한다.

### ✔️ Body

- 72글자 이내로 작성한다.
- 최대한 상세히 작성한다. (코드 변경의 이유를 명확히 작성할수록 좋다)
- 어떻게 변경했는지보다 무엇을, 왜 변경했는지 작성한다.

### ✔️ Footer

- 선택사항
- issue tracker ID 명시하고 싶은 경우에 작성한다.
- 유형: #이슈 번호 형식으로 작성한다.
- 여러 개의 이슈번호는 쉼표(,)로 구분한다.
- 이슈 트래커 유형은 다음 중 하나를 사용한다.
    - Fixes: 이슈 수정중 (아직 해결되지 않은 경우)
    - Resolves: 이슈를 해결했을 때 사용
    - Ref: 참고할 이슈가 있을 때 사용
    - Related to: 해당 커밋에 관련된 이슈번호 (아직 해결되지 않은 경우)
    
    ```
    ex)
    Fixes: #45 
    Related to: #34, #23
    ```
    

### ✔️ 커밋 Example

```
//Subject
**Feat:** 회원 가입 기능 구현

//Body
SMS, 이메일 중복확인 API 개발

//Footer
Resolves: #123
Ref: #456
Related to: #48, #45
```
