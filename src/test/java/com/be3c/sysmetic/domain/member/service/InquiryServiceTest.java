package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.*;
import com.be3c.sysmetic.domain.member.repository.InquiryAnswerRepository;
import com.be3c.sysmetic.domain.member.repository.InquiryRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.be3c.sysmetic.domain.strategy.entity.Method;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.global.common.Code;
import com.be3c.sysmetic.global.common.response.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = "/application-test.properties")
@SpringBootTest
@Transactional
public class InquiryServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    InquiryService inquiryService;
    @Autowired
    InquiryRepository inquiryRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private InquiryAnswerRepository inquiryAnswerRepository;

//    @Test
////    @Rollback(value = false)
//    public void dummy_data() throws Exception {
////        Member member1 = createMember("일반닉네임1");
////        Member member2 = createMember("일반닉네임2");
////        Member member3 = createMember("일반닉네임3");
////        Member member4 = createMember("일반닉네임4");
//        Member member1 = createMember("테스트1"); // user
//        Member member2 = createMember("테스트2"); // trader
//        Member member3 = createMember("테스트3"); // user manager
//        Member member4 = createMember("테스트4"); // trader manager
//        Member member5 = createMember("테스트5"); // trader manager
//        Member member6 = createMember("테스트");
//        Member member7 = createMember("테스트");
//        Member member8 = createMember("테스트");
////        Member member9 = createMember("테스트");
////        Member member10 = createMember("테스트");
////        Member member11 = createMember("테스트");
////        Member member12 = createMember("테스트");
////        Member member13 = createMember("테스트");
////        Member member14 = createMember("테스트");
////        Member member15 = createMember("테스트");
//
////        for (int i = 1; i < 124; i++) {
////            createStrategyWithMember("빈 전략", StrategyStatusCode.PUBLIC.getCode(), member2);
////        }
//        Strategy strategy1 = createStrategyWithMember("삼성전자", StrategyStatusCode.PUBLIC.getCode(), member7);
//        Strategy strategy2 = createStrategyWithMember("LG전자", StrategyStatusCode.PUBLIC.getCode(), member6);
//        Strategy strategy3 = createStrategyWithMember("애플", StrategyStatusCode.PUBLIC.getCode(), member8);
//        Strategy strategy4 = createStrategyWithMember("테슬라", StrategyStatusCode.PRIVATE.getCode(), member7);
////
////        for (int i = 1; i < 204; i++) {
////            Inquiry preInquiry = Inquiry.builder()
////                    .strategy(strategy1)
////                    .inquirer(member5)
////                    .traderId(strategy1.getTrader().getId())
////                    .inquiryStatus(InquiryStatus.closed)
////                    .inquiryTitle("문의제목")
////                    .inquiryContent("문의내용")
////                    .inquiryRegistrationDate(LocalDateTime.now())
////                    .build();
////            inquiryRepository.save(preInquiry);
////            if (i < 102) {
////                InquiryAnswer inquiryAnswer = InquiryAnswer.builder()
////                        .inquiry(preInquiry)
////                        .answerTitle("답변제목")
////                        .answerContent("답변내용")
////                        .answerRegistrationDate(LocalDateTime.now())
////                        .build();
////                inquiryAnswerRepository.save(inquiryAnswer);
////            }
////        }
//
//        int countInquiry = 1;
////        int countInquiryAnswer = 1;
//        Strategy strategy = null;
//        Member member = null;
//        InquiryStatus inquiryStatus = null;
//        for(int i = 1; i <= 4; i++) {
//            if (i == 1) { strategy = strategy1; }
//            else if (i == 2) { strategy = strategy2; }
//            else if (i == 3) { strategy = strategy3; }
//            else if (i == 4) { strategy = strategy4; }
//            for(int j = 1; j <= 5; j++) {
//                if (j == 1) { member = member1; }
//                else if (j == 2) { member = member2; }
//                else if (j == 3) { member = member3; }
//                else if (j == 4) { member = member4; }
//                else if (j == 5) { member = member5; }
//                for(int k = 1; k <= 1; k++) {
//                    if (k == 1) { inquiryStatus = InquiryStatus.unclosed; }
////                    if (k == 2) { inquiryStatus = InquiryStatus.closed; }
//                    for(int l = 1; l <= 1; l++) {
//                        Inquiry inquiry = Inquiry.builder()
//                                .strategy(strategy)
//                                .inquirer(member)
//                                .trader(strategy.getTrader())
//                                .inquiryStatus(inquiryStatus)
//                                .inquiryTitle("문의제목" + countInquiry)
//                                .inquiryContent("문의내용" + countInquiry)
//                                .inquiryRegistrationDate(LocalDateTime.now())
//                                .build();
//                        inquiryRepository.save(inquiry);
//                        if (inquiryStatus == InquiryStatus.closed) {
//                            InquiryAnswer inquiryAnswer = InquiryAnswer.builder()
//                                    .inquiry(inquiry)
//                                    .answerTitle("답변제목" + countInquiry)
//                                    .answerContent("답변내용" + countInquiry)
//                                    .answerRegistrationDate(LocalDateTime.now())
//                                    .build();
//                            inquiryAnswerRepository.save(inquiryAnswer);
////                            countInquiryAnswer++;
//                        }
//                        countInquiry++;
//                    }
//                }
//            }
//        }
//    }


//    @Test
//    public void 문의_등록() throws Exception {
//        //given
//        Strategy strategy = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//        InquirySaveRequestDto inquirySaveRequestDto = InquirySaveRequestDto.builder()
//                .inquiryTitle("문의제목1")
//                .inquiryContent("문의내용1")
//                .build();
//
//        //when
//        inquiryService.registerInquiry(strategy.getId(), inquirySaveRequestDto);
//        Inquiry inquiry = inquiryRepository.findByInquiryTitle("문의제목1").get(0);
//
//        //then
//        assertEquals("문의내용1", inquiry.getInquiryContent());
//    }
//
//    @Test
//    public void 문의_수정() throws Exception {
//        //given
//        Strategy strategy = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//        InquirySaveRequestDto inquirySaveRequestDto = InquirySaveRequestDto.builder()
//                .inquiryTitle("문의제목수정1")
//                .inquiryContent("문의내용수정1")
//                .build();
//
//        inquiryService.registerInquiry(strategy.getId(), inquirySaveRequestDto);
//
//        //when
//        Inquiry inquiry = inquiryRepository.findByInquiryTitle("문의제목수정1").get(0);
//
//        InquiryModifyRequestDto inquirySaveRequestDto2 = InquiryModifyRequestDto.builder()
//                .inquiryTitle("수정문의제목1")
//                .inquiryContent("수정문의내용1")
//                .build();
//        inquiryService.modifyInquiry(inquiry.getId(), inquirySaveRequestDto2);
//
//        //then
//        assertEquals("수정문의제목1", inquiry.getInquiryTitle());
//        assertEquals("수정문의내용1", inquiry.getInquiryContent());
//    }
//
//    @Test
//    public void 문의_삭제() throws Exception {
//        //given
//        Strategy strategy = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//
//        InquirySaveRequestDto inquirySaveRequestDto = InquirySaveRequestDto.builder()
//                .inquiryTitle("문의제목삭제1")
//                .inquiryContent("문의내용삭제1")
//                .build();
//        inquiryService.registerInquiry(strategy.getId(), inquirySaveRequestDto);
//
//        //when
//        Inquiry inquiry = inquiryRepository.findByInquiryTitle("문의제목삭제1").get(0);
//        inquiryService.deleteInquiry(inquiry.getId());
//
//        //then
//        assertTrue(inquiryRepository.findById(inquiry.getId()).isEmpty());
//
//    }
//
//    @Test
//    public void 문의_목록_삭제() throws Exception {
//        //given
//        Strategy strategy1 = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//        Strategy strategy2 = createStrategy("LG전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임2");
//
//        InquirySaveRequestDto inquirySaveRequestDto1 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문1")
//                .inquiryContent("내용1")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto1);
//        InquirySaveRequestDto inquirySaveRequestDto2 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문2")
//                .inquiryContent("내용2")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto2);
//        InquirySaveRequestDto inquirySaveRequestDto3 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문3")
//                .inquiryContent("내용3")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto3);
//        InquirySaveRequestDto inquirySaveRequestDto4 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문4")
//                .inquiryContent("내용4")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto4);
//        InquirySaveRequestDto inquirySaveRequestDto5 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문5")
//                .inquiryContent("내용5")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto5);
//        InquirySaveRequestDto inquirySaveRequestDto6 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문6")
//                .inquiryContent("내용6")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto6);
//
//        //when
//        Inquiry inquiry1 = inquiryRepository.findByInquiryTitle("질문1").get(0);
//        Inquiry inquiry2 = inquiryRepository.findByInquiryTitle("질문2").get(0);
//        Inquiry inquiry3 = inquiryRepository.findByInquiryTitle("질문3").get(0);
//        Inquiry inquiry4 = inquiryRepository.findByInquiryTitle("질문4").get(0);
//        Inquiry inquiry5 = inquiryRepository.findByInquiryTitle("질문5").get(0);
//        Inquiry inquiry6 = inquiryRepository.findByInquiryTitle("질문6").get(0);
//        List<Long> idList = new ArrayList<>();
//        idList.add(inquiry1.getId());
//        idList.add(inquiry2.getId());
//        idList.add(inquiry3.getId());
//        idList.add(inquiry4.getId());
//        InquiryAdminListDeleteRequestDto requestDto = InquiryAdminListDeleteRequestDto.builder()
//                .inquiryIdList(idList)
//                .build();
//        inquiryService.deleteAdminInquiryList(requestDto);
//
//        //then
//        assertTrue(inquiryRepository.findById(inquiry1.getId()).isEmpty());
//        assertTrue(inquiryRepository.findById(inquiry2.getId()).isEmpty());
//        assertTrue(inquiryRepository.findById(inquiry3.getId()).isEmpty());
//        assertTrue(inquiryRepository.findById(inquiry4.getId()).isEmpty());
//        assertTrue(inquiryRepository.findById(inquiry5.getId()).isPresent());
//        assertTrue(inquiryRepository.findById(inquiry6.getId()).isPresent());
//
//    }
//
//    @Test
//    public void 관리자_문의_검색() throws Exception {
//        //given
//        Strategy strategy1 = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//        Strategy strategy2 = createStrategy("LG전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임2");
//
//        InquiryAdminListShowRequestDto inquirySearch1 = new InquiryAdminListShowRequestDto();
//        inquirySearch1.setClosed(InquiryClosed.UNCLOSED);
//        inquirySearch1.setSearchType(InquirySearchType.STRATEGY);
//        inquirySearch1.setSearchText("삼성전");
//
//        InquiryAdminListShowRequestDto inquirySearch2 = new InquiryAdminListShowRequestDto();
//        inquirySearch2.setClosed(InquiryClosed.CLOSED);
//        inquirySearch2.setSearchType(InquirySearchType.STRATEGY);
//        inquirySearch2.setSearchText("");
//
//        Member member = createMember("일반닉네임1");
//
//        InquirySaveRequestDto inquirySaveRequestDto1 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문1")
//                .inquiryContent("내용1")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto1);
//        InquirySaveRequestDto inquirySaveRequestDto2 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문2")
//                .inquiryContent("내용2")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto2);
//        InquirySaveRequestDto inquirySaveRequestDto3 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문3")
//                .inquiryContent("내용3")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto3);
//        InquirySaveRequestDto inquirySaveRequestDto4 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문4")
//                .inquiryContent("내용4")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto4);
//        Inquiry inquiry1 = inquiryRepository.findByInquiryTitle("질문4").get(0);
//        inquiry1.setStatusCode(Code.CLOSED_INQUIRY.getCode());
//        InquirySaveRequestDto inquirySaveRequestDto5 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문5")
//                .inquiryContent("내용5")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto5);
//        Inquiry inquiry2 = inquiryRepository.findByInquiryTitle("질문5").get(0);
//        inquiry2.setStatusCode(Code.CLOSED_INQUIRY.getCode());
//
//        //when
//        int page = 0;
//        PageResponse<InquiryAdminListOneShowResponseDto> inquiryList1 = inquiryService.findInquiriesAdmin(inquirySearch1, page);
//        PageResponse<InquiryAdminListOneShowResponseDto> inquiryList2 = inquiryService.findInquiriesAdmin(inquirySearch2, page);
//
//        //then
//        assertEquals(3, inquiryList1.getContent().size());
//        assertEquals(inquiryRepository.findByStatusCode(Code.CLOSED_INQUIRY.getCode(), PageRequest.of(0, 100)).getTotalElements(), inquiryList2.getContent().size());
//    }
//
//    @Test
//    public void 문의_검색() throws Exception {
//        //given
//        Strategy strategy1 = createStrategy("삼성전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임1");
//        Strategy strategy2 = createStrategy("LG전자", StrategyStatusCode.PUBLIC.getCode(), "트레이더닉네임2");
//
//        InquirySaveRequestDto inquirySaveRequestDto1 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문1")
//                .inquiryContent("내용1")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto1);
//        InquirySaveRequestDto inquirySaveRequestDto2 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문2")
//                .inquiryContent("내용2")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto2);
//        InquirySaveRequestDto inquirySaveRequestDto3 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문3")
//                .inquiryContent("내용3")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto3);
//        InquirySaveRequestDto inquirySaveRequestDto4 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문4")
//                .inquiryContent("내용4")
//                .build();
//        inquiryService.registerInquiry(strategy2.getId(), inquirySaveRequestDto4);
//        InquirySaveRequestDto inquirySaveRequestDto5 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문5")
//                .inquiryContent("내용5")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto5);
//        InquirySaveRequestDto inquirySaveRequestDto6 = InquirySaveRequestDto.builder()
//                .inquiryTitle("질문6")
//                .inquiryContent("내용6")
//                .build();
//        inquiryService.registerInquiry(strategy1.getId(), inquirySaveRequestDto6);
//
//        //when
//        int page = 0;
//        PageResponse<InquiryListOneShowResponseDto> inquiryList1 = inquiryService.showTraderInquiry(page, InquirySort.REGISTRATION_DATE, InquiryClosed.ALL);
//        PageResponse<InquiryListOneShowResponseDto> inquiryList2 = inquiryService.showTraderInquiry(page, InquirySort.STRATEGY_NAME, InquiryClosed.ALL);
//        PageResponse<InquiryListOneShowResponseDto> inquiryList3 = inquiryService.showTraderInquiry(page, InquirySort.STRATEGY_NAME, InquiryClosed.ALL);
//
//        // then
//        assertEquals("질문6", inquiryList1.getContent().get(0).getInquiryTitle());
//        assertEquals("질문4", inquiryList2.getContent().get(0).getInquiryTitle());
//        assertEquals("질문3", inquiryList3.getContent().get(0).getInquiryTitle());
//    }


    private Member createMember(String nickName) {
        Member member = new Member();
        member.setRoleCode("USER");
        member.setEmail("user@gmail.com");
        member.setPassword("123456");
        member.setName("송중기");
        member.setNickname(nickName);
        member.setBirth(LocalDateTime.now().toLocalDate());
        member.setPhoneNumber("01012345678");
        member.setUsingStatusCode("UR001");
        member.setTotalFollow(39);
        member.setTotalStrategyCount(100);
        member.setReceiveInfoConsent("true");
        member.setInfoConsentDate(LocalDateTime.now());
        member.setReceiveMarketingConsent("true");
        member.setMarketingConsentDate(LocalDateTime.now());
        em.persist(member);
        return member;
    }

    private Method createMethod() {
        Method method = new Method();
        method.setName("DAY");
        method.setStatusCode("PUBLIC");
        method.setMethodCreatedDate(LocalDateTime.now());
        em.persist(method);
        return method;
    }

    private Strategy createStrategy(String name, String statusCode, String traderNickname) {
        Strategy strategy = new Strategy();
        strategy.setTrader(createMember(traderNickname));
        strategy.setMethod(createMethod());
        strategy.setStatusCode(statusCode);
        strategy.setName(name);
        strategy.setCycle('P');
        strategy.setContent("전략내용");
        strategy.setFollowerCount(36L);
        strategy.setMdd(1.1);
        strategy.setKpRatio(2.2);
        strategy.setSmScore(3.3);
        strategy.setWinningRate(4.4);
        strategy.setAccumulatedProfitLossRate(5.5);
        strategy.setStrategyCreatedDate(LocalDateTime.now());
        strategy.setStrategyModifiedDate(LocalDateTime.now());
        em.persist(strategy);
        return strategy;
    }

    private Strategy createStrategyWithMember(String name, String statusCode, Member trader) {
        Strategy strategy = new Strategy();
        strategy.setTrader(trader);
        strategy.setMethod(createMethod());
        strategy.setStatusCode(statusCode);
        strategy.setName(name);
        strategy.setCycle('P');
        strategy.setContent("전략내용");
        strategy.setFollowerCount(36L);
        strategy.setMdd(1.1);
        strategy.setKpRatio(2.2);
        strategy.setSmScore(3.3);
        strategy.setWinningRate(4.4);
        strategy.setAccumulatedProfitLossRate(5.5);
        strategy.setStrategyCreatedDate(LocalDateTime.now());
        strategy.setStrategyModifiedDate(LocalDateTime.now());
        em.persist(strategy);
        return strategy;
    }
}