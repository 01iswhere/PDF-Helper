package com.zero1iswhere.pdfhelper.controller;

import com.zero1iswhere.pdfhelper.annotation.Timer;
import com.zero1iswhere.pdfhelper.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ai/pdf")
@Slf4j
@RequiredArgsConstructor
public class PDFController {

    private final PdfService pdfService;

    @Timer(name = "PDF上传")
    @PostMapping("/upload")
    public String uploadPdf(@RequestParam("file") MultipartFile file, @RequestParam("chatId") String chatId) throws IOException {
        String res = pdfService.upload(file, chatId);
        return res;
    }
}
