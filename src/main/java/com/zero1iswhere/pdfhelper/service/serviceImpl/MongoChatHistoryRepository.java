package com.zero1iswhere.pdfhelper.service.serviceImpl;

import com.zero1iswhere.pdfhelper.exception.ChatMemorySaveException;
import com.zero1iswhere.pdfhelper.mapper.UserChatMapper;
import com.zero1iswhere.pdfhelper.pojo.vo.ChatDetail;
import com.zero1iswhere.pdfhelper.pojo.vo.MessageVo;
import com.zero1iswhere.pdfhelper.service.ChatHistoryRepository;
import com.zero1iswhere.pdfhelper.utils.RedisConstant;
import com.zero1iswhere.pdfhelper.utils.UserChatStatus;
import com.zero1iswhere.pdfhelper.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MongoChatHistoryRepository implements ChatHistoryRepository {

    private final UserChatMapper userChatMapper;

    private final ChatMemory mongoChatMemory;

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void save(String chatId) {
        String userName = UserHolder.getUser();
        Integer status = userChatMapper.getChatIdStatus(userName, chatId);
        if(status != null && status.equals(UserChatStatus.DELETED.getCode())) {
            throw new ChatMemorySaveException("会话不存在或权限不足");
        }
        if(status != null && status.equals(UserChatStatus.ACTIVE.getCode())) {
            return;
        }
        userChatMapper.addUserChat(userName, chatId);
    }

    @Override
    public List<String> getChatIds() {
        String userName = UserHolder.getUser();
        List<String> res = userChatMapper.getAllChatIds(userName);
        return res;
    }

    @Override
    public ChatDetail getChatDetail(String chatId) {
        List<Message> messages = mongoChatMemory.get(chatId);
        List<MessageVo> messageVos = messages.stream()
                .map(MessageVo::new)
                .collect(Collectors.toList());

        String pdfUUID = (String) redisTemplate.opsForValue().get(RedisConstant.PDF_CHAT + chatId);
        ChatDetail res = new ChatDetail();
        res.setMessageVos(messageVos);
        res.setPdfUUID(pdfUUID);
        return res;
    }
}