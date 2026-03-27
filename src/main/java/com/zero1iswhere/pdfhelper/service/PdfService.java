package com.zero1iswhere.pdfhelper.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PdfService {

    List<String> upload(MultipartFile file, String chatId) throws IOException;
}
