package com.zero1iswhere.pdfhelper.utils;

import lombok.Getter;

@Getter
public enum UserChatStatus {
    DELETED(0, "已删除"),
    ACTIVE(1, "未删除");

    private final int code;
    private final String status;

    UserChatStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }


}
