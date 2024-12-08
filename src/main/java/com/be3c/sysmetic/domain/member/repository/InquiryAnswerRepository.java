package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    List<InquiryAnswer> findByAnswerTitle(String inquiryAnswerTitle);

    Optional<InquiryAnswer> findByInquiryId(Long inquiryId);

    @Modifying
    @Query("DELETE FROM InquiryAnswer ia WHERE ia.inquiry.id  = :inquiryId")
    void deleteByinquiryId(@Param("inquiryId") Long inquiryId);

    @Modifying
    @Query("DELETE FROM InquiryAnswer ia WHERE ia.inquiry.trader.id = :MemberId")
    void deleteByMemberId(@Param("MemberId") Long MemberId);

    @Modifying
    @Query("DELETE FROM InquiryAnswer a WHERE a.inquiry.strategy.id = :strategyId")
    void deleteByStrategyId(@Param("strategyId") Long strategyId);
}
