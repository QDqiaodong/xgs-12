package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.InventoryExecuteDTO;
import com.retail.material.dto.InventoryPlanCreateDTO;
import com.retail.material.dto.InventoryPlanQueryDTO;
import com.retail.material.service.InventoryService;
import com.retail.material.vo.InventoryAnalysisVO;
import com.retail.material.vo.InventoryDetailVO;
import com.retail.material.vo.InventoryPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "盘点管理", description = "盘点相关接口")
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "创建盘点计划")
    @PostMapping("/plan")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Result<Long> createInventoryPlan(@Valid @RequestBody InventoryPlanCreateDTO createDTO) {
        return Result.success("创建成功", inventoryService.createInventoryPlan(createDTO));
    }

    @Operation(summary = "查询盘点计划列表")
    @GetMapping("/plan")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<PageResult<InventoryPlanVO>> listInventoryPlans(InventoryPlanQueryDTO queryDTO) {
        return Result.success(inventoryService.listInventoryPlans(queryDTO));
    }

    @Operation(summary = "更新盘点计划")
    @PutMapping("/plan/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Result<Void> updateInventoryPlan(
            @Parameter(description = "盘点计划ID") @PathVariable Long id,
            @Valid @RequestBody InventoryPlanCreateDTO updateDTO) {
        inventoryService.updateInventoryPlan(id, updateDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "执行盘点")
    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<Void> executeInventory(@Valid @RequestBody InventoryExecuteDTO executeDTO) {
        inventoryService.executeInventory(executeDTO);
        return Result.success("盘点执行成功", null);
    }

    @Operation(summary = "查询盘点记录")
    @GetMapping("/record/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<List<InventoryDetailVO>> getInventoryRecord(
            @Parameter(description = "盘点计划ID") @PathVariable Long planId) {
        return Result.success(inventoryService.getInventoryRecord(planId));
    }

    @Operation(summary = "盘点差异分析")
    @GetMapping("/analysis/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<InventoryAnalysisVO> getInventoryAnalysis(
            @Parameter(description = "盘点计划ID") @PathVariable Long planId) {
        return Result.success(inventoryService.getInventoryAnalysis(planId));
    }
}
