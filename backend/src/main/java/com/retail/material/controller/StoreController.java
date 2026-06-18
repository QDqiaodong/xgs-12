package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.StoreCreateDTO;
import com.retail.material.dto.StoreQueryDTO;
import com.retail.material.dto.StoreUpdateDTO;
import com.retail.material.service.StoreService;
import com.retail.material.vo.StoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "门店管理", description = "门店相关接口")
@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "查询门店列表")
    @GetMapping
    public Result<PageResult<StoreVO>> listStores(StoreQueryDTO queryDTO) {
        return Result.success(storeService.listStores(queryDTO));
    }

    @Operation(summary = "查询门店详情")
    @GetMapping("/{id}")
    public Result<StoreVO> getStore(
            @Parameter(description = "门店ID") @PathVariable Long id) {
        return Result.success(storeService.getStoreById(id));
    }

    @Operation(summary = "新增门店")
    @PostMapping
    public Result<Long> createStore(@Valid @RequestBody StoreCreateDTO createDTO) {
        return Result.success("创建成功", storeService.createStore(createDTO));
    }

    @Operation(summary = "更新门店信息")
    @PutMapping("/{id}")
    public Result<Void> updateStore(
            @Parameter(description = "门店ID") @PathVariable Long id,
            @Valid @RequestBody StoreUpdateDTO updateDTO) {
        storeService.updateStore(id, updateDTO);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除门店")
    @DeleteMapping("/{id}")
    public Result<Void> deleteStore(
            @Parameter(description = "门店ID") @PathVariable Long id) {
        storeService.deleteStore(id);
        return Result.success("删除成功", null);
    }
}