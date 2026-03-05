package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 项目人员清单 */
@Data
@TableName("t_staff")
public class TStaff {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String staffNo;
    /** SM4加密 */
    private String realName;
    private String department;
    private String position;
    private String roleLevel;
    /** SM4加密 */
    private String certNo;
    private LocalDate certExpire;
    /** SM4加密 */
    private String phone;
    /** SM4加密 */
    private String email;
    private Integer status;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private String dataHmac;
}
