package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.TransferApproveDTO;
import com.retail.material.dto.TransferCreateDTO;
import com.retail.material.dto.TransferOutboundDTO;
import com.retail.material.dto.TransferQueryDTO;
import com.retail.material.dto.TransferReceiveDTO;
import com.retail.material.vo.TransferVO;

import java.util.List;

public interface TransferService {

    Long createTransfer(TransferCreateDTO createDTO);

    PageResult<TransferVO> listTransfers(TransferQueryDTO queryDTO);

    TransferVO getTransferById(Long id);

    void approveTransfer(Long id, TransferApproveDTO approveDTO);

    void outboundTransfer(Long id, TransferOutboundDTO outboundDTO);

    void receiveTransfer(Long id, TransferReceiveDTO receiveDTO);

    List<TransferVO> getPendingApprovalList();

    List<TransferVO> getPendingOutboundList();

    List<TransferVO> getPendingReceiveList();
}
