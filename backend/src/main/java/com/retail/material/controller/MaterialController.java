package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.MaterialCreateDTO;
import com.retail.material.dto.MaterialQueryDTO;
import com.retail.material.dto.MaterialUpdateDTO;
import com.retail.material.service.MaterialService;
import com.retail.material.vo.MaterialVO;
import com.retail.material.vo.StockAlertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "耗材管理", description = "耗材相关接口")
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @Operation(summary = "查询耗材列表")
    @GetMapping
    public Result<PageResult<MaterialVO>> listMaterials(MaterialQueryDTO queryDTO) {
        return Result.success(materialService.listMaterials(queryDTO));
    }

    @Operation(summary = "查询耗材详情")
    @GetMapping("/{id}")
    public Result<MaterialVO> getMaterial(
            @Parameter(description = "耗材ID") @PathVariable Long id) {
        return Result.success(materialService.getMaterialById(id));
    }

    @Operation(summary = "新增耗材")
    @PostMapping
    public Result<Long> createMaterial(@Valid @RequestBody MaterialCreateDTO createDTO) {
        return Result.success("创建成功", materialService.createMaterial(createDTO));
    }

    @Operation(summary = "更新耗材信息")
    @PutMapping("/{id}")
    public Result<Void> updateMaterial(
            @Parameter(description = "耗材ID") @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateDTO updateDTO) {
        materialService.updateMaterial(id, updateDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除耗材")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMaterial(
            @Parameter(description = "耗材ID") @PathVariable Long id) {
        materialService.deleteMaterial(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "查询库存预警列表")
    @GetMapping("/stock")
    public Result<List<StockAlertVO>> getStockAlerts() {
        return Result.success(materialService.getStockAlerts());
    }
}