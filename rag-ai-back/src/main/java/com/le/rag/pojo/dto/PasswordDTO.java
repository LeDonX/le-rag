package com.le.rag.pojo.dto;

import lombok.Data;

/**
 * @Title: PasswardDTO
 * @Author LeDon
 * @Package com.le.rag.pojo.dto
 * @description: 修改密码DTO
 */

@Data
public class PasswordDTO {
    private Integer id;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
