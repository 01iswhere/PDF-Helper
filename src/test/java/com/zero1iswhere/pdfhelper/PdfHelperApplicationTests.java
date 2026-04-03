package com.zero1iswhere.pdfhelper;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.regex.Pattern;

@SpringBootTest
class PdfHelperApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void ragsearch() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query("简要概括这个pdf")
                .topK(10)
                .similarityThreshold(0.0)
                .filterExpression(b.and(
                        b.eq("username", "Zero1isWhere"),
                        b.eq("pdfuuid", "a68c6ba85e8445fbaab9b6de42cf0ffa")).build())
                .build());
        System.out.println(documents.size());
    }

    @Test
    void sumTest() {
        Pattern SUMMARY_PATTERN = Pattern.compile(".*(归纳|总结|概述|简要|大体|大概|整体|总体|全文).*");
        String userMessage = "简要概括这个pdf";
        boolean flag = SUMMARY_PATTERN.matcher(userMessage).find();
        System.out.println(flag);
        flag = SUMMARY_PATTERN.matcher(userMessage.toLowerCase()).find();
        System.out.println(flag);
    }

}
