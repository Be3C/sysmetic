package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.repository.FolderRepository;
import com.be3c.sysmetic.domain.member.repository.InterestStrategyRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.member.service.FolderService;
import com.be3c.sysmetic.domain.strategy.dto.ReplyDeleteRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.ReplyPostRequestDto;
import com.be3c.sysmetic.domain.strategy.entity.Method;
import com.be3c.sysmetic.domain.strategy.entity.Reply;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.entity.StrategyStatistics;
import com.be3c.sysmetic.domain.strategy.repository.*;
import com.be3c.sysmetic.global.common.Code;
import com.be3c.sysmetic.global.config.security.CustomUserDetails;
import com.be3c.sysmetic.global.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.be3c.sysmetic.global.common.Code.USING_STATE;
import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = "/application-test.properties")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReplyServiceTest {

    private final ReplyService replyService;

    private final ReplyRepository replyRepository;

    private final StrategyApprovalRepository strategyApprovalRepository;

    private final MemberRepository memberRepository;

    private final FolderRepository folderRepository;

    private final InterestStrategyRepository interestStrategyRepository;

    private final MethodRepository methodRepository;

    private final StrategyRepository strategyRepository;

    private final DailyRepository dailyRepository;

    private final EntityManager entityManager;

    private final StrategyStockReferenceRepository strategyStockReferenceRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    private final StrategyStatisticsRepository strategyStatisticsRepository;
    @Autowired
    private SecurityUtils securityUtils;

    @BeforeEach
    public void setUp() {
        replyRepository.deleteAll();
        strategyStockReferenceRepository.deleteAll();
        interestStrategyRepository.deleteAll();
        folderRepository.deleteAll();
        strategyRepository.deleteAll();
        methodRepository.deleteAll();
        dailyRepository.deleteAll();
        strategyApprovalRepository.deleteAll();
        memberRepository.deleteAll();

        entityManager.createNativeQuery("ALTER TABLE member AUTO_INCREMENT = 1")
                .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE folder AUTO_INCREMENT = 1")
                .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE strategy AUTO_INCREMENT = 1")
                .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE strategy_statistics AUTO_INCREMENT = 1")
                .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE method AUTO_INCREMENT = 1")
                .executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reply AUTO_INCREMENT = 1")
                .executeUpdate();

        entityManager.flush();
        entityManager.clear();

        Member member = Member.builder()
                .email("test@test.com")
                .password(bCryptPasswordEncoder.encode("encodedPassword"))
                // 초기값 설정
                .roleCode("TRADER")
                .name("테스트")
                .nickname("테스트")
                .birth(LocalDate.of(2000,1,1))
                .phoneNumber("01012341234")
                .usingStatusCode("US001")
                .totalFollow(0)
                .totalStrategyCount(0)
                .receiveInfoConsent("Y")
                .infoConsentDate(LocalDateTime.now())
                .receiveMarketingConsent("Y")
                .marketingConsentDate(LocalDateTime.now())
                .build();

        memberRepository.save(member);

        Method method = Method.builder()
                .name("테스트매매유형")
                .statusCode(USING_STATE.getCode())
                .build();

        methodRepository.save(method);

        List<Strategy> strategyList = new ArrayList<>();

        for(int i = 1; i <= 20; i++) {
            strategyList.add(Strategy.builder()
                    .name("테스트전략" + i)
                    .trader(member)
                    .content("설명" + i)
                    .method(method)
                    .statusCode(Code.OPEN_STRATEGY.getCode())
                    .cycle('D')
                    .build());
        }

        strategyRepository.saveAll(strategyList);

        StrategyStatistics strategyStatistics = StrategyStatistics.builder()
                .strategy(strategyList.get(1))
                .currentBalance(1000000.0)
                .principal(500000.0)
                .accumulatedDepositWithdrawalAmount(200000.0)
                .accumulatedProfitLossAmount(150000.0)
                .accumulatedProfitLossRate(30.0)
                .maximumAccumulatedProfitLossAmount(200000.0)
                .maximumAccumulatedProfitLossRate(40.0)
                .currentCapitalReductionAmount(50000.0)
                .currentCapitalReductionRate(5.0)
                .maximumCapitalReductionAmount(100000.0)
                .maximumCapitalReductionRate(10.0)
                .averageProfitLossAmount(2000.0)
                .averageProfitLossRate(1.0)
                .maximumDailyProfitAmount(5000.0)
                .maximumDailyProfitRate(2.0)
                .maximumDailyLossAmount(-3000.0)
                .maximumDailyLossRate(-1.5)
                .totalTradingDays(100L)
                .currentContinuousProfitLossDays(5L)
                .totalProfitDays(60L)
                .maximumContinuousProfitDays(10L)
                .totalLossDays(40L)
                .maximumContinuousLossDays(8L)
                .winningRate(60.0)
                .highPointRenewalProgress(30L)
                .profitFactor(1.5)
                .roa(0.08)
                .firstRegistrationDate(LocalDate.now())
                .lastRegistrationDate(LocalDate.now())
                .build();

        strategyStatisticsRepository.save(strategyStatistics);

        // 권한 설정
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // CustomUserDetails 생성
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, // memberId
                "test@example.com", // email
                "TRADER", // role
                authorities // 권한 목록
        );

        // Authentication 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // SecurityContext에 Authentication 객체 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("댓글 페이징 - 성공")
    @Order(1)
    public void testReplyPaging() {
        Strategy strategy = strategyRepository.findAll().get(0);

        for(int i = 1; i <= 20; i++) {
            replyService.insertReply(ReplyPostRequestDto.builder()
                    .strategyId(strategy.getId())
                    .content("테스트" + i)
                    .build());
        }

        replyService.getReplyPage(strategy.getId(), 1)
                .getContent()
                .forEach(
                        dto -> log.info("reply : {}", dto.toString())
                );
    }

    @Test
    @DisplayName("댓글 페이징 - 실패 : 최대 페이지 요청")
    @Order(2)
    public void testReplyPagingFailEmpty() {
        Strategy strategy = strategyRepository.findAll().get(0);

        assertThrows(NoSuchElementException.class,() -> replyService.getReplyPage(strategy.getId(), 1));
    }

    @Test
    @DisplayName("댓글 추가 - 성공")
    @Order(3)
    public void testReplyInsertSuccess() {
        Strategy strategy = strategyRepository.findAll().get(0);

        long count = replyRepository.
                findByStrategyIdAndStatusCode(
                        strategy.getId(),
                        USING_STATE.getCode()).size();

        replyService.insertReply(ReplyPostRequestDto.builder()
                        .strategyId(strategy.getId())
                        .content("반갑다 친구야")
                .build());

        assertEquals(
                count + 1,
                replyRepository.
                        findByStrategyIdAndStatusCode(
                                strategy.getId(),
                                USING_STATE.getCode()).size()
        );
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    @Order(4)
    public void testReplyDeleteSuccess() {
        Strategy strategy = strategyRepository.findAll().get(0);

        long count = replyRepository.
                        findByStrategyIdAndStatusCode(
                                strategy.getId(),
                                USING_STATE.getCode()).size();

        replyService.insertReply(ReplyPostRequestDto.builder()
                .strategyId(strategy.getId())
                .content("반갑다 친구야")
                .build());

        assertEquals(
                count + 1,
                replyRepository.
                        findByStrategyIdAndStatusCode(
                                strategy.getId(),
                                USING_STATE.getCode()).size()
        );

        Reply reply = replyRepository.findAll().get(0);

        replyService.deleteReply(ReplyDeleteRequestDto.builder()
                        .strategyId(strategy.getId())
                        .build()
        );

        assertEquals(
                count,
                replyRepository.
                        findByStrategyIdAndStatusCode(
                                strategy.getId(),
                                USING_STATE.getCode()).size()
        );
    }
}
