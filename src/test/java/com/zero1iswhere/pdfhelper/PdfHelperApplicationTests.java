package com.zero1iswhere.pdfhelper;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class PdfHelperApplicationTests {

    @Autowired
    private VectorStore vectorStore;

    @Test
    void ragsearch() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query("我的基本信息")
                .topK(5)
                .similarityThreshold(0.0)
                .filterExpression(b.and(
                        b.eq("username", "name"),
                        b.eq("pdfuuid", "uuid")).build())
                .build());
        for (Document document : documents) {
            System.out.println(document.getMetadata());
        }
    }

}
