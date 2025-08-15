package com.easen.aicode.model.dto;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Data
public class ChatToGenCodeRequest {
    /**
     * appId
     */
    private Long appId;

    /**
     * 提示词
     */
    private String message;

    /**
     * 图片
     */
    private List<String> image;
}
