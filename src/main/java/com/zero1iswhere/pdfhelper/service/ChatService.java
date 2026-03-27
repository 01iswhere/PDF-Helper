package com.zero1iswhere.pdfhelper.service;

import reactor.core.publisher.Flux;

public interface ChatService {

    Flux<String> streamChat(String userMessage, String chatId, String pdfUUIDs);
}
