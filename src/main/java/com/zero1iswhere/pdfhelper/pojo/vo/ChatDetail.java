package com.zero1iswhere.pdfhelper.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ChatDetail {

    private String pdfUUID;

    private List<MessageVo> messageVos;

}
