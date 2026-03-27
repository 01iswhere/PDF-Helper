package com.zero1iswhere.pdfhelper.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserChat {

    private String userName;

    private String chatId;

    private Integer status;

    private LocalDateTime createTime;
}
