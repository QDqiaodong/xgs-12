package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "门店创建请求")
public class StoreCreateDTO {

    @NotBlank(message = "门店编码不能为空")
    @Schema(description = "门店编码", required = true)
    private String storeCode;

    @NotBlank(message = "门店名称不能为空")
    @Schema(description = "门店名称", required = true)
    private String storeName;

    @NotBlank(message = "门店类型不能为空")
    @Schema(description = "门店类型：tea_milk/automotive/retail/warehouse", required = true)
    private String storeType;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status = 1;
}