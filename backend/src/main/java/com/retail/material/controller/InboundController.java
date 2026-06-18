package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.InboundCreateDTO;
import com.retail.material.dto.InboundQueryDTO;
import com.retail.material.service.InboundService;
import com.retail.material.vo.InboundVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "入库管理", description = "入库相关接口")
@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @Operation(summary = "创建入库单")
    @PostMapping
    public Result<Long> createInbound(@Valid @RequestBody InboundCreateDTO createDTO) {
        return Result.success("创建成功", inboundService.createInbound(createDTO));
    }

    @Operation(summary = "查询入库单列表")
    @GetMapping
    public Result<PageResult<InboundVO>> listInbounds(InboundQueryDTO queryDTO) {
        return Result.success(inboundService.listInbounds(queryDTO));
    }

    @Operation(summary = "查询入库单详情")
    @GetMapping("/{id}")
    public Result<InboundVO> getInbound(
            @Parameter(description = "入库单ID") @PathVariable Long id) {
        return Result.success(inboundService.getInboundById(id));
    }
}