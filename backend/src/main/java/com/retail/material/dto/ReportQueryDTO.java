package com.retail.material.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报表查询请求")
public class ReportQueryDTO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "年份")
    private Integer year;

    @Schema(description = "月份")
    private Integer month;

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "耗材ID")
    private Long materialId;

    @Schema(description = "分类")
    private String category;
}