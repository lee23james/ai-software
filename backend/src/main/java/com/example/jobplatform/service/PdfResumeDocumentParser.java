package com.example.jobplatform.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PdfResumeDocumentParser {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!filename.endsWith(".pdf")) {
            return readAsTextFallback(file);
        }
        try (var document = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(document).trim();
        } catch (IOException ex) {
            return "";
        }
    }

    private String readAsTextFallback(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            return "";
        }
    }
}
