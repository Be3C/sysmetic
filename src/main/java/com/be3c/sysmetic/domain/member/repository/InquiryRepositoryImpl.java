package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.dto.InquiryAdminListShowRequestDto;
import com.be3c.sysmetic.domain.member.dto.InquiryDetailAdminShowDto;
import com.be3c.sysmetic.domain.member.dto.InquiryDetailTraderInquirerShowDto;
import com.be3c.sysmetic.domain.member.dto.InquiryListShowRequestDto;
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
import java.util.Optional;

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
                predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

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


    public Page<Inquiry> pageInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquirerId = inquiryListShowRequestDto.getInquirerId();
        Long traderId = inquiryListShowRequestDto.getTraderId();
        InquiryStatus closed = inquiryListShowRequestDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        List<Inquiry> content = new ArrayList<>();
        long total;

        QueryResults<Inquiry> results = jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        content = results.getResults();
        total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    public List<Inquiry> listTraderInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquirerId = inquiryListShowRequestDto.getInquirerId();
        Long traderId = inquiryListShowRequestDto.getTraderId();
        InquiryStatus closed = inquiryListShowRequestDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 트레이더 별
        if (traderId != null) {
            predicate1.and(inquiry.trader.id.eq(traderId));
            predicate2.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()));

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

    public List<Inquiry> listInquirerInquirySearchWithBooleanBuilder(InquiryListShowRequestDto inquiryListShowRequestDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquirerId = inquiryListShowRequestDto.getInquirerId();
        Long traderId = inquiryListShowRequestDto.getTraderId();
        InquiryStatus closed = inquiryListShowRequestDto.getClosed();

        // 질문자 별
        if (inquirerId != null) {
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 트레이더 별
        if (traderId != null) {
            predicate1.and(inquiry.trader.id.eq(traderId));
            predicate2.and(inquiry.trader.id.eq(traderId));
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
    public Optional<Inquiry> adminFindPreviousInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

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
                predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

        predicate.and(inquiry.id.lt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst());
    }

    // 관리자 다음 문의 조회
    public Optional<Inquiry> adminFindNextInquiry(InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

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
                predicate.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
            } else if (searchType.equals("trader")) {
                predicate.and(inquiry.strategy.trader.nickname.contains(searchText));
            } else if (searchType.equals("inquirer")) {
                predicate.and(inquiry.inquirer.nickname.contains(searchText));
            }
        }

        predicate.and(inquiry.id.gt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst());
    }

    // 질문자 이전 문의 조회
    public Optional<Inquiry> inquirerFindPreviousInquiry(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();
        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()));
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()).not());

        predicate.and(inquiry.id.lt(inquiryId));

        // 정렬순 별
        if (sort.equals("registrationDate")) {
            return Optional.ofNullable(jpaQueryFactory
                    .selectFrom(inquiry)
                    .where(predicate)
                    .orderBy(inquiry.id.desc())
                    .fetchFirst());
        } else if (sort.equals("strategyName")) {

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
                return Optional.empty();
            } else {
                return Optional.ofNullable(content.get(0));
            }
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }
    }

    // 질문자 다음 문의 조회
    public Optional<Inquiry> inquirerFindNextInquiry(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();
        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        // 질문자 별
        if (inquirerId != null) {
            predicate.and(inquiry.inquirer.id.eq(inquirerId));
            predicate1.and(inquiry.inquirer.id.eq(inquirerId));
            predicate2.and(inquiry.inquirer.id.eq(inquirerId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()));
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.PUBLIC.getCode()).not());

        predicate.and(inquiry.id.gt(inquiryId));

        // 정렬순 별
        if (sort.equals("registrationDate")) {
            return Optional.ofNullable(jpaQueryFactory
                    .selectFrom(inquiry)
                    .where(predicate)
                    .orderBy(inquiry.id.asc())
                    .fetchFirst());
        } else if (sort.equals("strategyName")) {

            List<Inquiry> content = new ArrayList<>();
            List<Inquiry> results1 = jpaQueryFactory1
                    .selectFrom(inquiry)
                    .where(predicate1)
                    .orderBy(inquiry.strategy.name.desc(), inquiry.id.asc()) // 따로 해서 최적화 가능
                    .fetch();
            List<Inquiry> results2 = jpaQueryFactory2
                    .selectFrom(inquiry)
                    .where(predicate2)
                    .orderBy(inquiry.id.asc()) // 따로 해서 최적화 가능
                    .fetch();

            content.addAll(results1);
            content.addAll(results2);

            if (content.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(content.get(0));
            }
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }
    }

    // 질문자 이전 문의 조회 최신순
    public Optional<Inquiry> inquirerFindPreviousInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

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

        predicate.and(inquiry.id.lt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst());
    }

    // 질문자 이전 문의 조회 전략명순
    public Optional<Inquiry> inquirerFindPreviousInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

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
            return Optional.empty();
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == content.size() - 1) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(content.get(i+1));
                    }
                }
            }

            return Optional.empty();
        }
    }

    // 질문자 다음 문의 조회 최신순
    public Optional<Inquiry> inquirerFindNextInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

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

        predicate.and(inquiry.id.gt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst());
    }

    // 질문자 다음 문의 조회 전략명순
    public Optional<Inquiry> inquirerFindNextInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long inquirerId = inquiryDetailTraderInquirerShowDto.getInquirerId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

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
            return Optional.empty();
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == 0) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(content.get(i-1));
                    }
                }
            }

            return Optional.empty();
        }
    }

    // 트레이더 이전 문의 조회 최신순
    public Optional<Inquiry> traderFindPreviousInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderInquirerShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.id.lt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.desc())
                .fetchFirst());
    }

    // 트레이더 이전 문의 조회 전략명순
    public Optional<Inquiry> traderFindPreviousInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderInquirerShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        // 트레이더 별
        if (traderId != null) {
            predicate1.and(inquiry.trader.id.eq(traderId));
            predicate2.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()));

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
            return Optional.empty();
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == content.size() - 1) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(content.get(i+1));
                    }
                }
            }

            return Optional.empty();
        }
    }

    // 트레이더 다음 문의 조회 최신순
    public Optional<Inquiry> traderFindNextInquiryRegistrationDate(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderInquirerShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();

        // 트레이더 별
        if (traderId != null) {
            predicate.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate.and(inquiry.id.gt(inquiryId));

        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(inquiry)
                .where(predicate)
                .orderBy(inquiry.id.asc())
                .fetchFirst());
    }

    // 트레이더 다음 문의 조회 전략명순
    public Optional<Inquiry> traderFindNextInquiryStrategyName(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        BooleanBuilder predicate1 = new BooleanBuilder();
        BooleanBuilder predicate2 = new BooleanBuilder();

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        Long traderId = inquiryDetailTraderInquirerShowDto.getTraderId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        // 트레이더 별
        if (traderId != null) {
            predicate1.and(inquiry.trader.id.eq(traderId));
            predicate2.and(inquiry.trader.id.eq(traderId));
        }

        // 전체, 답변 대기, 답변 완료
        if (closed.equals(InquiryStatus.unclosed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        } else if (closed.equals(InquiryStatus.closed)) {
            predicate1.and(inquiry.inquiryStatus.eq(closed));
            predicate2.and(inquiry.inquiryStatus.eq(closed));
        }

        predicate1.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()).not());
        predicate2.and(inquiry.strategy.statusCode.eq(StrategyStatusCode.NOT_USING_STATE.getCode()));

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
            return Optional.empty();
        } else {

            for (int i = 0; i < content.size(); i++) {
                if (content.get(i).getId().equals(inquiryId)) {
                    if (i == 0) {
                        return Optional.empty();
                    } else {
                        return Optional.ofNullable(content.get(i-1));
                    }
                }
            }

            return Optional.empty();
        }
    }
}
