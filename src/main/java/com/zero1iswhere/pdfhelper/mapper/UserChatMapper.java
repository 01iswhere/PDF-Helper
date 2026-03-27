package com.zero1iswhere.pdfhelper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserChatMapper {

    void addUserChat(@Param("userName") String userName, @Param("chatId") String chatId);

    Integer getChatIdStatus(@Param("userName") String userName, @Param("chatId") String chatId);

    List<String> getAllChatIds(@Param("userName") String userName);
}
