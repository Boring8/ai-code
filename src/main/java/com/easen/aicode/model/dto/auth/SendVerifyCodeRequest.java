package com.easen.aicode.model.dto.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendVerifyCodeRequest implements Serializable {

    private String email;

}



