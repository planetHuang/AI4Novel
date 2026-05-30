package org.example.ai4novel.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ai4novel.entity.ChatConversation;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ChatConversationMapper {

    @Select("SELECT * FROM chat_conversation WHERE novel_id = #{novelId} ORDER BY update_time DESC")
    List<ChatConversation> findByNovelId(String novelId);

    @Select("SELECT * FROM chat_conversation WHERE id = #{id}")
    ChatConversation findById(String id);

    @Insert("INSERT INTO chat_conversation (id, novel_id, title, create_time, update_time) " +
            "VALUES (#{id}, #{novelId}, #{title}, #{createTime}, #{updateTime})")
    int insert(ChatConversation conversation);

    @Update("UPDATE chat_conversation SET title = #{title}, update_time = #{updateTime} WHERE id = #{id}")
    int updateTitle(@Param("id") String id, @Param("title") String title, @Param("updateTime") LocalDateTime updateTime);

    @Update("UPDATE chat_conversation SET update_time = #{updateTime} WHERE id = #{id}")
    int updateTime(@Param("id") String id, @Param("updateTime") LocalDateTime updateTime);

    @Delete("DELETE FROM chat_conversation WHERE id = #{id}")
    int deleteById(String id);
}
