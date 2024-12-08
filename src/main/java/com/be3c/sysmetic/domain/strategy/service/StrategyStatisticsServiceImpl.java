package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.strategy.dto.StrategyStatisticsGetResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.be3c.sysmetic.domain.strategy.entity.Daily;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.entity.StrategyStatistics;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.repository.DailyRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyStatisticsRepository;
import com.be3c.sysmetic.domain.strategy.util.StrategyViewAuthorize;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.util.SecurityUtils;
import com.be3c.sysmetic.domain.strategy.util.DoubleHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class StrategyStatisticsServiceImpl implements StrategyStatisticsService {

    private final DailyRepository dailyRepository;
    private final StrategyStatisticsRepository strategyStatisticsRepository;
    private final StrategyRepository strategyRepository;
    private final DoubleHandler doubleHandler;
    private final StrategyViewAuthorize strategyViewAuthorize;

    // 전략통계 DB 저장 - SchedulerConfiguration 에서 호출하는 메서드
    public void runStrategyStatistics(Long strategyId) {

        // 최근 일간분석데이터 조회
        final Daily recentDaily = dailyRepository.findTopByStrategyIdOrderByDateDesc(strategyId);

        // 최근 통계 조회
        final Strategy existingStrategy = strategyRepository.findById(strategyId).orElseThrow(() -> new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));
        final StrategyStatistics savedStatistics = getOrCreateStatistics(existingStrategy);

        if(recentDaily == null) {
            return;
        }

        // 다회 사용시 여러번 조회하지 않도록 저장하여 사용
        final Daily firstDaily = dailyRepository.findTopByStrategyIdOrderByDateAsc(strategyId);
        final Daily lastDaily = dailyRepository.findTopByStrategyIdOrderByDateDesc(strategyId);

        final Double accumulatedProfitLossAmount = getAccumulatedProfitLossAmount(recentDaily, strategyId);
        final Double accumulatedProfitLossRate = getAccumulatedProfitLossRate(recentDaily);

        final Double maxAccumulatedProfitLossAmount = getMaximumAccumulatedProfitLossAmount(savedStatistics, accumulatedProfitLossAmount);
        final Double currentCapitalReductionAmount = getCurrentCapitalReductionAmount(recentDaily, accumulatedProfitLossAmount, maxAccumulatedProfitLossAmount);
        final Double currentCapitalReductionRate = getCurrentCapitalReductionRate(strategyId, recentDaily);
        final Double maximumCapitalReductionAmount = getMaximumCapitalReductionAmount(savedStatistics, currentCapitalReductionAmount);

        final Long totalTradingDays = dailyRepository.countByStrategyId(strategyId);
        final Long totalProfitDays = getTotalProfitDays(strategyId, recentDaily);

        final StrategyStatistics saveStatistics = StrategyStatistics.builder()
                .id(savedStatistics.getId())
                .strategy(findStrategyById(strategyId))
                .currentBalance(getBalance(recentDaily))
                .principal(getPrincipal(recentDaily))
                .accumulatedDepositWithdrawalAmount(getAccumulatedDepositWithdrawalAmount(recentDaily, strategyId))
                .accumulatedProfitLossAmount(accumulatedProfitLossAmount)
                .accumulatedProfitLossRate(accumulatedProfitLossRate)
                .maximumAccumulatedProfitLossAmount(maxAccumulatedProfitLossAmount)
                .maximumAccumulatedProfitLossRate(getMaximumAccumulatedProfitLossRate(savedStatistics, accumulatedProfitLossRate))
                .currentCapitalReductionAmount(currentCapitalReductionAmount)
                .currentCapitalReductionRate(currentCapitalReductionRate)
                .maximumCapitalReductionAmount(maximumCapitalReductionAmount)
                .maximumCapitalReductionRate(getMaximumCapitalReductionRate(savedStatistics, currentCapitalReductionRate))
                .averageProfitLossAmount(getAverageProfitLossAmount(totalTradingDays, accumulatedProfitLossAmount))
                .averageProfitLossRate(getAverageProfitLossRate(totalTradingDays, accumulatedProfitLossRate))
                .maximumDailyProfitAmount(getMaximumProfitAmount(strategyId, recentDaily))
                .maximumDailyProfitRate(getMaximumProfitRate(strategyId, recentDaily))
                .maximumDailyLossAmount(getMaximumLossAmount(strategyId, recentDaily))
                .maximumDailyLossRate(getMaximumLossRate(strategyId, recentDaily))
                .totalTradingDays(totalTradingDays)
                .currentContinuousProfitLossDays(getContinuousProfitLossDays(strategyId, recentDaily))
                .totalProfitDays(totalProfitDays)
                .maximumContinuousProfitDays(getMaxContinuousProfitDays(strategyId, recentDaily))
                .totalLossDays(getTotalLossDays(strategyId, recentDaily))
                .maximumContinuousLossDays(getMaxContinuousLossDays(strategyId, recentDaily))
                .winningRate(getWinningRate(recentDaily, totalProfitDays, totalTradingDays))
                .highPointRenewalProgress(getHighPointRenewalProgress(strategyId, recentDaily))
                .profitFactor(getProfitFactor(strategyId, recentDaily))
                .roa(getRoa(recentDaily, accumulatedProfitLossAmount, maximumCapitalReductionAmount))
                .firstRegistrationDate(firstDaily.getDate())
                .lastRegistrationDate(lastDaily.getDate())
                .build();

        // 계산 후 DB 저장
        strategyStatisticsRepository.save(saveStatistics);
    }

    public StrategyStatistics getOrCreateStatistics(Strategy strategy) {
        // 존재하면 반환, 존재하지 않으면 생성
        return strategyStatisticsRepository.findByStrategyId(strategy.getId())
                .orElseGet(() -> {
                    StrategyStatistics newStatistics = new StrategyStatistics(strategy);
                    return strategyStatisticsRepository.save(newStatistics);
                });
    }

    // 전략통계 조회
    public StrategyStatisticsGetResponseDto findStrategyStatistics(Long strategyId) {
        StrategyStatistics statistics = strategyStatisticsRepository.findByStrategyId(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));

        strategyViewAuthorize.Authorize(strategy);

        return StrategyStatisticsGetResponseDto.getResponseDto(statistics, calculateOperationPeriod(statistics.getFirstRegistrationDate(), statistics.getLastRegistrationDate()));
    }

    // find strategy by id
    private Strategy findStrategyById(Long strategyId) {
        return strategyRepository.findById(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));
    }

    // 잔고 -> 가장 최근 일간분석 데이터의 잔고
    private Double getBalance(Daily recentDaily) {
        return recentDaily == null ? 0.0 : recentDaily.getCurrentBalance();
    }

    // 원금 -> 가장 최근 일간분석 데이터의 원금
    private Double getPrincipal(Daily recentDaily) {
        return recentDaily == null ? 0.0 : recentDaily.getPrincipal();
    }

    // 누적입출금액 -> 일간분석 데이터의 입출금액 총합
    // 시스메틱에서 제공한 자료에서는 첫 입금은 계산하지 않음. 반영 필요한가? todo
    private Double getAccumulatedDepositWithdrawalAmount(Daily recentDaily, Long strategyId) {
        return recentDaily == null ? 0.0 : dailyRepository.findTotalDepositWithdrawalAmountByStrategyId(strategyId);
    }

    // 누적손익금액
    private Double getAccumulatedProfitLossAmount(Daily recentDaily, Long strategyId) {
        return recentDaily == null ? 0.0 : dailyRepository.findTotalProfitLossAmountByStrategyId(strategyId);
    }

    // 누적손익률 -> 기준가 / 1000 - 1
    private Double getAccumulatedProfitLossRate(Daily recentDaily) {
        return recentDaily == null ? 0.0 : doubleHandler.cutDouble(recentDaily.getStandardAmount() / 1000 - 1);
    }

    // 최대누적손익금액
    private Double getMaximumAccumulatedProfitLossAmount(StrategyStatistics savedStatistics, Double accumulatedProfitLossAmount) {
        if(savedStatistics == null) return accumulatedProfitLossAmount;

        Double savedMaxValue = savedStatistics.getMaximumAccumulatedProfitLossAmount();
        return Math.max(savedMaxValue, accumulatedProfitLossAmount);
    }

    // 최대누적손익률
    private Double getMaximumAccumulatedProfitLossRate(StrategyStatistics savedStatistics, Double accumulatedProfitLossRate) {
        if(savedStatistics == null) return accumulatedProfitLossRate;

        Double savedMaxValue = savedStatistics.getMaximumAccumulatedProfitLossRate();
        return Math.max(savedMaxValue, accumulatedProfitLossRate);
    }

    // 현재자본인하금액 -> 누적손익 > 0 ? 누적손익 - 최대누적손익 : 0
    private Double getCurrentCapitalReductionAmount(Daily recentDaily, Double accumulatedProfitLossAmount, Double maxAccumulatedProfitLossAmount) {
        if(recentDaily == null) return 0.0;

        return accumulatedProfitLossAmount > 0 ? accumulatedProfitLossAmount - maxAccumulatedProfitLossAmount : 0;
    }

    // 현재자본인하율 -> 기준가 - 1000 > 0 ? (기준가 - 최대 기준가) / 기준가 : 0
    private double getCurrentCapitalReductionRate(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        Double standardAmount = recentDaily.getStandardAmount();
        Double maxStandardAmount = dailyRepository.findTopByStrategyIdOrderByStandardAmountDesc(strategyId).getStandardAmount();

        if(standardAmount == null || standardAmount == 0) return 0.0;

        return standardAmount - 1000 > 0 ? doubleHandler.cutDouble((standardAmount - maxStandardAmount) / standardAmount * 100) : 0.0;
    }

    // 최대자본인하금액
    private Double getMaximumCapitalReductionAmount(StrategyStatistics savedStatistics, Double currentCapitalReductionAmount) {
        if(savedStatistics == null) return currentCapitalReductionAmount;

        return Math.max(savedStatistics.getMaximumCapitalReductionAmount(), currentCapitalReductionAmount);
    }

    // 최대자본인하율
    private Double getMaximumCapitalReductionRate(StrategyStatistics savedStatistics, Double currentCapitalReductionRate) {
        if(savedStatistics == null) return currentCapitalReductionRate;

        return Math.max(savedStatistics.getMaximumCapitalReductionRate(), currentCapitalReductionRate);
    }

    // 평균손익금 -> 누적손익금액 / 일간분석 개수(=매매일수)
    private Double getAverageProfitLossAmount(Long totalTradingDays, Double accumulatedProfitLossAmount) {
        if(totalTradingDays == null || totalTradingDays == 0L) return 0.0;

        return doubleHandler.cutDouble(accumulatedProfitLossAmount / totalTradingDays);
    }

    // 평균손익률 -> 누적손익율 / 일간분석 개수(=매매일수)
    private Double getAverageProfitLossRate(Long totalTradingDays, Double averageProfitLossRate) {
        if(totalTradingDays == null || totalTradingDays == 0L) return 0.0;

        return doubleHandler.cutDouble(averageProfitLossRate / totalTradingDays * 100);
    }

    // 최대일이익금
    private Double getMaximumProfitAmount(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        return dailyRepository.findTopByStrategyIdOrderByProfitLossAmountDesc(strategyId).getProfitLossAmount();
    }

    // 최대일이익률
    private Double getMaximumProfitRate(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        return dailyRepository.findTopByStrategyIdOrderByProfitLossRateDesc(strategyId).getProfitLossRate();
    }

    // 최대일손실금
    private Double getMaximumLossAmount(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        return dailyRepository.findTopByStrategyIdOrderByProfitLossAmountAsc(strategyId).getProfitLossAmount();
    }

    // 최대일손실률
    private Double getMaximumLossRate(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        return dailyRepository.findTopByStrategyIdOrderByProfitLossRateAsc(strategyId).getProfitLossRate();
    }

    // 현재연속손익일수
    private Long getContinuousProfitLossDays(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        return dailyRepository.findContinuousProfitLossDays(strategyId);
    }

    // 총이익일수
    private Long getTotalProfitDays(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        return dailyRepository.countProfitDays(strategyId);
    }

    // 최대연속이익일수
    private Long getMaxContinuousProfitDays(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        Long maxConsecutiveProfitDays = dailyRepository.findMaxConsecutiveProfitDays(strategyId);
        return maxConsecutiveProfitDays != null ? maxConsecutiveProfitDays : 0L;
    }

    // 총손실일수
    private Long getTotalLossDays(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        return dailyRepository.countLossDays(strategyId);
    }

    // 최대연속손실일수
    private Long getMaxContinuousLossDays(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        Long maxConsecutiveLossDays = dailyRepository.findMaxConsecutiveLossDays(strategyId);
        return maxConsecutiveLossDays != null ? maxConsecutiveLossDays : 0L;
    }

    // 승률 -> 이익일수 / 거래일수
    private Double getWinningRate(Daily recentDaily, Long totalProfitDays, Long totalTradingDays) {
        if(recentDaily == null || totalTradingDays == 0L) return 0.0;

        return doubleHandler.cutDouble((double) (totalProfitDays / totalTradingDays));
    }

    // 고점갱신 후 경과일
    private Long getHighPointRenewalProgress(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0L;

        return dailyRepository.findHighPointRenewalProgress(strategyId);
    }

    // profit factor -> 총손실금액 < 0 ? 총이익금액 / |총손실금액| : 0
    private Double getProfitFactor(Long strategyId, Daily recentDaily) {
        if(recentDaily == null) return 0.0;

        Double totalProfitAmount = dailyRepository.findTotalProfitAmountByStrategyId(strategyId);
        Double totalLossAmount = dailyRepository.findTotalLossAmountByStrategyId(strategyId);

        if(totalProfitAmount == null || totalLossAmount == null || totalLossAmount == 0) return 0.0;

        return totalLossAmount < 0 ? doubleHandler.cutDouble(totalProfitAmount / (totalLossAmount * -1)) : 0.0;
    }

    // roa -> 누적손익금 / 최대자본인하금액 * -1
    private Double getRoa(Daily recentDaily, Double accumulatedProfitLossAmount, Double maximumCapitalReductionAmount) {
        if(recentDaily == null || maximumCapitalReductionAmount == 0) return 0.0;

        return doubleHandler.cutDouble(accumulatedProfitLossAmount / maximumCapitalReductionAmount * -1);
    }

    // 운용기간 -> 첫번째 일간분석 데이터의 등록일시, 마지막 일간분석 데이터의 등록일시 차이 - m년 n개월
    private String calculateOperationPeriod(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        int years = period.getYears();
        int months = period.getMonths();

        if(years == 0 && months == 0) return "0년 0개월";

        if(years == 0) return "0년 " + months + "개월";

        return years + "년 " + months + "개월";
    }
}
