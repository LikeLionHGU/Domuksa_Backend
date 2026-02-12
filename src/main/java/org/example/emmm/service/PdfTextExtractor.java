package org.example.emmm.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfTextExtractor {
    public String extractText(byte[] pdfBytes) {
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            return (text == null) ? "" : text;
        } catch (Exception e) {
            return ""; // 실패해도 요약이 전체 실패하지 않게 빈값 처리
        }
    }
}
