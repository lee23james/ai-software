package com.example.jobplatform.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfResumeDocumentParserTest {

    @Test
    void extractsTextFromDigitalPdfResume() throws Exception {
        byte[] pdfBytes = createPdf("Java Python SQL");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "resume.pdf",
            "application/pdf",
            pdfBytes
        );

        PdfResumeDocumentParser parser = new PdfResumeDocumentParser();

        String text = parser.extractText(file);

        assertThat(text).contains("Java");
        assertThat(text).contains("Python");
        assertThat(text).contains("SQL");
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
