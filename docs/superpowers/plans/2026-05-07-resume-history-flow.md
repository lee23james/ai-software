# Resume History Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make PDF resume uploads visible and reviewable by extracting text, saving parse results, and adding a history page with detail view.

**Architecture:** The backend keeps resume files and parsed content in `resume_info`, stores extracted metadata in `resume_parse_result`, and reuses existing `resume_skill` and `job_match_record` tables for skills and matches. The frontend adds a history list and detail page so users can revisit any uploaded resume and inspect parsed text, extracted metadata, and match results.

**Tech Stack:** Spring Boot, MyBatis-Plus, H2/MySQL schema, Vue 3, Vue Router, Element Plus, PDFBox 3.0.6.

---

### Task 1: Backend PDF extraction and history model

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/java/com/example/jobplatform/entity/ResumeParseResult.java`
- Create: `backend/src/main/java/com/example/jobplatform/mapper/ResumeParseResultMapper.java`
- Create: `backend/src/main/java/com/example/jobplatform/service/PdfResumeDocumentParser.java`
- Modify: `backend/src/main/java/com/example/jobplatform/entity/Resume.java`
- Modify: `backend/src/main/java/com/example/jobplatform/service/impl/ResumeServiceImpl.java`
- Test: `backend/src/test/java/com/example/jobplatform/service/PdfResumeDocumentParserTest.java`

- [ ] **Step 1: Write the failing test**

```java
@Test
void extractsTextFromDigitalPdfResume() {
    // create a simple PDF and assert extracted text contains the content
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q -Dtest=PdfResumeDocumentParserTest test`
Expected: fail because the parser class does not exist yet.

- [ ] **Step 3: Write minimal implementation**

```java
public String extractText(MultipartFile file) {
    try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
        return new PDFTextStripper().getText(document).trim();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q -Dtest=PdfResumeDocumentParserTest test`
Expected: pass and the extracted text should contain the PDF body text.

### Task 2: Backend history APIs

**Files:**
- Create: `backend/src/main/java/com/example/jobplatform/vo/ResumeHistoryVO.java`
- Create: `backend/src/main/java/com/example/jobplatform/vo/ResumeHistoryDetailVO.java`
- Create: `backend/src/main/java/com/example/jobplatform/vo/ResumeParseResultVO.java`
- Modify: `backend/src/main/java/com/example/jobplatform/service/ResumeService.java`
- Modify: `backend/src/main/java/com/example/jobplatform/controller/ResumeController.java`
- Modify: `backend/src/main/java/com/example/jobplatform/service/impl/ResumeServiceImpl.java`
- Test: `backend/src/test/java/com/example/jobplatform/controller/ResumeControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
@WebMvcTest(ResumeController.class)
class ResumeControllerTest {
    // verify GET /api/resume/history and /api/resume/{id} exist
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `mvn -q -Dtest=ResumeControllerTest test`
Expected: fail because the endpoints are missing.

- [ ] **Step 3: Write minimal implementation**

```java
@GetMapping("/history")
public ApiResponse<List<ResumeHistoryVO>> listHistory(@RequestParam Long userId)
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `mvn -q -Dtest=ResumeControllerTest test`
Expected: pass with JSON containing resume history and detail payloads.

### Task 3: Frontend history pages

**Files:**
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/layouts/MainLayout.vue`
- Modify: `frontend/src/views/ResumeView.vue`
- Create: `frontend/src/views/ResumeHistoryView.vue`
- Create: `frontend/src/views/ResumeHistoryDetailView.vue`
- Create: `frontend/src/api/resume.js`
- Modify: `frontend/src/assets/styles.css`
- Test: `frontend/src/router/index.test.js`

- [ ] **Step 1: Write the failing test**

```js
it('registers resume history routes', () => {
  // expect route names for history and history-detail
})
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- --runInBand frontend/src/router/index.test.js`
Expected: fail because history routes are not registered.

- [ ] **Step 3: Write minimal implementation**

```js
{
  path: 'resume/history',
  name: 'resume-history',
  component: ResumeHistoryView
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `npm test -- --runInBand frontend/src/router/index.test.js`
Expected: pass and the resume menu can navigate to the new pages.

### Task 4: Verification and cleanup

**Files:**
- Modify: any files changed above

- [ ] Run backend tests: `mvn test`
- [ ] Run frontend tests: `npm test`
- [ ] Run frontend build: `npm run build`
- [ ] Fix any integration issues discovered during verification
