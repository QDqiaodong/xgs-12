package com.retail.material.service;

import com.retail.material.common.PageResult;
import com.retail.material.dto.InboundCreateDTO;
import com.retail.material.dto.InboundQueryDTO;
import com.retail.material.vo.InboundVO;

public interface InboundService {

    PageResult<InboundVO> listInbounds(InboundQueryDTO queryDTO);

    InboundVO getInboundById(Long id);

    Long createInbound(InboundCreateDTO createDTO);
}