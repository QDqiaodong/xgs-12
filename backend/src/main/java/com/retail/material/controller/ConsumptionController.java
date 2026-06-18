package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.ConsumptionCreateDTO;
import com.retail.material.dto.ConsumptionQueryDTO;
import com.retail.material.service.ConsumptionService;
import com.retail.material.vo.ConsumptionVO;
import com.retail.material.vo.MonthlyConsumptionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "消耗登记", description = "消耗登记相关接口")
@RestController
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @Operation(summary = "登记月度消耗")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<Long> createConsumption(@Valid @RequestBody ConsumptionCreateDTO createDTO) {
        return Result.success("登记成功", consumptionService.createConsumption(createDTO));
    }

    @Operation(summary = "月度消耗统计")
    @GetMapping("/monthly")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<MonthlyConsumptionVO> getMonthlyConsumption(
            @Parameter(description = "门店ID") @RequestParam Long storeId,
            @Parameter(description = "年份") @RequestParam Integer year,
            @Parameter(description = "月份") @RequestParam Integer month) {
        return Result.success(consumptionService.getMonthlyConsumption(storeId, year, month));
    }

    @Operation(summary = "门店消耗记录")
    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<List<ConsumptionVO>> getStoreConsumptions(
            @Parameter(description = "门店ID") @PathVariable Long storeId) {
        return Result.success(consumptionService.getStoreConsumptions(storeId));
    }

    @Operation(summary = "查询消耗记录列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<PageResult<ConsumptionVO>> listConsumptions(ConsumptionQueryDTO queryDTO) {
        return Result.success(consumptionService.listConsumptions(queryDTO));
    }

    @Operation(summary = "更新消耗记录")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<Void> updateConsumption(
            @Parameter(description = "消耗记录ID") @PathVariable Long id,
            @Valid @RequestBody ConsumptionCreateDTO updateDTO) {
        consumptionService.updateConsumption(id, updateDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除消耗记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Result<Void> deleteConsumption(
            @Parameter(description = "消耗记录ID") @PathVariable Long id) {
        consumptionService.deleteConsumption(id);
        return Result.success("删除成功", null);
    }
}
