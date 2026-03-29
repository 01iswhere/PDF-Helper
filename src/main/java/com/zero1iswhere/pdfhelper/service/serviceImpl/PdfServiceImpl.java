package com.zero1iswhere.pdfhelper.service.serviceImpl;

import com.zero1iswhere.pdfhelper.exception.ChatIdSaveException;
import com.zero1iswhere.pdfhelper.exception.ChatMemorySaveException;
import com.zero1iswhere.pdfhelper.exception.EmbeddingSaveException;
import com.zero1iswhere.pdfhelper.service.ChatHistoryRepository;
import com.zero1iswhere.pdfhelper.service.PdfService;
import com.zero1iswhere.pdfhelper.utils.Constant;
import com.zero1iswhere.pdfhelper.utils.RedisConstant;
import com.zero1iswhere.pdfhelper.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final VectorStore vectorStore;

    private final TokenTextSplitter tokenTextSplitter;

    private final ChatHistoryRepository chatHistoryRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public String upload(MultipartFile file, String chatId) throws IOException {

        List<Document> splitDocuments = new ArrayList<>();

        // 旧pdf的md5值
        Object md5 = redisTemplate.opsForValue().get(RedisConstant.PDF_CHAT_EXIST + chatId);
        String oldFileMd5 = md5 == null ? null : (String) md5;

        // 会话历史是否已经构建过
        Boolean chatHistoryExist = redisTemplate.opsForValue().get(RedisConstant.PDF_CHAT + chatId) == null ? Boolean.FALSE : Boolean.TRUE;

        try {
            // 文件校验
            if (Boolean.FALSE.equals(checkFile(file))) {
                throw new RuntimeException("文件校验不通过, 检查是否pdf过大或文件格式非pdf");
            }

            // 重复性检查
            if (Boolean.FALSE.equals(existFile(file ,chatId, oldFileMd5))) {
                throw new RuntimeException("pdf文件重复");
            }

            // 分词器解析PDF
            log.info("开始处理 PDF 文件：{}", file.getOriginalFilename());

            // 1. 读取 PDF 内容为 Document
            Document pdfToDocument = readPdfToDocument(file);

            // 2. 使用分块器进行分段
            splitDocuments = tokenTextSplitter.split(pdfToDocument);
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
            try {
                vectorStore.add(splitDocuments);
            } catch (Exception e) {
                throw new EmbeddingSaveException("向量存储异常");
            }
            log.info("PDF {} 向量化完成，UUID: {}", file.getOriginalFilename(), pdfUUID);

            try {
                // 5. 将新上传的pdf与会话关联, 存到redis中
                //    MySQL持久化会话记录
                redisTemplate.opsForValue().set(RedisConstant.PDF_CHAT + chatId, pdfUUID);
                if(Boolean.FALSE.equals(chatHistoryExist)) {
                    chatHistoryRepository.save(chatId);
                }
            } catch (Exception e) {
                throw new ChatIdSaveException("pdf与会话关联异常");
            }
            log.info("PDF {} 与会话关联完成，UUID: {}", file.getOriginalFilename(), pdfUUID);

            return pdfUUID;
        } catch (ChatIdSaveException e) {
            if(Boolean.FALSE.equals(chatHistoryExist)) {
                redisTemplate.delete(RedisConstant.PDF_CHAT + chatId);
            }
            rollback(splitDocuments, chatId, oldFileMd5);
            throw new RuntimeException(e.getMessage());
        } catch (EmbeddingSaveException e) {
            rollback(splitDocuments, chatId, oldFileMd5);
            throw new RuntimeException(e.getMessage());
        }
    }

    private Document readPdfToDocument(MultipartFile file) throws IOException {
        // 使用 PDFBox 读取 PDF 内容
        PDDocument pdDocument = Loader.loadPDF(file.getBytes());
        PDFTextStripper stripper = new PDFTextStripper();
        String content = stripper.getText(pdDocument);
        pdDocument.close();

        return new Document(content);
    }

    private Boolean checkFile(MultipartFile file) {
        // pdf格式
        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".pdf")) {
            return Boolean.FALSE;
        }
        // 文件大小
        if (file.getSize() > Constant.MAX_FILE_SIZE) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    private Boolean existFile(MultipartFile file, String chatId, String oldFileMd5) throws IOException {
        // 服务端是否已存在该文件
        String fileMd5 = DigestUtils.md5DigestAsHex(file.getBytes());
        if (!Strings.isBlank(oldFileMd5) && fileMd5.equals(oldFileMd5)) {
            return Boolean.FALSE;
        } else {
            redisTemplate.opsForValue().set(RedisConstant.PDF_CHAT_EXIST + chatId, fileMd5);
        }
        return Boolean.TRUE;
    }

    private void rollback(List<Document> splitDocuments, String chatId, String oldFileMd5) {
        vectorStore.delete(splitDocuments.stream().map(Document::getId).collect(Collectors.toList()));

        if(!StringUtils.isBlank(oldFileMd5)) {
            redisTemplate.opsForValue().set(RedisConstant.PDF_CHAT_EXIST + chatId, oldFileMd5);
        } else {
            redisTemplate.delete(RedisConstant.PDF_CHAT_EXIST + chatId);
        }
    }
}
