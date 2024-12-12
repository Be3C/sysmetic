package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.strategy.dto.DailyGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.DailyRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.DailyPostResponseDto;
import com.be3c.sysmetic.domain.strategy.entity.StrategyStatistics;
import com.be3c.sysmetic.domain.strategy.repository.*;
import com.be3c.sysmetic.domain.strategy.entity.Daily;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.util.StrategyCalculator;
import com.be3c.sysmetic.domain.strategy.util.StrategyViewAuthorize;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@Slf4j
@Service
public class DailyServiceImpl implements DailyService {
    private final DailyRepository dailyRepository;
    private final MonthlyRepository monthlyRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyStatisticsRepository statisticsRepository;
    private final StrategyDetailService strategyDetailService;
    private final StrategyGraphAnalysisRepository strategyGraphAnalysisRepository;

    private final StrategyViewAuthorize strategyViewAuthorize;
    private final StrategyCalculator strategyCalculator;
    private final MonthlyServiceImpl monthlyService;
    private final SecurityUtils securityUtils;

    // 일간분석 등록
    @Transactional
    @Override
    public void saveDaily(Long strategyId, List<DailyRequestDto> requestDtoList) {
        Strategy exitingStrategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        // user 검증
        validUser(exitingStrategy.getTrader().getId());

        // 요청 객체 검증 및 entity 변환
        List<Daily> dailyList = requestDtoListToEntity(strategyId, null, requestDtoList);

        // 일간분석 등록
        dailyRepository.saveAll(dailyList);

        // 기존 반복 호출 제거
        LocalDate earliestDate = dailyList.stream()
                .map(Daily::getDate)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalArgumentException("Daily list is empty"));
        recalculateAccumulatedData(strategyId, earliestDate);

        // 월간분석 갱신
        List<LocalDate> updatedDateList = dailyList
                .stream().map(Daily::getDate).collect(Collectors.toList());

        monthlyService.updateMonthly(strategyId, updatedDateList);

        // StrategyGraphAnalysis 분석 그래프 등록 (분석 그래프)
        dailyList.forEach(daily -> strategyDetailService.saveAnalysis(strategyId, daily.getDate()));
    }

    // 일간분석 수정
    @Transactional
    @Override
    public void updateDaily(Long dailyId, DailyRequestDto requestDto) {
        Daily exitingDaily = dailyRepository.findById(dailyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        Long strategyId = exitingDaily.getStrategy().getId();

        Strategy exitingStrategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        // user 검증
        validUser(exitingStrategy.getTrader().getId());

        // 요청 객체 검증 및 entity 변환
        Daily daily = requestDtoListToEntity(strategyId, dailyId, List.of(requestDto))
                .stream().findFirst().orElseThrow(() ->
                        new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        // 일간분석 수정
        // 기존 save -> saveAndFlush 수정 (DailyRepository 바로 반영 필요)
        dailyRepository.saveAndFlush(daily);

        // 누적금액 갱신
        recalculateAccumulatedData(strategyId, daily.getDate());

        // 월간분석 갱신
        List<LocalDate> updatedDateList = List.of(daily.getDate());
        monthlyService.updateMonthly(strategyId, updatedDateList);

        // StrategyGraphAnalysis 분석 그래프 수정 (분석 그래프)
        strategyDetailService.updateAnalysis(strategyId, dailyId, daily.getDate());
    }

    // 일간분석 삭제
    @Transactional
    @Override
    public void deleteDaily(Long dailyId) {
        Daily exitingDaily = dailyRepository.findById(dailyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        Long strategyId = exitingDaily.getStrategy().getId();

        Strategy exitingStrategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        // user 검증
        validUser(exitingStrategy.getTrader().getId());

        // StrategyGraphAnalysis 분석 그래프 데이터 삭제 (삭제하는 existingDaily 전달)
        strategyDetailService.deleteAnalysis(strategyId, dailyId, exitingDaily);

        // DB 삭제
        dailyRepository.deleteById(dailyId);

        // 삭제 후 일간분석 데이터 수
        Long countDaily = countDaily(strategyId);

        // 누적금액 갱신
        recalculateAccumulatedData(strategyId, exitingDaily.getDate());

        // 월간분석 갱신
        List<LocalDate> updatedDateList = List.of(exitingDaily.getDate());
        monthlyService.updateMonthly(strategyId, updatedDateList);

        if(countDaily < 3) {
            // 일간분석 데이터 수가 3 미만일 경우 비공개 전환
            strategyRepository.updateStatusToPrivate(strategyId);
        }

        if(countDaily == 0) {
            // 모든 일간분석 데이터 삭제시 월간분석 데이터 삭제
            monthlyRepository.deleteAllByStrategyId(strategyId);
            // 전략 통계 데이터 선 삭제
            statisticsRepository.deleteByStrategyId(strategyId);
            // 전략 통계 데이터 후 초기화
            statisticsRepository.save(new StrategyStatistics(exitingStrategy));
            // 분석 그래프 데이터 삭제
            strategyGraphAnalysisRepository.deleteAllByStrategyId(strategyId);
        }
    }

    // 일간분석 조회 - PUBLIC 상태인 전략의 일간분석 데이터 조회
    @Override
    public PageResponse<DailyGetResponseDto> findDaily(Long strategyId, Integer page, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, 10);

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        strategyViewAuthorize.Authorize(strategy);

        Page<DailyGetResponseDto> dailyResponseDtoPage = dailyRepository
                .findAllByStrategyIdAndDateBetween(strategyId, startDate, endDate, pageable)
                .map(this::entityToDto);

        // 페이지 요청이 제대로 된 것인지 확인 ( 페이지 안에 데이터가 하나라도 존재한다면 )
        if(dailyResponseDtoPage.hasContent()) {
            return PageResponse.<DailyGetResponseDto>builder()
                    .currentPage(dailyResponseDtoPage.getPageable().getPageNumber())
                    .pageSize(dailyResponseDtoPage.getPageable().getPageSize())
                    .totalElement(dailyResponseDtoPage.getTotalElements())
                    .totalPages(dailyResponseDtoPage.getTotalPages())
                    .content(dailyResponseDtoPage.getContent())
                    .build();
        }

        // 페이지 안에 데이터가 하나라도 존재하지 않는다면 ( 잘못된 페이지 요청 )
        throw new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND);
    }

    // 중복 여부 조회
    @Override
    public DailyPostResponseDto getIsDuplicate(Long strategyId, List<DailyRequestDto> requestDtoList) {
        for (DailyRequestDto requestDto : requestDtoList) {
            // 이미 등록한 날짜가 하나라도 존재할 경우 true 반환
            if(dailyRepository.findByStrategyIdAndDate(strategyId, requestDto.getDate()) != null) {
                return DailyPostResponseDto.builder()
                        .isDuplicate(true)
                        .build();
            }
        }
        return DailyPostResponseDto.builder()
                .isDuplicate(false)
                .build();
    }

    private List<Daily> requestDtoListToEntity(Long strategyId, Long dailyId, List<DailyRequestDto> requestDtoList) {
        final Daily beforeDaily = dailyRepository.findTopByStrategyIdOrderByDateDesc(strategyId);
        List<Daily> toAddDailyList = new ArrayList<>();

        // 요청 데이터가 여러 개일 경우, date 기준 오래된 순으로 정렬
        if(requestDtoList.size() > 1) {
            requestDtoList.sort(Comparator.comparing(DailyRequestDto::getDate));
        }

        for(DailyRequestDto requestDto : requestDtoList) {
            Daily existingDaily = dailyRepository.findByStrategyIdAndDate(strategyId, requestDto.getDate());

            // 일간분석 등록이면서 이미 존재하는 일자일 경우 미저장
            if(dailyId == null && existingDaily != null) continue;

            // 입력한 값 중 이미 입력한 일자가 존재할 경우 미저장
            if(toAddDailyList.stream().map(toAddDaily -> toAddDaily.getDate()).toList().contains(requestDto.getDate())) {
                continue;
            }

            if(toAddDailyList.isEmpty()) {
                if(beforeDaily == null) {
                    // DB 데이터 미존재
                    toAddDailyList.add(dtoToEntity(dailyId, strategyId, true, requestDto, 0.0, 0.0, 0.0));
                } else {
                    // DB 데이터 존재
                    toAddDailyList.add(dtoToEntity(dailyId, strategyId, false, requestDto, beforeDaily.getPrincipal(), beforeDaily.getCurrentBalance(), beforeDaily.getStandardAmount()));
                }
            } else {
                // 요청 데이터가 여러 개일 경우, 이전 요청 데이터
                Daily beforeRequestDaily = toAddDailyList.get(toAddDailyList.size()-1);
                toAddDailyList.add(dtoToEntity(dailyId, strategyId, false, requestDto, beforeRequestDaily.getPrincipal(), beforeRequestDaily.getCurrentBalance(), beforeRequestDaily.getStandardAmount()));
            }
        }

        return toAddDailyList;
    }

    // 현재 로그인한 유저와 전략 업로드한 유저가 일치하는지 검증
    private void validUser(Long traderId) {
        if(!securityUtils.getUserIdInSecurityContext().equals(traderId)) {
            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_MEMBER.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

    // 일간분석 총 개수 조회
    private Long countDaily(Long strategyId) {
        return dailyRepository.countByStrategyId(strategyId);
    }

    private Daily dtoToEntity(Long dailyId, Long strategyId, boolean isFirst, DailyRequestDto requestDto, Double beforePrincipal, Double beforeBalance, Double beforeStandardAmount) {
        // 일손익률
        Double dailyProfitLossRate = strategyCalculator.getDailyProfitLossRate(
                isFirst,
                requestDto.getDepositWithdrawalAmount(),
                requestDto.getDailyProfitLossAmount(),
                beforeBalance,
                beforePrincipal,
                beforeStandardAmount
        );

        return Daily.builder()
                .id(dailyId) // 등록일 경우 null
                .strategy(strategyRepository.findById(strategyId).orElseThrow(() ->
                        new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND)))
                .date(requestDto.getDate())
                .principal(strategyCalculator.getPrincipal(isFirst, requestDto.getDepositWithdrawalAmount(), beforePrincipal, beforeBalance)) // 원금
                .depositWithdrawalAmount(requestDto.getDepositWithdrawalAmount()) // 입출금
                .profitLossAmount(requestDto.getDailyProfitLossAmount()) // 일손익금
                .profitLossRate(dailyProfitLossRate) // 일손익률
                .currentBalance(strategyCalculator.getCurrentBalance(isFirst, beforeBalance, requestDto.getDepositWithdrawalAmount(), requestDto.getDailyProfitLossAmount())) // 잔고
                .standardAmount(strategyCalculator.getStandardAmount(isFirst, requestDto.getDepositWithdrawalAmount(), requestDto.getDailyProfitLossAmount(), beforeBalance, beforePrincipal)) // 기준가
                .accumulatedProfitLossAmount(isFirst ? requestDto.getDailyProfitLossAmount() : getAccumulatedProfitLossAmount(strategyId, requestDto.getDailyProfitLossAmount())) // 누적손익금액
                .accumulatedProfitLossRate(isFirst ? dailyProfitLossRate : getAccumulatedProfitLossRate(strategyId, dailyProfitLossRate)) // 누적손익률
                .build();
    }

    private DailyGetResponseDto entityToDto(Daily daily) {
        return DailyGetResponseDto.builder()
                .dailyId(daily.getId())
                .date(daily.getDate())
                .principal(daily.getPrincipal())
                .depositWithdrawalAmount(daily.getDepositWithdrawalAmount())
                .profitLossAmount(daily.getProfitLossAmount())
                .profitLossRate(daily.getProfitLossRate())
                .accumulatedProfitLossAmount(daily.getAccumulatedProfitLossAmount())
                .accumulatedProfitLossRate(daily.getAccumulatedProfitLossRate())
                .build();
    }

    private Double getAccumulatedProfitLossAmount(Long strategyId, Double profitLossAmount) {
        Double accumulatedProfitLossAmount = dailyRepository.findTotalProfitLossAmountByStrategyId(strategyId);
        if(accumulatedProfitLossAmount == null) accumulatedProfitLossAmount = 0.0;
        accumulatedProfitLossAmount += profitLossAmount; // 현재 손익금 추가
        return accumulatedProfitLossAmount;
    }

    private Double getAccumulatedProfitLossRate(Long strategyId, Double profitLossRate) {
        Double accumulativeProfitLossRate = dailyRepository.findTotalProfitLossRateByStrategyId(strategyId);
        if(accumulativeProfitLossRate == null) accumulativeProfitLossRate = 0.0;
        accumulativeProfitLossRate += profitLossRate; // 현재 손익률 추가
        return accumulativeProfitLossRate;
    }

    public void recalculateAccumulatedData(Long strategyId, LocalDate startDate) {
        // startDate 이전의 누적 손익 금액 조회
        Double accumulativeProfitLossAmount = dailyRepository.findLastAccumulatedProfitLossAmount(strategyId, startDate)
                .orElse(0.0);

        // startDate 이후의 데이터 조회
        List<Daily> dailyList = dailyRepository.findAllByStrategyIdAndDateAfterOrderByDateAsc(strategyId, startDate);

        for (Daily daily : dailyList) {
            accumulativeProfitLossAmount += daily.getProfitLossAmount();
            daily.setAccumulatedProfitLossAmount(accumulativeProfitLossAmount);
        }

        dailyRepository.saveAll(dailyList);
    }
}
