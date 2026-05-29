package org.example.ai4novel.mapper;

import org.apache.ibatis.annotations.*;
import org.example.ai4novel.entity.Novel;

import java.util.List;

@Mapper
public interface NovelMapper {

    @Select("SELECT * FROM novel ORDER BY create_time DESC")
    List<Novel> findAll();

    @Select("SELECT * FROM novel WHERE id = #{id}")
    Novel findById(String id);

    @Insert("INSERT INTO novel(id, name, description, folder_path, create_time) VALUES(#{id}, #{name}, #{description}, #{folderPath}, #{createTime})")
    void insert(Novel novel);

    @Delete("DELETE FROM novel WHERE id = #{id}")
    void deleteById(String id);
}
