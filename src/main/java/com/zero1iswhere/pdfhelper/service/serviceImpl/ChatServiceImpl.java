package com.zero1iswhere.pdfhelper.service.serviceImpl;

import com.zero1iswhere.pdfhelper.service.ChatService;
import com.zero1iswhere.pdfhelper.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient QwenChatClient;

    private final MongoChatHistoryRepository chatHistoryRepository;

    private final VectorStore vectorStore;

    private static final Pattern SUMMARY_PATTERN = Pattern.compile(".*(归纳|总结|概述|简要|大体|大概|整体|总体|全文).*");

    @Override
    public Flux<String> streamChat(String userMessage, String chatId, String pdfUUID) {
        // 当前chatId是否合规
        chatHistoryRepository.save(chatId);

        // 概括则不需要相似度精度
        boolean isSummaryQuestion = isSummaryQuestion(userMessage);
        // RAG过滤检索
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(createSearchRequest(pdfUUID, userMessage, isSummaryQuestion ? 10 : 3, isSummaryQuestion ? 0.0 : 0.7))
                .build();

        // 流式输出
        Flux<String> res = QwenChatClient.prompt()
                .user(userMessage)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(questionAnswerAdvisor)
                .stream()
                .content();

        return res;
    }

    private boolean isSummaryQuestion(String userMessage) {
        return SUMMARY_PATTERN.matcher(userMessage.toLowerCase()).find();
    }

    private SearchRequest createSearchRequest(String pdfUUID, String userMessage, Integer cap, Double threshold) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        return SearchRequest.builder()
                .query(userMessage)
                .topK(cap)
                .similarityThreshold(threshold)
                .filterExpression(b.and(
                        b.eq("username", UserHolder.getUser()),
                        b.eq("pdfuuid", pdfUUID)
                ).build())
                .build();
    }
}
