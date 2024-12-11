package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.domain.member.entity.NoticeSearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {

    Page<Notice> adminNoticeSearchWithBooleanBuilder(NoticeSearchType searchType, String searchText, Pageable pageable);

    Page<Notice> noticeSearchWithBooleanBuilder(String searchText, Pageable pageable);

    Notice findPreviousNoticeAdmin(Long noticeId, NoticeSearchType SearchType, String SearchText);

    Notice findNextNoticeAdmin(Long noticeId, NoticeSearchType SearchType, String SearchText);

    Notice findPreviousNotice(Long noticeId, String SearchText);

    Notice findNextNotice(Long noticeId, String SearchText);
}
