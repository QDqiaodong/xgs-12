package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "门店更新请求")
public class StoreUpdateDTO {

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "门店类型：tea_milk/automotive/retail/warehouse")
    private String storeType;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "状态：0禁用 1启用")
    private Integer status;
}