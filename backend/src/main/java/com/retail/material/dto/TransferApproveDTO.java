package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "调拨审批请求")
public class TransferApproveDTO {

    @NotNull(message = "审批结果不能为空")
    @Schema(description = "是否通过", required = true)
    private Boolean approved;

    @Schema(description = "审批备注")
    private String remark;
}