package com.be3c.sysmetic.domain.strategy.dummy;

import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.strategy.entity.Method;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.repository.MethodRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyListRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Component
public class InsertDummyStrategy implements CommandLineRunner {

    @Autowired
    private StrategyListRepository strategyListRepository;

    @Autowired
    private MethodRepository methodRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StrategyRepository strategyRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 더미 데이터 삽입 로직
        insertDummyData();
    }

    public void insertDummyData() {
        // Method 더미 데이터
        Method method = Method.builder()
                .name("Manual")
                .statusCode("MS001")
                .createdBy(1L)
                .modifiedBy(1L)
                .build();
        // method 저장
        methodRepository.save(method);

        // Member trader 더미 데이터
        for (int i = 0; i < 300; i++) {
            Member trader = Member.builder()
                    .roleCode("trader")
                    .email("trader" + i + "@gmail.com")
                    .password("1234")
                    .name("홍길동" + i)
                    .nickname("트레이더" + i)
                    .phoneNumber("0101234" + String.format("%04d", i))
                    .usingStatusCode("US001")
                    .totalFollow(0)
                    .totalStrategyCount(0)
                    .receiveInfoConsent("Yes")
                    .infoConsentDate(LocalDateTime.now())
                    .receiveMarketingConsent("NO")
                    .marketingConsentDate(LocalDateTime.now())
                    .createdBy(1L)
                    .createdDate(LocalDateTime.now())
                    .modifiedBy(1L)
                    .modifiedDate(LocalDateTime.now())
                    .build();
            // trader 저장
            memberRepository.save(trader);
        }

        // Strategy 전략 더미 데이터
        for (int i = 0; i < 200; i++) {
            Strategy s = Strategy.builder()
                    .trader(getTrader("트레이더" + i))
                    .method(getMethod())
                    .statusCode("ST001")
                    .name("전략" + (i + 1))
                    .cycle('P')
                    .content("전략" + (i + 1) + " 소개 내용")
                    .followerCount((long) ((Math.random() * 100) + 1))
                    .accumProfitLossRate(Math.random() * 100)
                    .createdBy((long) i)
                    .modifiedBy((long) i)
                    .build();
            strategyRepository.saveAndFlush(s);
        }

        // 트레이더 닉네임 조회를 위한 전략 더미 데이터
        for (int i = 0; i < 20; i++) {
            Strategy s = Strategy.builder()
                    .trader(getTrader("트레이더119"))
                    .method(getMethod())
                    .statusCode("ST001")
                    .name("전략" + (i + 1) + "버전2")
                    .cycle('P')
                    .content("전략" + (i + 1) + " 소개 내용")
                    .followerCount((long) ((Math.random() * 100) + 1))
                    .accumProfitLossRate(Math.random() * 100)
                    .createdBy(getTrader("트레이더119").getId())
                    .modifiedBy(getTrader("트레이더119").getId())
                    .build();
            strategyRepository.saveAndFlush(s);
        }

        // 비공개인 전략 추가 - 트레이더 닉네임으로 검색 시 아래 전략 개수 세면 안됨
        for (int i = 0; i < 20; i++) {
            Strategy s = Strategy.builder()
                    .trader(getTrader("트레이더1"))
                    .method(getMethod())
                    .statusCode("ST002")
                    .name("비공개 전략" + (i + 1))
                    .cycle('P')
                    .content("전략" + (i + 1) + " 소개 내용")
                    .followerCount((long) ((Math.random() * 100) + 1))
                    .accumProfitLossRate(Math.random() * 100)
                    .createdBy(getTrader("트레이더1").getId())
                    .modifiedBy(getTrader("트레이더1").getId())
                    .build();
            strategyRepository.saveAndFlush(s);
        }

    }

    private Member getTrader(String nickname) {
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("해당 트레이더가 없습니다."));
    }

    private Method getMethod() {
        return methodRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("매매방식이 없습니다."));
    }
}