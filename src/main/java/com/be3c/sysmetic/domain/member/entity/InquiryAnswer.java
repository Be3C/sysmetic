package com.be3c.sysmetic.domain.member.entity;

import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "inquiry_answer")
public class InquiryAnswer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", unique = true)
    private Inquiry inquiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trader_id", nullable = false)
    private Member trader;

    @Column(name = "answer_title", length = 100, nullable = false)
    private String answerTitle;

    @Column(name = "answer_content", length = 1000, nullable = false)
    private String answerContent;

    @Column(name = "answer_registration_date", nullable = false)
    private LocalDateTime answerRegistrationDate;

    //==생성 메서드==//
    public static InquiryAnswer createInquiryAnswer(Inquiry inquiry, Member trader, String answerTitle, String answerContent) {
        InquiryAnswer inquiryAnswer = new InquiryAnswer();
        inquiryAnswer.setInquiry(inquiry);
        inquiryAnswer.setTrader(trader);
        inquiryAnswer.setAnswerTitle(answerTitle);
        inquiryAnswer.setAnswerContent(answerContent);
        inquiryAnswer.setAnswerRegistrationDate(LocalDateTime.now());
        inquiry.setInquiryAnswer(inquiryAnswer);

        return inquiryAnswer;
    }
}
