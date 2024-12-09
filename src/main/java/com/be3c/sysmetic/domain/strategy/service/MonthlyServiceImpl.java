package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.strategy.dto.MonthlyGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.be3c.sysmetic.domain.strategy.entity.Daily;
import com.be3c.sysmetic.domain.strategy.entity.Monthly;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.repository.DailyRepository;
import com.be3c.sysmetic.domain.strategy.repository.MonthlyRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.domain.strategy.util.DoubleHandler;
import com.be3c.sysmetic.domain.strategy.util.StrategyViewAuthorize;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@Service
public class MonthlyServiceImpl implements MonthlyService {
    /*
     월간분석 업데이트

        1. 업데이트된 날짜 받아서 년, 월 변환
        2. set 을 통해 중복 제거
        3. 중복 제거된 년, 월 다시 계산

     월간분석 조회

        1. 전략 식별번호, 기간, 페이지를 받는다.
        2. 전략 식별번호를 검증한다.
        3. 해당 기간의 월간분석 데이터를 한 페이지에 10개 노출한다.
     */

    private final MonthlyRepository monthlyRepository;
    private final DailyRepository dailyRepository;
    private final StrategyRepository strategyRepository;
    private final DoubleHandler doubleHandler;
    private final SecurityUtils securityUtils;
    private final StrategyViewAuthorize strategyViewAuthorize;

    // 월간분석 업데이트
    @Override
    @Transactional
    public void updateMonthly(Long strategyId, List<LocalDate> updatedDateList) {
        Set<YearMonth> yearMonthSet = updatedDateList.stream()
                .map(YearMonth::from)
                .collect(Collectors.toSet());

        yearMonthSet.forEach(yearMonth -> {
            int year = yearMonth.getYear();
            int month = yearMonth.getMonthValue();

            Optional<Monthly> existingMonthly = monthlyRepository.findByYearAndMonth(strategyId, year, month);

            existingMonthly.ifPresent(monthly ->
                    monthlyRepository.deleteSpecificMonthlyData(strategyId, year, month)
            );

            Monthly updatedMonthly = calculateMonthlyData(strategyId, year, month);
            monthlyRepository.saveAndFlush(updatedMonthly);
        });
    }

    // 월간분석 조회
    @Override
    public PageResponse<MonthlyGetResponseDto> findMonthly(Long strategyId, Integer page, String startYearMonth, String endYearMonth) {
        Pageable pageable = PageRequest.of(page, 10);

        YearMonth start = null;
        YearMonth end = null;

        if(startYearMonth != null && !startYearMonth.isEmpty()) {
            start = YearMonth.parse(startYearMonth);
        }

        if(endYearMonth != null && !endYearMonth.isEmpty()) {
            end = YearMonth.parse(endYearMonth);
        }

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        strategyViewAuthorize.Authorize(strategy);

        Page<MonthlyGetResponseDto> monthlyResponseDtoPage = monthlyRepository
                .findAllByStrategyIdAndDateBetween(strategyId, start, end, pageable)
                .map(this::entityToDto);

        // 페이지 내부에 데이터가 하나라도 존재한다면 ( 정상 페이지 요청 )
        if(monthlyResponseDtoPage.hasContent()) {
            return PageResponse.<MonthlyGetResponseDto>builder()
                    .currentPage(monthlyResponseDtoPage.getPageable().getPageNumber())
                    .pageSize(monthlyResponseDtoPage.getPageable().getPageSize())
                    .totalElement(monthlyResponseDtoPage.getTotalElements())
                    .totalPages(monthlyResponseDtoPage.getTotalPages())
                    .content(monthlyResponseDtoPage.getContent())
                    .build();
        }

        // 페이지 내부에 데이터가 하나라도 존재하지 않는다면 ( 비정상 페이지 요청 )
        throw new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND);
    }

    @Override
    public Monthly calculateMonthlyData(Long strategyId, int year, int month) {
        List<Daily> dailyList = dailyRepository.findAllByStrategyIdAndYearAndMonth(strategyId, year, month);

        Double totalProfitLossAmount = doubleHandler.cutDouble(dailyList.stream().mapToDouble(Daily::getProfitLossAmount).sum());
        Double totalProfitLossRate = doubleHandler.cutDouble(dailyList.stream().mapToDouble(Daily::getProfitLossRate).sum());
        Double totalPrincipal = doubleHandler.cutDouble(dailyList.stream().mapToDouble(Daily::getPrincipal).sum());
        Double averageMonthlyPrincipal = dailyList.isEmpty() ? 0 : doubleHandler.cutDouble(totalPrincipal/dailyList.size());
        Double accumulatedProfitLossAmount = dailyList.isEmpty() ? 0 : doubleHandler.cutDouble(dailyList.get(dailyList.size() - 1).getProfitLossAmount());
        Double accumulatedProfitLossRate = dailyList.isEmpty() ? 0 : doubleHandler.cutDouble(dailyList.get(dailyList.size() - 1).getProfitLossRate());

        return Monthly.builder()
                .strategy(findStrategy(strategyId))
                .yearNumber(year)
                .monthNumber(month)
                .averageMonthlyPrincipal(averageMonthlyPrincipal)
                .profitLossAmount(totalProfitLossAmount)
                .profitLossRate(totalProfitLossRate)
                .accumulatedProfitLossAmount(accumulatedProfitLossAmount)
                .accumulatedProfitLossRate(accumulatedProfitLossRate)
                .build();
    }

    private MonthlyGetResponseDto entityToDto(Monthly monthly) {
        return MonthlyGetResponseDto.builder()
                .monthId(monthly.getId())
                .yearMonth(monthly.getYearNumber() + "-" + monthly.getMonthNumber())
                .averagePrincipal(monthly.getAverageMonthlyPrincipal())
                .profitLossAmount(monthly.getProfitLossAmount())
                .profitLossRate(monthly.getProfitLossRate())
                .accumulatedProfitLossAmount(monthly.getAccumulatedProfitLossAmount())
                .accumulatedProfitLossRate(monthly.getAccumulatedProfitLossRate())
                .build();
    }

    private Strategy findStrategy(Long strategyId) {
        return strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));
    }

}
