package com.example.jobplatform.service;

import com.example.jobplatform.dto.RegisterRequestDTO;
import com.example.jobplatform.service.impl.ResumeServiceImpl;
import com.example.jobplatform.vo.ResumeCreateVO;
import com.example.jobplatform.vo.ResumeHistoryDetailVO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ResumeHistoryDetailIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private ResumeService resumeService;

    @Test
    void uploadedResumeDetailContainsExtractedText() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setPhone("13900000001");
        request.setPassword("password123");
        Long userId = authService.register(request).userId();

        MockMultipartFile pdf = new MockMultipartFile(
            "file",
            "resume.pdf",
            "application/pdf",
            createPdf("Resume Detail Smoke Test")
        );

        ResumeCreateVO created = resumeService.uploadResume(userId, "测试简历", "数据分析师", pdf, "Python,SQL");
        ResumeHistoryDetailVO detail = resumeService.getResumeHistoryDetail(created.resumeId());

        assertThat(detail.resumeText()).contains("Resume Detail Smoke Test");
    }

    @Test
    void uploadedResumeDetailExtractsSchoolAndEducationFromText() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setPhone("13900000002");
        request.setPassword("password123");
        Long userId = authService.register(request).userId();

        String resumeText = String.join("\n",
            "教育经历",
            "东南大学（985）",
            "2023年9月-2027年9月（预期毕业）",
            "数据分析相关课程：Python、SQL、机器学习"
        );
        MockMultipartFile txt = new MockMultipartFile(
            "file",
            "resume.txt",
            "text/plain",
            resumeText.getBytes(StandardCharsets.UTF_8)
        );

        ResumeCreateVO created = resumeService.uploadResume(userId, "文本简历", "数据分析师", txt, "Python,SQL");
        ResumeHistoryDetailVO detail = resumeService.getResumeHistoryDetail(created.resumeId());

        assertThat(detail.parseResult()).isNotNull();
        assertThat(detail.parseResult().parsedSchool()).isEqualTo("东南大学");
        assertThat(detail.parseResult().parsedEducation()).isEqualTo("本科");
    }

    @Test
    void uploadedResumeDetailMapsTwoGraduationsToMaster() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setPhone("13900000003");
        request.setPassword("password123");
        Long userId = authService.register(request).userId();

        String resumeText = String.join("\n",
            "教育经历",
            "南京大学",
            "2020年6月毕业",
            "2023年6月毕业"
        );
        MockMultipartFile txt = new MockMultipartFile(
            "file",
            "resume.txt",
            "text/plain",
            resumeText.getBytes(StandardCharsets.UTF_8)
        );

        ResumeCreateVO created = resumeService.uploadResume(userId, "双毕业简历", "算法工程师", txt, "Java");
        ResumeHistoryDetailVO detail = resumeService.getResumeHistoryDetail(created.resumeId());

        assertThat(detail.parseResult()).isNotNull();
        assertThat(detail.parseResult().parsedSchool()).isEqualTo("南京大学");
        assertThat(detail.parseResult().parsedEducation()).isEqualTo("硕士");
    }

    private byte[] createPdf(String content) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(72, 700);
                contentStream.showText(content);
                contentStream.endText();
            }

            document.save(out);
            return out.toByteArray();
        }
    }
}
