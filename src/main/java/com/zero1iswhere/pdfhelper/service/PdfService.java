package com.zero1iswhere.pdfhelper.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PdfService {

    String upload(MultipartFile file, String chatId) throws IOException;
}
