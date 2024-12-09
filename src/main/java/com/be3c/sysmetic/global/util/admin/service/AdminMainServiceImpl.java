package com.be3c.sysmetic.global.util.admin.service;

import com.be3c.sysmetic.domain.member.repository.InquiryRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.member.repository.NoticeRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyApprovalRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.global.util.admin.dto.*;
import com.google.analytics.data.v1beta.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdminMainServiceImpl implements AdminMainService {

    @Value("${google.property.id}")
    private String propertyId;

    private final BetaAnalyticsDataClient analyticsDataClient; // 스프링 빈으로 주입됨

    private final MemberRepository memberRepository;

    private final StrategyRepository strategyRepository;

    private final StrategyApprovalRepository strategyApprovalRepository;

    private final InquiryRepository inquiryRepository;

    private final NoticeRepository noticeRepository;

    @Override
    public AdminMainResponseDto getAdminMain() {
        return AdminMainResponseDto.builder()
                .runReportResponseDto(getReport(LocalDate.of(2015, 8, 14), LocalDate.now())) // 전체 기간)
                .strategyCount(getStrategyCount())
                .memberCount(getMemberRatio())
                .adminNoticeResponse(getAdminNoticeResponseDto())
                .adminInquiryResponseDto(getInquiryResponseDto())
                .build();
    }

    @Override
    public RunReportResponseDto getAnalytics(String period) {
        return switch (period) {
            case "day" -> getReport(LocalDate.now().minusDays(1), LocalDate.now());
            case "week" -> getReport(LocalDate.now().minusWeeks(1), LocalDate.now());
            case "month" -> getReport(LocalDate.now().minusMonths(1), LocalDate.now());
            case "year" -> getReport(LocalDate.now().minusYears(1), LocalDate.now());
            case "all" -> getReport(LocalDate.of(2015, 8, 14), LocalDate.now());
            default -> throw new IllegalArgumentException("Invalid period: " + period);
        };
    }

    private RunReportResponseDto getReport(LocalDate startDate, LocalDate endDate) {
        // 요청 생성
        RunReportRequest request = RunReportRequest.newBuilder()
                .setProperty("properties/" + propertyId) // Analytics 속성 ID
                .addDateRanges(DateRange.newBuilder()
                        .setStartDate(startDate.toString())
                        .setEndDate(endDate.toString()))
                .addMetrics(Metric.newBuilder().setName("activeUsers"))
                .addMetrics(Metric.newBuilder().setName("averageSessionDuration"))
                .addDimensions(Dimension.newBuilder().setName("pagePath")) // URL 수집을 위한 Dimension 추가
                .build();

        RunReportResponse runReportResponse = analyticsDataClient.runReport(request);

        // 활성 사용자와 평균 세션 시간 데이터 처리
        Row row = runReportResponse.getRows(0);
        String activeUsersRaw = row.getMetricValues(0).getValue(); // 활성 사용자 수
        String avgSessionDurationRaw = row.getMetricValues(1).getValue(); // 초 단위

        double activeUsers = Double.parseDouble(activeUsersRaw);
        double avgSessionDuration = Double.parseDouble(avgSessionDurationRaw);

        double avgParticipationTimePerUserInSeconds = avgSessionDuration / activeUsers;

        // 초 단위를 분과 초로 변환
        int minutes = (int) avgParticipationTimePerUserInSeconds / 60;
        int seconds = (int) avgParticipationTimePerUserInSeconds % 60;

        // 포맷된 값 생성 (예: "2분 30초")
        String formattedAvgParticipationTime = String.format("%d분 %d초", minutes, seconds);

        // 가장 많이 방문한 URL과 횟수 처리
        Map<String, Integer> urlVisitCounts = new HashMap<>();

        for (Row reportRow : runReportResponse.getRowsList()) {
            String url = reportRow.getDimensionValues(0).getValue(); // URL
            int count = Integer.parseInt(reportRow.getMetricValues(0).getValue()); // 방문 횟수
            urlVisitCounts.put(url, urlVisitCounts.getOrDefault(url, 0) + count);
        }

        // 방문 횟수가 많은 순으로 정렬하고 상위 5개 추출
        Map<String, Integer> topUrls = urlVisitCounts.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 병합 전략 필요 없음
                        LinkedHashMap::new // 순서 유지
                ));

        // DTO 반환
        return RunReportResponseDto.builder()
                .activeUser(activeUsersRaw)
                .avgSessionDuration(formattedAvgParticipationTime) // 사용자당 평균 참여 시간
                .topVisitedUrls(topUrls) // 가장 많이 방문한 URL 맵 추가
                .build();
    }


    private MemberCountResponseDto getMemberRatio() {
        Long userCount = memberRepository.countUser();
        Long traderCount = memberRepository.countTrader();
        Long managerCount = memberRepository.countManager();

        return MemberCountResponseDto.builder()
                .userMemberCount(userCount)
                .TraderMemberCount(traderCount)
                .ManagerMemberCount(managerCount)
                .totalMemberCount(userCount + traderCount)
                .build();
    }

    private AdminInquiryResponseDto getInquiryResponseDto() {
        Long inquiryCount = inquiryRepository.count();
        Long answeredInquiry = inquiryRepository.countAnsweredInquiry();

        return AdminInquiryResponseDto.builder()
                .inquiryCount(inquiryCount)
                .answeredInquiryCount(answeredInquiry)
                .waitingInquiryCount(inquiryCount - answeredInquiry)
                .build();
    }

    private List<AdminNoticeResponseDto> getAdminNoticeResponseDto() {
        Pageable pageable = PageRequest.of(0, 5);

        return noticeRepository.findAdminMainNotice(pageable).getContent();
    }

    private StrategyCountResponseDto getStrategyCount() {
        Long strategyCount = strategyRepository.count();
        Long openStrategyCount = strategyRepository.countOpenStatus();
        Long approvalOpenStrategyCount = strategyApprovalRepository.countWaitingStrategyCount();

        return StrategyCountResponseDto.builder()
                .totalStrategyCount(strategyCount)
                .openStrategyCount(openStrategyCount)
                .waitingStrategyCount(approvalOpenStrategyCount)
                .build();
    }
}