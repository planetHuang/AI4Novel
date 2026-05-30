package org.example.ai4novel.service;

import org.example.ai4novel.entity.ChatConversation;
import org.example.ai4novel.entity.ChatMessage;
import org.example.ai4novel.mapper.ChatConversationMapper;
import org.example.ai4novel.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;

    public ChatService(ChatConversationMapper conversationMapper, ChatMessageMapper messageMapper) {
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
    }

    public List<ChatConversation> getConversations(String novelId) {
        return conversationMapper.findByNovelId(novelId);
    }

    public ChatConversation createConversation(String novelId, String title) {
        String id = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        ChatConversation conv = new ChatConversation(id, novelId, title, now, now);
        conversationMapper.insert(conv);
        return conv;
    }

    public Map<String, Object> getConversationWithMessages(String conversationId) {
        ChatConversation conv = conversationMapper.findById(conversationId);
        if (conv == null) return null;
        List<ChatMessage> messages = messageMapper.findByConversationId(conversationId);
        Map<String, Object> result = new HashMap<>();
        result.put("conversation", conv);
        result.put("messages", messages);
        return result;
    }

    @Transactional
    public void deleteConversation(String conversationId) {
        messageMapper.deleteByConversationId(conversationId);
        conversationMapper.deleteById(conversationId);
    }

    public void saveMessage(String conversationId, String role, String content) {
        ChatMessage msg = new ChatMessage(conversationId, role, content, LocalDateTime.now());
        messageMapper.insert(msg);
    }

    public void updateConversationTitle(String conversationId, String title) {
        conversationMapper.updateTitle(conversationId, title, LocalDateTime.now());
    }

    public void touchConversation(String conversationId) {
        conversationMapper.updateTime(conversationId, LocalDateTime.now());
    }
}
