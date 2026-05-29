package org.example.ai4novel.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ai4novel.entity.AiConfig;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiConfigMapper {

    @Select("SELECT * FROM ai_config ORDER BY is_default DESC, create_time DESC")
    List<AiConfig> findAll();

    @Select("SELECT * FROM ai_config WHERE id = #{id}")
    AiConfig findById(String id);

    @Select("SELECT * FROM ai_config WHERE is_default = TRUE LIMIT 1")
    AiConfig findDefault();

    @Insert("INSERT INTO ai_config(id, name, api_url, api_key, model, thinking_enabled, reasoning_effort, " +
            "is_default, create_time, update_time) " +
            "VALUES(#{id}, #{name}, #{apiUrl}, #{apiKey}, #{model}, #{thinkingEnabled}, #{reasoningEffort}, " +
            "#{isDefault}, #{createTime}, #{updateTime})")
    void insert(AiConfig aiConfig);

    @Update("UPDATE ai_config SET name = #{name}, api_url = #{apiUrl}, api_key = #{apiKey}, " +
            "model = #{model}, thinking_enabled = #{thinkingEnabled}, reasoning_effort = #{reasoningEffort}, " +
            "is_default = #{isDefault}, update_time = #{updateTime} WHERE id = #{id}")
    void update(AiConfig aiConfig);

    @Update("UPDATE ai_config SET is_default = FALSE WHERE is_default = TRUE")
    void clearAllDefault();

    @Update("UPDATE ai_config SET is_default = TRUE, update_time = #{updateTime} WHERE id = #{id}")
    void setDefault(String id, LocalDateTime updateTime);

    @Delete("DELETE FROM ai_config WHERE id = #{id}")
    void deleteById(String id);
}