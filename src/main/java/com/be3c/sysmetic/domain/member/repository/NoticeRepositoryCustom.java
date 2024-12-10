package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NoticeRepositoryCustom {

    Page<Notice> adminNoticeSearchWithBooleanBuilder(String searchType, String searchText, Pageable pageable);

    Page<Notice> noticeSearchWithBooleanBuilder(String searchText, Pageable pageable);

    Notice findPreviousNoticeAdmin(Long noticeId, String SearchType, String SearchText);

    Notice findNextNoticeAdmin(Long noticeId, String SearchType, String SearchText);

    Notice findPreviousNotice(Long noticeId, String SearchText);

    Notice findNextNotice(Long noticeId, String SearchText);
}
