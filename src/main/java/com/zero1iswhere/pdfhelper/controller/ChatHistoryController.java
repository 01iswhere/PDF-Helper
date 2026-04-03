package com.zero1iswhere.pdfhelper.controller;

import com.zero1iswhere.pdfhelper.annotation.Timer;
import com.zero1iswhere.pdfhelper.pojo.vo.ChatDetail;
import com.zero1iswhere.pdfhelper.service.serviceImpl.MongoChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final MongoChatHistoryRepository chatHistoryService;

    @Timer(name = "用户获取所有会话ID")
    @GetMapping("/allChatIds")
    public List<String> getAllChatId() {
        List<String> chatIds = chatHistoryService.getChatIds();
        return chatIds;
    }

    @Timer(name = "用户获取会话详细ID")
    @GetMapping("/{chatId}")
    public ChatDetail getChatHistory(@PathVariable("chatId") String chatId) {
        ChatDetail res = chatHistoryService.getChatDetail(chatId);
        return res;
    }

}
