package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.strategy.dto.StrategyAnalysisResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyDetailDto;
import com.be3c.sysmetic.domain.strategy.service.StrategyDetailService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/v1/strategy/detail")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class StrategyDetailController implements StrategyDetailControllerDocs {

    private final StrategyDetailService strategyDetailService;

    /*
        getDetailPage : 전략 상세 페이지 기본 정보 요청
        http://localhost:8080/v1/strategy/detail/1
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public APIResponse<StrategyDetailDto> getDetailPage(
            @PathVariable("id") Long id)  {

        StrategyDetailDto strategyDetailDto = strategyDetailService.getDetail(id);

        return APIResponse.success(strategyDetailDto);
    }

    /*
       getAnalysis : 전략 상세 페이지 그래프 데이터 요청
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/analysis/{id}")
    public APIResponse<StrategyAnalysisResponseDto> getAnalysis(
            @PathVariable("id") Long id) {

        return strategyDetailService.getAnalysis(id);
    }
}
