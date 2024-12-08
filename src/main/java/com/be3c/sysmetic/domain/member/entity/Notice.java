package com.be3c.sysmetic.domain.member.entity;

import com.be3c.sysmetic.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString   //@ToString(of = ("id", "username", "age"))
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notice")
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notice_title", nullable = false, length = 100)
    private String noticeTitle;

    @Column(name = "notice_content", nullable = false, columnDefinition = "TEXT")
    private String noticeContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;

    @Column(name = "writer_nickname", nullable = false)
    private String writerNickname;

    @Column(name = "write_date", nullable = false)
    private LocalDateTime writeDate;

    // 화면엔 안 보임
    @Column(name = "corrector_id", nullable = false)
    private Long correctorId;

    @Column(name = "correct_date", nullable = false)
    private LocalDateTime correctDate;

    @Column(name = "hits", nullable = false)
    private Long hits;

    @Column(name = "file_exists", nullable = false)
    private Boolean fileExists;

    @Column(name = "image_exists", nullable = false)
    private Boolean imageExists;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    public static Notice createNotice(String noticeTitle,
                                      String noticeContent,
                                      Member writer,
                                      Boolean fileExists,
                                      Boolean imageExists,
                                      Boolean isOpen) {

        return Notice.builder()
                .noticeTitle(noticeTitle)
                .noticeContent(noticeContent)
                .writer(writer)
                .writerNickname(writer.getNickname())
                .writeDate(LocalDateTime.now())
                .correctorId(writer.getId())
                .correctDate(LocalDateTime.now())
                .hits(0L)
                .fileExists(fileExists)
                .imageExists(imageExists)
                .isOpen(isOpen)
                .build();
    }
}
