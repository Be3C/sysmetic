package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.domain.member.entity.QNotice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.apache.poi.sl.draw.geom.GuideIf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    private final QNotice notice = QNotice.notice;

    @Override
    public Page<Notice> adminNoticeSearchWithBooleanBuilder(String searchType, String searchText, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        // 검색 (제목, 내용, 제목+내용, 작성자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("title")) {
                predicate.and(notice.noticeTitle.contains(searchText));
            } else if (searchType.equals("content")) {
                predicate.and(notice.noticeContent.contains(searchText));
            } else if (searchType.equals("all")) {
                predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
            } else if (searchType.equals("writer")) {
                predicate.and(notice.writerNickname.contains(searchText));
            }
        }

        QueryResults<Notice> results = jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Notice> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }


    @Override
    public Page<Notice> noticeSearchWithBooleanBuilder(String searchText, Pageable pageable) {

        BooleanBuilder predicate = new BooleanBuilder();

        if (StringUtils.hasText(searchText)) {
            predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
        }

        predicate.and(notice.isOpen.eq(true));

        QueryResults<Notice> results = jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.desc()) // 따로 해서 최적화 가능
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<Notice> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Notice findPreviousNoticeAdmin(Long noticeId, String searchType, String searchText) {

        BooleanBuilder predicate = new BooleanBuilder();

        // 검색 (제목, 내용, 제목+내용, 작성자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("title")) {
                predicate.and(notice.noticeTitle.contains(searchText));
            } else if (searchType.equals("content")) {
                predicate.and(notice.noticeContent.contains(searchText));
            } else if (searchType.equals("all")) {
                predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
            } else if (searchType.equals("writer")) {
                predicate.and(notice.writerNickname.contains(searchText));
            }
        }

        predicate.and(notice.id.lt(noticeId));

        return jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.desc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }

    @Override
    public Notice findNextNoticeAdmin(Long noticeId, String searchType, String searchText) {

        BooleanBuilder predicate = new BooleanBuilder();

        // 검색 (제목, 내용, 제목+내용, 작성자)
        if (StringUtils.hasText(searchText)) {
            if (searchType.equals("title")) {
                predicate.and(notice.noticeTitle.contains(searchText));
            } else if (searchType.equals("content")) {
                predicate.and(notice.noticeContent.contains(searchText));
            } else if (searchType.equals("all")) {
                predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
            } else if (searchType.equals("writer")) {
                predicate.and(notice.writerNickname.contains(searchText));
            }
        }

        predicate.and(notice.id.gt(noticeId));

        return jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.asc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }

    @Override
    public Notice findPreviousNotice(Long noticeId, String searchText) {

        BooleanBuilder predicate = new BooleanBuilder();

        if (StringUtils.hasText(searchText)) {
            predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
        }

        predicate.and(notice.isOpen.eq(true));

        predicate.and(notice.id.lt(noticeId));

        return jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.desc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }

    @Override
    public Notice findNextNotice(Long noticeId, String searchText) {

        BooleanBuilder predicate = new BooleanBuilder();

        if (StringUtils.hasText(searchText)) {
            predicate.andAnyOf(notice.noticeTitle.contains(searchText), notice.noticeContent.contains(searchText));
        }

        predicate.and(notice.isOpen.eq(true));

        predicate.and(notice.id.gt(noticeId));

        return jpaQueryFactory
                .selectFrom(notice)
                .where(predicate)
                .orderBy(notice.id.asc()) // 따로 해서 최적화 가능
                .fetchFirst();
    }

}
