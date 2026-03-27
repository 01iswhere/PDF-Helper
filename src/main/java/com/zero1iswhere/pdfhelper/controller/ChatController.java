package com.zero1iswhere.pdfhelper.controller;

import com.zero1iswhere.pdfhelper.annotation.Timer;
import com.zero1iswhere.pdfhelper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping(value = "/ai/chat", produces = "text/html;charset=utf-8")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Timer(name = "流式回答")
    @GetMapping("/streamChat")
    public Flux<String> streamChat(@RequestParam("userMessage") String userMessage, @RequestParam("chatId") String chatId, @RequestParam(value = "pdfUUID", required = false) String pdfUUID) {
        Flux<String> res = chatService.streamChat(userMessage, chatId, pdfUUID);
        return res;
    }
}
