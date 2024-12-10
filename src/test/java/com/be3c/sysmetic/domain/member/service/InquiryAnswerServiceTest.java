package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.InquiryAnswerSaveRequestDto;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryAnswer;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.repository.InquiryAnswerRepository;
import com.be3c.sysmetic.domain.strategy.entity.Method;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = "/application-test.properties")
@SpringBootTest
@Transactional
public class InquiryAnswerServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    InquiryAnswerService inquiryAnswerService;
    @Autowired
    private InquiryAnswerRepository inquiryAnswerRepository;

//    @Test
//    public void 전체_조회() throws Exception {
//        //given
//        Inquiry inquiry1 = createInquiry("문의제목1", "문의내용1");
//        Inquiry inquiry2 = createInquiry("문의제목2", "문의내용2");
//
//        InquiryAnswerSaveRequestDto requestDto1 = InquiryAnswerSaveRequestDto.builder()
//                .answerTitle("답변제목1")
//                .answerContent("답변내용1")
//                .build();
//        InquiryAnswerSaveRequestDto requestDto2 = InquiryAnswerSaveRequestDto.builder()
//                .answerTitle("답변제목2")
//                .answerContent("답변내용2")
//                .build();
//
//        inquiryAnswerService.registerInquiryAnswer(inquiry1.getId(), requestDto1);
//        inquiryAnswerService.registerInquiryAnswer(inquiry2.getId(), requestDto2);
//
//        //when
//        List<InquiryAnswer> inquiryAnswerList = inquiryAnswerRepository.findAll();
//
//        //then
//        assertEquals(2, inquiryAnswerList.size());
//
//    }
//
//    @Test
//    public void 문의별_조회() throws Exception {
//        //given
//        Inquiry inquiry1 = createInquiry("문의제목1", "문의내용1");
//        Inquiry inquiry2 = createInquiry("문의제목2", "문의내용2");
//
//        InquiryAnswerSaveRequestDto inquiryDetailSaveRequestDto1 = InquiryAnswerSaveRequestDto.builder()
//                .answerTitle("답변제목1")
//                .answerContent("답변내용1")
//                .build();
//
//        InquiryAnswerSaveRequestDto inquiryDetailSaveRequestDto2 = InquiryAnswerSaveRequestDto.builder()
//                .answerTitle("답변제목2")
//                .answerContent("답변내용2")
//                .build();
//
//        inquiryAnswerService.registerInquiryAnswer(inquiry1.getId(), inquiryDetailSaveRequestDto1);
//        inquiryAnswerService.registerInquiryAnswer(inquiry2.getId(), inquiryDetailSaveRequestDto2);
//
//        //when
//        InquiryAnswer inquiryAnswerList1 = inquiryAnswerRepository.findByInquiryId(inquiry1.getId()).get();
//        InquiryAnswer inquiryAnswerList2 = inquiryAnswerRepository.findByInquiryId(inquiry2.getId()).get();
//
//        //then
//        assertEquals("답변내용1", inquiryAnswerList1.getAnswerContent());
//        assertEquals("답변내용2", inquiryAnswerList2.getAnswerContent());
//
//    }
//
//    @Test
//    public void 답변_등록() throws Exception {
//        //given
//        Inquiry inquiry = createInquiry("문의제목1", "문의내용1");
//        InquiryAnswerSaveRequestDto inquiryDetailSaveRequestDto = InquiryAnswerSaveRequestDto.builder()
//                .answerTitle("답변제목1")
//                .answerContent("답변내용1")
//                .build();
//
//        //when
//        inquiryAnswerService.registerInquiryAnswer(inquiry.getId(), inquiryDetailSaveRequestDto);
//        InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByAnswerTitle("답변제목1").get(0);
//
//        //then
//        assertEquals("답변제목1", inquiryAnswer.getAnswerTitle());
//        assertEquals("답변내용1", inquiryAnswer.getAnswerContent());
//
//    }


    private Inquiry createInquiry(String inquiryTitle, String inquiryContent) {
        Inquiry inquiry = Inquiry.createInquiry(createStrategy("삼성전자"), createMember("닉네임"), inquiryTitle, inquiryContent);
        em.persist(inquiry);
        return inquiry;
    }


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

    private Strategy createStrategy(String name) {
        Strategy strategy = new Strategy();
        strategy.setTrader(createMember("닉네임1"));
        strategy.setMethod(createMethod());
        strategy.setStatusCode("PUBLIC");
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