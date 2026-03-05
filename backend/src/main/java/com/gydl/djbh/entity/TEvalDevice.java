package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/** 硬件测评设备 */
@Data
@TableName("t_eval_device")
public class TEvalDevice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String deviceNo;
    private String deviceName;
    private String deviceModel;
    /** 设备类型: 1=测评专用机 2=扫描设备 */
    private Integer deviceType;
    /** 归属人员ID（硬件设备才有） */
    private Long ownerStaffId;
    /** 状态: 1=正常 0=停用 */
    private Integer status;
    private String remark;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    private String dataHmac;

    @TableField(exist = false)
    private String ownerStaffName;
}
