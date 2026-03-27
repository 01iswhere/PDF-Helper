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

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient QwenChatClient;

    private final MongoChatHistoryRepository chatHistoryRepository;

    private final VectorStore vectorStore;

    @Override
    public Flux<String> streamChat(String userMessage, String chatId, String pdfUUID) {
        // 当前chatId是否合规
        chatHistoryRepository.save(chatId);

        // RAG过滤检索
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(createSearchRequest(pdfUUID, userMessage))
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

    private SearchRequest createSearchRequest(String pdfUUID, String userMessage) {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        return SearchRequest.builder()
                .query(userMessage)
                .topK(5)
                .similarityThreshold(0.7)
                .filterExpression(b.and(
                        b.eq("username", UserHolder.getUser()),
                        b.eq("pdfuuid", pdfUUID)
                ).build())
                .build();
    }
}
