package com.be3c.sysmetic.domain.member.repository;

import com.be3c.sysmetic.domain.member.entity.Inquiry;
import com.be3c.sysmetic.domain.member.entity.Notice;
import com.be3c.sysmetic.global.util.admin.dto.AdminNoticeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> , NoticeRepositoryCustom {

    // NoticeRepositoryCustom에 QueryDSL로 검색 메소드

    @Query("select n from Notice n where n.id = :noticeId and n.isOpen = true")
    Optional<Notice> findByIdAndAndIsOpen(@Param("noticeId") Long noticeId);

    // 목록에서 삭제
    @Modifying(clearAutomatically = true)
    @Query("delete Notice n where n.id in :idList")
    int bulkDelete(@Param("idList") List<Long> idList);

    // 제목으로 찾기
    List<Notice> findByNoticeTitle(String noticeTitle);

//    // 관리자 이전 문의 조회
//    @Query("select n from Notice n where n.id < :noticeId order by n.id desc")
//    List<Notice> findPreviousNoticeAdmin(@Param("noticeId") Long noticeId, Pageable pageable);
//
//    // 관리자 다음 문의 조회
//    @Query("select n from Notice n where n.id > :noticeId order by n.id asc")
//    List<Notice> findNextNoticeAdmin(@Param("noticeId") Long noticeId, Pageable pageable);
//
//    // 일반 이전 문의 조회
//    @Query("select n from Notice n where n.id < :noticeId and n.isOpen = true order by n.id desc")
//    List<Notice> findPreviousNotice(@Param("noticeId") Long noticeId, Pageable pageable);
//
//    // 일반 다음 문의 조회
//    @Query("select n from Notice n where n.id > :noticeId and n.isOpen = true order by n.id asc")
//    List<Notice> findNextNotice(@Param("noticeId") Long noticeId, Pageable pageable);

    @Query("SELECT new com.be3c.sysmetic.global.util.admin.dto.AdminNoticeResponseDto(" +
            "n.id, n.noticeTitle, n.createdAt) FROM Notice n")
    Page<AdminNoticeResponseDto> findAdminMainNotice(Pageable pageable);
}
