package com.zero1iswhere.pdfhelper.service;

import com.zero1iswhere.pdfhelper.pojo.vo.MessageVo;

import java.util.List;

public interface ChatHistoryRepository {

    void save(String chatId);

    List<String> getChatIds();

    List<MessageVo> getChatDetail(String chatId);
}