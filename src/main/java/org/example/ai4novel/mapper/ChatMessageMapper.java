package org.example.ai4novel.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ai4novel.entity.ChatMessage;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} ORDER BY id ASC")
    List<ChatMessage> findByConversationId(String conversationId);

    @Insert("INSERT INTO chat_message (conversation_id, role, content, create_time) " +
            "VALUES (#{conversationId}, #{role}, #{content}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Delete("DELETE FROM chat_message WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(String conversationId);
}
