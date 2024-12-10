package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.member.repository.InterestStrategyRepository;
import com.be3c.sysmetic.domain.strategy.dto.*;
import com.be3c.sysmetic.domain.strategy.entity.Daily;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.entity.StrategyGraphAnalysis;
import com.be3c.sysmetic.domain.strategy.entity.StrategyStatistics;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.repository.*;
import com.be3c.sysmetic.domain.strategy.util.DoubleHandler;
import com.be3c.sysmetic.domain.strategy.util.StockGetter;
import com.be3c.sysmetic.domain.strategy.util.StrategyIndicatorsCalculator;
import com.be3c.sysmetic.domain.strategy.util.StrategyViewAuthorize;
import com.be3c.sysmetic.global.common.response.APIResponse;
import com.be3c.sysmetic.global.common.response.ErrorCode;
import com.be3c.sysmetic.global.util.SecurityUtils;
import com.be3c.sysmetic.global.util.file.dto.FileReferenceType;
import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class StrategyDetailServiceImpl implements StrategyDetailService {

    private final InterestStrategyRepository interestStrategyRepository;
    private final StrategyRepository strategyRepository;
    private final StrategyDetailRepository strategyDetailRepository;
    private final StrategyStatisticsRepository strategyStatisticsRepository;
    private final StrategyGraphAnalysisRepository strategyGraphAnalysisRepository;
    private final MonthlyRepository monthlyRepository;
    private final DailyRepository dailyRepository;
    private final StrategyViewAuthorize strategyViewAuthorize;
    private final StockGetter stockGetter;
    private final DoubleHandler doubleHandler;
    private final FileService fileService;
    private final SecurityUtils securityUtils;
    private final StrategyIndicatorsCalculator strategyIndicatorsCalculator;

    @Override
    @Transactional
    public StrategyDetailDto getDetail(Long id) {

        strategyViewAuthorize.Authorize(strategyRepository.findById(id).orElseThrow(NoSuchElementException::new));

        StrategyDetailStatistics statistics = strategyStatisticsRepository.findStrategyDetailStatistics(id);
        StrategyDetailDto detailDto = strategyDetailRepository.findPublicStrategy(id)
                .map(strategy -> StrategyDetailDto.builder()
                        .id(strategy.getId())
                        .traderId(strategy.getTrader().getId())
                        .traderNickname(strategy.getTrader().getNickname())
                        .traderProfileImage(fileService.getFilePathNullable(new FileRequest(FileReferenceType.MEMBER, strategy.getTrader().getId())))
                        .methodId(strategy.getMethod().getId())
                        .methodName(strategy.getMethod().getName())
                        .methodIconPath(fileService.getFilePathNullable(new FileRequest(FileReferenceType.METHOD, strategy.getMethod().getId())))
                        .stockList(stockGetter.getStocks(strategy.getId()))
                        .isFollow(false)
                        .name(strategy.getName())
                        .statusCode(strategy.getStatusCode())
                        .cycle(strategy.getCycle())
                        .content(strategy.getContent())
                        .followerCount(strategy.getFollowerCount())
                        .mdd(strategy.getMdd())
                        .kpRatio(strategy.getKpRatio())
                        .smScore(strategy.getSmScore())
                        .accumulatedProfitLossRate(strategy.getAccumulatedProfitLossRate())
                        .maximumCapitalReductionAmount(statistics.getMaximumCapitalReductionAmount())
                        .averageProfitLossRate(doubleHandler.cutDouble(statistics.getAverageProfitLossRate()))
                        .profitFactor(doubleHandler.cutDouble(statistics.getProfitFactor()))
                        .winningRate(strategy.getWinningRate())
                        .monthlyRecord(getMonthlyRecords(strategy.getId()))
                        .fileWithInfoResponse(
                                fileService.getFileWithInfoNullable(new FileRequest(FileReferenceType.STRATEGY, strategy.getId()))
                        )
                        .build())
                .orElseThrow(() -> new NoSuchElementException("전략 상세 페이지가 존재하지 않습니다."));

        try {
            Long userId = securityUtils.getUserIdInSecurityContext();

            HashSet<Long> interestStrategyList = interestStrategyRepository.findAllByMemberId(userId);

            if(interestStrategyList.contains(detailDto.getId())) {
                detailDto.setIsFollow(true);
            }
        } catch (UsernameNotFoundException | AuthenticationCredentialsNotFoundException e) {
        }

        return detailDto;
    }


    // 분석 지표 그래프 데이터 요청
    @Override
    @Transactional
    public APIResponse<StrategyAnalysisResponseDto> getAnalysis(Long strategyId) {

        StrategyAnalysisResponseDto analysis = strategyRepository.findGraphAnalysis(strategyId);

        strategyViewAuthorize.Authorize(strategyRepository.findById(strategyId).orElseThrow(EntityNotFoundException::new));

        if (analysis == null || analysis.getXAxis().isEmpty()) return APIResponse.fail(ErrorCode.NOT_FOUND, "분석 그래프가 존재하지 않습니다.");

        return APIResponse.success(analysis);
    }


    // saveAnalysis - 분석 지표 그래프 데이터 생성 (일간 분석 등록하면, 그래프 데이터도 생성)
    @Override
    @Transactional
    public APIResponse<String> saveAnalysis(Long strategyId, LocalDate date) {
        Daily daily = dailyRepository.findByStrategyIdAndDate(strategyId, date);
        StrategyStatistics statistics = strategyStatisticsRepository.findByStrategyId(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));
        Double accumulatedProfitLossAmount = daily.getAccumulatedProfitLossAmount();
        Double standardAmount = daily.getStandardAmount();
        Double maximumCapitalReductionAmount = statistics.getMaximumCapitalReductionAmount();

        StrategyGraphAnalysis data = StrategyGraphAnalysis.builder()
                .strategy(strategyRepository.findById(strategyId).orElseThrow())
                .daily(daily)
                .date(daily.getDate())
                .standardAmount(daily.getStandardAmount())
                .currentBalance(daily.getCurrentBalance())
                .principal(daily.getPrincipal())
                .accumulatedDepositWithdrawalAmount(dailyRepository.findTotalDepositWithdrawalAmountByStrategyId(strategyId))
                .depositWithdrawalAmount(daily.getDepositWithdrawalAmount())
                .profitLossAmount(daily.getProfitLossAmount())
                .profitLossRate(daily.getProfitLossRate())
                .accumulatedProfitLossAmount(daily.getAccumulatedProfitLossAmount())
                .currentCapitalReductionAmount(strategyIndicatorsCalculator.calCurrentCapitalReductionAmount(strategyId, accumulatedProfitLossAmount))
                .currentCapitalReductionRate(strategyIndicatorsCalculator.calCurrentCapitalReductionRate(strategyId, standardAmount))
                .averageProfitLossAmount(strategyIndicatorsCalculator.calAverageProfitLossAmount(strategyId, accumulatedProfitLossAmount))
                .averageProfitLossRate(strategyIndicatorsCalculator.calAverageProfitLossRate(strategyId, accumulatedProfitLossAmount))
                .winningRate(strategyIndicatorsCalculator.calWinningRate(strategyId))
                .profitFactor(strategyIndicatorsCalculator.calProfitFactor(strategyId))
                .roa(strategyIndicatorsCalculator.calRoa(accumulatedProfitLossAmount, maximumCapitalReductionAmount))
                .maximumCapitalReductionAmount(maximumCapitalReductionAmount)
                .build();

        strategyGraphAnalysisRepository.saveAndFlush(data);
        strategyStatisticsRepository.updateIndicators(
                data.getStrategy().getId(),
                data.getCurrentCapitalReductionAmount(),
                data.getCurrentCapitalReductionRate(),
                data.getAverageProfitLossAmount(),
                data.getAverageProfitLossRate(),
                data.getWinningRate(),
                data.getProfitFactor(),
                data.getRoa(),
                data.getMaximumCapitalReductionAmount()
        );
        return APIResponse.success("분석 지표 그래프 데이터 생성 성공");
    }


    // updateAnalysis - 분석 지표 그래프 데이터 수정(일간 분석 데이터 수정하면, 그래프 데이터도 수정)
    @Override
    @Transactional
    public APIResponse<String> updateAnalysis(Long strategyId, Long dailyId, LocalDate date) {
        Daily newDaily = dailyRepository.findByStrategyIdAndDate(strategyId, date);
        StrategyStatistics statistics = strategyStatisticsRepository.findByStrategyId(strategyId).orElseThrow(() ->
                new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage(), ErrorCode.NOT_FOUND));
        Double accumulatedProfitLossAmount = statistics.getAccumulatedProfitLossAmount();
        Double standardAmount = newDaily.getStandardAmount();
        Double maximumCapitalReductionAmount = statistics.getMaximumCapitalReductionAmount();

        StrategyGraphAnalysis strategyGraphAnalysis = strategyGraphAnalysisRepository.findByDailyId(dailyId);

        StrategyGraphAnalysis data = StrategyGraphAnalysis.builder()
                .id(strategyGraphAnalysis.getId())
                .strategy(strategyRepository.findById(strategyId).orElseThrow())
                .daily(newDaily)
                .date(newDaily.getDate())
                .standardAmount(newDaily.getStandardAmount())
                .currentBalance(newDaily.getCurrentBalance())
                .principal(newDaily.getPrincipal())
                .accumulatedDepositWithdrawalAmount(dailyRepository.findTotalDepositWithdrawalAmountByStrategyId(strategyId))
                .depositWithdrawalAmount(newDaily.getDepositWithdrawalAmount())
                .profitLossAmount(newDaily.getProfitLossAmount())
                .profitLossRate(newDaily.getProfitLossRate())
                .accumulatedProfitLossAmount(newDaily.getAccumulatedProfitLossAmount())
                .currentCapitalReductionAmount(strategyIndicatorsCalculator.calCurrentCapitalReductionAmount(strategyId, accumulatedProfitLossAmount))
                .currentCapitalReductionRate(strategyIndicatorsCalculator.calCurrentCapitalReductionRate(strategyId, standardAmount))
                .averageProfitLossAmount(strategyIndicatorsCalculator.calAverageProfitLossAmount(strategyId, accumulatedProfitLossAmount))
                .averageProfitLossRate(strategyIndicatorsCalculator.calAverageProfitLossRate(strategyId, accumulatedProfitLossAmount))
                .winningRate(strategyIndicatorsCalculator.calWinningRate(strategyId))
                .profitFactor(strategyIndicatorsCalculator.calProfitFactor(strategyId))
                .roa(strategyIndicatorsCalculator.calRoa(accumulatedProfitLossAmount, maximumCapitalReductionAmount))
                .maximumCapitalReductionAmount(maximumCapitalReductionAmount)

                .build();

        strategyGraphAnalysisRepository.save(data);
        strategyStatisticsRepository.updateIndicators(
                data.getStrategy().getId(),
                data.getCurrentCapitalReductionAmount(),
                data.getCurrentCapitalReductionRate(),
                data.getAverageProfitLossAmount(),
                data.getAverageProfitLossRate(),
                data.getWinningRate(),
                data.getProfitFactor(),
                data.getRoa(),
                data.getMaximumCapitalReductionAmount()
        );
        return APIResponse.success("분석 지표 그래프 데이터 생성 성공");
    }


    // deleteAnalysis - 일간 분석 삭제하면, 해당 날짜 그래프 데이터 삭제
    @Override
    @Transactional
    public APIResponse<String> deleteAnalysis(Long strategyId, Long dailyId, Daily deletedDaily) {

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new IllegalArgumentException("Strategy not fount with ID : " + strategyId));

        // DB에서 삭제
        strategyGraphAnalysisRepository.deleteByDailyId(dailyId);

        // 빼야 하는 값
        Double profitLossAmountToMinus = deletedDaily.getProfitLossAmount();
        Double profitLossRateToMinus = deletedDaily.getProfitLossRate();
        Double depositWithdrawalAmountToMinus = deletedDaily.getDepositWithdrawalAmount();

        List<StrategyGraphAnalysis> toBeModified = strategyGraphAnalysisRepository.findByIdAndAfterDate(strategyId, deletedDaily.getDate());

        toBeModified.forEach(analysis -> {
            analysis.setAccumulatedDepositWithdrawalAmount(analysis.getAccumulatedDepositWithdrawalAmount() - depositWithdrawalAmountToMinus);
            analysis.setAverageProfitLossAmount(analysis.getAverageProfitLossAmount() - profitLossAmountToMinus);
            analysis.setAverageProfitLossRate(analysis.getAverageProfitLossRate() - profitLossRateToMinus);
            analysis.setWinningRate(calculateUpdatedWinningRate(strategyId, analysis.getDate()));
            analysis.setProfitFactor(calculateUpdatedProfitFactor(strategyId, analysis.getDate()));
            analysis.setRoa(calculateUpdatedRoa(strategyId, analysis.getDate(), analysis.getAccumulatedProfitLossAmount()));
            analysis.setMaximumCapitalReductionAmount(strategyGraphAnalysisRepository.findTop1MaximumCapitalReductionAmountByStrategyId(strategyId));
        });

        strategyGraphAnalysisRepository.saveAll(toBeModified);

        return APIResponse.success("그래프 데이터 삭제 성공");
    }

    // 그 시점의 winningRate 계산
    private Double calculateUpdatedWinningRate(Long strategyId, LocalDate date) {
        Long tradingDays = strategyGraphAnalysisRepository.countAllByStrategyIdBeforeDate(strategyId, date);
        if (tradingDays == 0) return 0.0;

        Long profitDays = strategyGraphAnalysisRepository.countProfitDaysByStrategyIdBeforeDate(strategyId, date);

        return doubleHandler.cutDouble(tradingDays / (double) profitDays);
    }

    // 그 시점의 Profit Factor 계산
    private Double calculateUpdatedProfitFactor(Long strategyId, LocalDate date) {
        Object[] profitLossAmounts = strategyGraphAnalysisRepository.findProfitLossAmount(strategyId, date);
        if (profitLossAmounts == null) return 0.0;
        Double profit = (Double) profitLossAmounts[0];
        Double loss = (Double) profitLossAmounts[1];
        if (loss == 0) return 0.0;
        return doubleHandler.cutDouble(profit / loss);
    }

    // 그 시점의 ROA 계산
    private Double calculateUpdatedRoa(Long strategyId, LocalDate date, Double accumulatedProfitLossAmount) {
        Double maximumCapitalReductionAmount = strategyGraphAnalysisRepository.findMaximumCapitalReductionAmountBeforeDate(strategyId, date);

        if (maximumCapitalReductionAmount == 0) return 0.0;

        return doubleHandler.cutDouble(accumulatedProfitLossAmount / maximumCapitalReductionAmount * -1);
    }

    // getMonthlyRecords : 월간 분석 데이터 매핑
    private List<YearlyRecord> getMonthlyRecords(Long strategyId) {
        // 데이터 조회
        List<MonthlyForRepo> data = monthlyRepository.findAllMonthlyRecord(strategyId);

        // 데이터가 없을 경우 빈 리스트 반환
        if (data == null || data.isEmpty()) return new ArrayList<>();

        Map<Integer, List<MonthlyRecord>> groupedByYear = new LinkedHashMap<>();

        int currentYear = -1;
        double ytdAccumulation = 0.0;

        for (MonthlyForRepo monthly : data) {
            int year = monthly.getYear();
            double profitLossRate = monthly.getAccumulatedProfitLossRate();

            // 연도가 변경되면 YTD 값 추가
            if (year != currentYear) {
                if (currentYear != -1) {
                    groupedByYear.get(currentYear).add(
                            MonthlyRecord.builder()
                                    .month(13)
                                    .value(doubleHandler.cutDouble(ytdAccumulation))
                                    .build());
                }
                currentYear = year;
                ytdAccumulation = 0.0;
                groupedByYear.putIfAbsent(year, new ArrayList<>());
            }

            ytdAccumulation += profitLossRate;
            groupedByYear.get(year).add(
                    MonthlyRecord.builder()
                            .month(monthly.getMonth())
                            .value(profitLossRate)
                            .build());
        }

        // 마지막 연도의 YTD 데이터 추가
        if (currentYear != -1) {
            groupedByYear.get(currentYear).add(
                    MonthlyRecord.builder()
                            .month(13)
                            .value(doubleHandler.cutDouble(ytdAccumulation))
                            .build());
        }

        // Map을 YearlyRecord 리스트로 변환
        return groupedByYear.entrySet().stream()
                .map(entry -> YearlyRecord.builder()
                        .year(entry.getKey())
                        .data(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
}