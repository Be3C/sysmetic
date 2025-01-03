package com.be3c.sysmetic.domain.member.entity;

import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.global.common.Code;
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
@Table(name = "inquiry")
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "inquiry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private InquiryAnswer inquiryAnswer;

    // 연관관계 편의 메서드
    public void setInquiryAnswer (InquiryAnswer inquiryAnswer) {
        this.inquiryAnswer = inquiryAnswer;
        inquiryAnswer.setInquiry(this);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "strategy_id", nullable = false)
    private Strategy strategy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquirer_id", nullable = false)
    private Member inquirer;

    @Column(name = "status_code", nullable = false)
    private String statusCode;

    @Column(name = "inquiry_title", length = 100, nullable = false)
    private String inquiryTitle;

    @Column(name = "inquiry_content", length = 1000, nullable = false)
    private String inquiryContent;

    @Column(name = "inquiry_registration_date", nullable = false)
    private LocalDateTime inquiryRegistrationDate;

    //==생성 메서드==//
    public static Inquiry createInquiry(Strategy strategy, Member member, String inquiryTitle, String inquiryContent) {
        Inquiry inquiry = new Inquiry();
        inquiry.setStrategy(strategy);
        inquiry.setInquirer(member);

        inquiry.setStatusCode(Code.UNCLOSED_INQUIRY.getCode());
        inquiry.setInquiryTitle(inquiryTitle);
        inquiry.setInquiryContent(inquiryContent);
        inquiry.setInquiryRegistrationDate(LocalDateTime.now());
        
        return inquiry;
    }

}
