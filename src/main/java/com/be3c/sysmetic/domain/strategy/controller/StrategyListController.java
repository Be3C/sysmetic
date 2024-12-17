package com.be3c.sysmetic.domain.strategy.controller;

import com.be3c.sysmetic.domain.member.entity.InterestStrategy;
import com.be3c.sysmetic.domain.member.repository.InterestStrategyRepository;
import com.be3c.sysmetic.domain.strategy.dto.*;
import com.be3c.sysmetic.domain.strategy.service.StrategyListService;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/strategy/list")
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class StrategyListController implements StrategyListControllerDocs {

    private final StrategyListService strategyListService;

    /*
        getStrategies : 전략 목록 페이지 조회 요청
        요청 경로 : localhost:8080/v1/strategy/list?pageNum=0
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping()
    public APIResponse<PageResponse<StrategyListDto>> getStrategies(
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum){
        PageResponse<StrategyListDto> strategyList = strategyListService.findStrategyPage(pageNum);

        if (strategyList.getContent().isEmpty())
            return APIResponse.fail(ErrorCode.BAD_REQUEST, "요청하신 페이지가 없습니다.");

        return APIResponse.success(strategyList);
    }


    /*
        searchByTrader : 트레이더 닉네임으로 검색, 전략 수 내림차순 정렬
        요청 경로 : localhost:8080/v1/strategy/list/trader?nickname=트레이더&pageNum=0
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/trader")
    public APIResponse<PageResponse<TraderNickNameListResponseDto>> searchByTraderNickname(
            @RequestParam("nickname") String nickname,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum) {

        PageResponse<TraderNickNameListResponseDto> traderList = strategyListService.findTraderNickname(nickname, pageNum);

        if (traderList.getContent().isEmpty())
            return APIResponse.fail(ErrorCode.BAD_REQUEST, "해당 닉네임을 가진 트레이더가 없습니다.");

        return APIResponse.success(traderList);
    }


    /*
        getStrategiesByTraderId : 트레이더별 전략 목록
        요청 경로 : localhost:8080/v1/strategy/list/pick?traderId=1&pageNum=0
    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/pick")
    public APIResponse<StrategyListByTraderDto> getStrategiesByTraderId(
            @RequestParam("traderId") Long traderId,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum) {
        StrategyListByTraderDto strategyListByTrader = strategyListService.findStrategiesByTrader(traderId, pageNum);

        if (strategyListByTrader.getStrategyListDto().getContent().isEmpty())
            return APIResponse.fail(ErrorCode.NOT_FOUND, "해당 트레이더가 등록한 전략이 없습니다.");

        return APIResponse.success(strategyListByTrader);
    }


    /*
        getStrategiesByName : 전략명으로 검색

    */
    @Override
    @PreAuthorize("permitAll()")
    @GetMapping("/name")
    public APIResponse<PageResponse<StrategyListDto>> getStrategiesByName(
            @RequestParam("keyword") String keyword,
            @RequestParam(name = "pageNum", defaultValue = "0") Integer pageNum) {

        PageResponse<StrategyListDto> strategyListByName = strategyListService.findStrategiesByName(keyword, pageNum);

        return APIResponse.success(strategyListByName);
    }
}