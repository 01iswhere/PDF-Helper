package com.zero1iswhere.pdfhelper.service.serviceImpl;

import com.zero1iswhere.pdfhelper.service.ChatHistoryRepository;
import com.zero1iswhere.pdfhelper.service.PdfService;
import com.zero1iswhere.pdfhelper.utils.Constant;
import com.zero1iswhere.pdfhelper.utils.RedisConstant;
import com.zero1iswhere.pdfhelper.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter;

    private final ChatHistoryRepository chatHistoryRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public List<String> upload(MultipartFile file, String chatId) throws IOException {
        // 文件校验
        if(Boolean.FALSE.equals(checkFiles(file, chatId))) {
            throw new RuntimeException("文件校验不通过, 检查是否pdf过大或文件格式非pdf");
        }

        // 分词器解析PDF
        List<String> pdfUUIDs = new ArrayList<>();
        log.info("开始处理 PDF 文件：{}", file.getOriginalFilename());

        // 1. 读取 PDF 内容为 Document
        Document pdfToDocument = readPdfToDocument(file);

        // 2. 使用分块器进行分段
        List<Document> splitDocuments = tokenTextSplitter.split(pdfToDocument);
        log.info("PDF {} 分块完成，共 {} 个分块", file.getOriginalFilename(), splitDocuments.size());

        // 3. 为每个文件添加元数据
        String pdfUUID = UUID.randomUUID().toString().replaceAll("-", "");
        for (int i = 0; i < splitDocuments.size(); i++) {
            Document doc = splitDocuments.get(i);
            Map<String, Object> metadata = doc.getMetadata();
            metadata.put("username", UserHolder.getUser());
            metadata.put("pdfuuid", pdfUUID);
        }

        // 4. 保存到向量存储
        vectorStore.add(splitDocuments);
        pdfUUIDs.add(pdfUUID);
        log.info("PDF {} 向量化完成，UUID: {}", file.getOriginalFilename(), pdfUUID);

        // 将上传的pdf与会话关联, 存到redis中
        redisTemplate.opsForSet().add(RedisConstant.PDF_CHAT + chatId, pdfUUIDs.toArray(new String[0]));
        chatHistoryRepository.save(chatId);
        return pdfUUIDs;
    }

    private Document readPdfToDocument(MultipartFile file) throws IOException {
        // 使用 PDFBox 读取 PDF 内容
        PDDocument pdDocument = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(pdDocument);
        pdDocument.close();

        return new Document(content);
    }

    private Boolean checkFiles(MultipartFile file, String chatId) throws IOException {
        // pdf格式
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".pdf")) {
            return Boolean.FALSE;
        }
        // 文件大小
        if (file.getSize() > Constant.MAX_FILE_SIZE) {
            return Boolean.FALSE;
        }
        // 服务端是否已存在该文件
        String fileMd5 = DigestUtils.md5DigestAsHex(file.getBytes());
        if(Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConstant.PDF_CHAT_EXIST + chatId, fileMd5))) {
            return Boolean.FALSE;
        } else {
            redisTemplate.opsForSet().add(RedisConstant.PDF_CHAT_EXIST + chatId, fileMd5);
        }
        return Boolean.TRUE;
    }
}
