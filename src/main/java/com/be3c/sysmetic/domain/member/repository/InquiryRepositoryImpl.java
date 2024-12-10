package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.InquiryStatus;
import com.be3c.sysmetic.domain.member.entity.QInquiry;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryImpl implements InquiryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final JPAQueryFactory jpaQueryFactory1;
    private final JPAQueryFactory jpaQueryFactory2;
    private final QInquiry inquiry = QInquiry.inquiry;

    public Page<Inquiry> adminInquirySearchWithBooleanBuilder(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        InquiryStatus closed = inquiryAdminListShowRequestDto.getClosed();
        String searchType = inquiryAdminListShowRequestDto.getSearchType();
        String searchText = inquiryAdminListShowRequestDto.getSearchText();

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        // 검색 (전략명, 트레이더, 질문자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("strategy")) {
                predicate.and(inquiry.strategy.name.contains(searchText));
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        QueryResults<Inquiry> results = jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Inquiry> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }


    public Page<Inquiry> registrationDateTraderInquirySearchWithBooleanBuilder(InquiryTraderListShowRequestDto inquiryTraderListShowRequestDto, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long traderId = inquiryTraderListShowRequestDto.getTraderId();
        InquiryStatus closed = inquiryTraderListShowRequestDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        QueryResults<Inquiry> results = jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Inquiry> content = new ArrayList<>();
        long total;

        content = results.getResults();
        total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    public Page<Inquiry> registrationDateInquirerInquirySearchWithBooleanBuilder(InquiryInquirerListShowRequestDto inquiryInquirerListShowRequestDto, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquirerId = inquiryInquirerListShowRequestDto.getInquirerId();
        InquiryStatus closed = inquiryInquirerListShowRequestDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        QueryResults<Inquiry> results = jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Inquiry> content = new ArrayList<>();
        long total;

        content = results.getResults();
        total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    public Page<Inquiry> strategyNameTraderInquirySearchWithBooleanBuilder(InquiryTraderListShowRequestDto inquiryTraderListShowRequestDto, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long traderId = inquiryTraderListShowRequestDto.getTraderId();
        InquiryStatus closed = inquiryTraderListShowRequestDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        QueryResults<Inquiry> results = jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.strategy.name.asc(), inquiry.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Inquiry> content = new ArrayList<>();
        long total;

        content = results.getResults();
        total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    public List<Inquiry> strategyNameInquirerInquirySearchWithBooleanBuilder(InquiryInquirerListShowRequestDto inquiryInquirerListShowRequestDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquirerId = inquiryInquirerListShowRequestDto.getInquirerId();
        InquiryStatus closed = inquiryInquirerListShowRequestDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()));
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        List<Inquiry> content = new ArrayList<>();
        List<Inquiry> results1 = jpaQueryFactory1
                .selectFrom(inquiry)
                .where(predicate1)
                .orderBy(inquiry.strategy.name.asc(), inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();
        List<Inquiry> results2 = jpaQueryFactory2
                .selectFrom(inquiry)
                .where(predicate2)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();

        content.addAll(results1);
        content.addAll(results2);

        return content;
    }

    // 관리자 이전 문의 조회
    public Inquiry adminFindPreviousInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailAdminShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailAdminShowDto.getClosed();
        String searchType = inquiryDetailAdminShowDto.getSearchType();
        String searchText = inquiryDetailAdminShowDto.getSearchText();

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        // 검색 (전략명, 트레이더, 질문자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("strategy")) {
                predicate.and(inquiry.strategy.name.contains(searchText));
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.lt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst();
    }

    // 관리자 다음 문의 조회
    public Inquiry adminFindNextInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailAdminShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailAdminShowDto.getClosed();
        String searchType = inquiryDetailAdminShowDto.getSearchType();
        String searchText = inquiryDetailAdminShowDto.getSearchText();

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        // 검색 (전략명, 트레이더, 질문자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("strategy")) {
                predicate.and(inquiry.strategy.name.contains(searchText));
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.gt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst();
    }

    // 질문자 이전 문의 조회 최신순
    public Inquiry inquirerFindPreviousInquiryRegistrationDate(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailInquirerShowDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.lt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst();
    }

    // 질문자 이전 문의 조회 전략명순
    public Inquiry inquirerFindPreviousInquiryStrategyName(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailInquirerShowDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()));
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        List<Inquiry> content = new ArrayList<>();
        List<Inquiry> results1 = jpaQueryFactory1
                .selectFrom(inquiry)
                .where(predicate1)
                .orderBy(inquiry.strategy.name.asc(), inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();
        List<Inquiry> results2 = jpaQueryFactory2
                .selectFrom(inquiry)
                .where(predicate2)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();

        content.addAll(results1);
        content.addAll(results2);

        if (content.isEmpty()) {
            return null;
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == content.size() - 1) {
                        return null;
                    } else {
                        return content.get(i+1);
                    }
                }
            }

            return null;
        }
    }

    // 질문자 다음 문의 조회 최신순
    public Inquiry inquirerFindNextInquiryRegistrationDate(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailInquirerShowDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.gt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst();
    }

    // 질문자 다음 문의 조회 전략명순
    public Inquiry inquirerFindNextInquiryStrategyName(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailInquirerShowDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()));
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        List<Inquiry> content = new ArrayList<>();
        List<Inquiry> results1 = jpaQueryFactory1
                .selectFrom(inquiry)
                .where(predicate1)
                .orderBy(inquiry.strategy.name.asc(), inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();
        List<Inquiry> results2 = jpaQueryFactory2
                .selectFrom(inquiry)
                .where(predicate2)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetch();

        content.addAll(results1);
        content.addAll(results2);

        if (content.isEmpty()) {
            return null;
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == 0) {
                        return null;
                    } else {
                        return content.get(i-1);
                    }
                }
            }

            return null;
        }
    }

    // 트레이더 이전 문의 조회 최신순
    public Inquiry traderFindPreviousInquiryRegistrationDate(InquiryDetailTraderShowDto inquiryDetailTraderShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.lt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst();
    }

    // 트레이더 이전 문의 조회 전략명순
    public Inquiry traderFindPreviousInquiryStrategyName(InquiryDetailTraderShowDto inquiryDetailTraderShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.lt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.strategy.name.asc(), inquiry.id.desc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }

    // 트레이더 다음 문의 조회 최신순
    public Inquiry traderFindNextInquiryRegistrationDate(InquiryDetailTraderShowDto inquiryDetailTraderShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.gt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst();
    }

    // 트레이더 다음 문의 조회 전략명순
    public Inquiry traderFindNextInquiryStrategyName(InquiryDetailTraderShowDto inquiryDetailTraderShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.strategy.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());

        predicate.and(inquiry.id.gt(inquiryId));

        return jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.strategy.name.desc(), inquiry.id.asc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }
}
