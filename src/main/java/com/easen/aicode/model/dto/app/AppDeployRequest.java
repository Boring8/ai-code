package com.easen.aicode.model.dto.app;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    private static final long serialVersionUID = 1L;
}
