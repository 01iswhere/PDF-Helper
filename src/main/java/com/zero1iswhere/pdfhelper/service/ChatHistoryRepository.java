package com.zero1iswhere.pdfhelper.service;

import com.zero1iswhere.pdfhelper.pojo.vo.ChatDetail;

import java.util.List;

public interface ChatHistoryRepository {

    void save(String chatId);

    List<String> getChatIds();

    ChatDetail getChatDetail(String chatId);

}