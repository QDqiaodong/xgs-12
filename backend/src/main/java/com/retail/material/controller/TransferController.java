package com.retail.material.controller;

import com.retail.material.common.PageResult;
import com.retail.material.common.Result;
import com.retail.material.dto.TransferApproveDTO;
import com.retail.material.dto.TransferCreateDTO;
import com.retail.material.dto.TransferOutboundDTO;
import com.retail.material.dto.TransferQueryDTO;
import com.retail.material.dto.TransferReceiveDTO;
import com.retail.material.service.TransferService;
import com.retail.material.vo.TransferVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "调拨管理", description = "调拨相关接口")
@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "创建调拨申请")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<Long> createTransfer(@Valid @RequestBody TransferCreateDTO createDTO) {
        return Result.success("创建成功", transferService.createTransfer(createDTO));
    }

    @Operation(summary = "查询调拨列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<PageResult<TransferVO>> listTransfers(TransferQueryDTO queryDTO) {
        return Result.success(transferService.listTransfers(queryDTO));
    }

    @Operation(summary = "查询调拨详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER','STAFF')")
    public Result<TransferVO> getTransfer(
            @Parameter(description = "调拨单ID") @PathVariable Long id) {
        return Result.success(transferService.getTransferById(id));
    }

    @Operation(summary = "审批调拨")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Result<Void> approveTransfer(
            @Parameter(description = "调拨单ID") @PathVariable Long id,
            @Valid @RequestBody TransferApproveDTO approveDTO) {
        transferService.approveTransfer(id, approveDTO);
        return Result.success("审批成功", null);
    }

    @Operation(summary = "确认出库")
    @PutMapping("/{id}/outbound")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAREHOUSE')")
    public Result<Void> outboundTransfer(
            @Parameter(description = "调拨单ID") @PathVariable Long id,
            @Valid @RequestBody TransferOutboundDTO outboundDTO) {
        transferService.outboundTransfer(id, outboundDTO);
        return Result.success("出库成功", null);
    }

    @Operation(summary = "门店签收")
    @PutMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<Void> receiveTransfer(
            @Parameter(description = "调拨单ID") @PathVariable Long id,
            @Valid @RequestBody TransferReceiveDTO receiveDTO) {
        transferService.receiveTransfer(id, receiveDTO);
        return Result.success("签收成功", null);
    }

    @Operation(summary = "获取待审批列表")
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Result<List<TransferVO>> getPendingApprovalList() {
        return Result.success(transferService.getPendingApprovalList());
    }

    @Operation(summary = "获取待出库列表")
    @GetMapping("/pending-outbound")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','WAREHOUSE')")
    public Result<List<TransferVO>> getPendingOutboundList() {
        return Result.success(transferService.getPendingOutboundList());
    }

    @Operation(summary = "获取待签收列表")
    @GetMapping("/pending-receive")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','STORE_MANAGER')")
    public Result<List<TransferVO>> getPendingReceiveList() {
        return Result.success(transferService.getPendingReceiveList());
    }
}
