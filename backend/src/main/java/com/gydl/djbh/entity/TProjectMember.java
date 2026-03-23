package com.gydl.djbh.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目人员关联表
 */
@Data
@TableName("t_project_member")
public class TProjectMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long memberId;

    /**
     * 角色类型：
     * project_manager / project_leader /
     * registered_evaluator / actual_member /
     * survey_editor / plan_editor / report_editor /
     * network_evaluator / host_evaluator / physical_evaluator /
     * tool_scanner / pentest_member
     */
    private String roleType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    private String dataHmac;
}
