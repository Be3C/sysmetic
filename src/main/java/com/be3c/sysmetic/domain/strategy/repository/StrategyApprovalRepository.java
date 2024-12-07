package com.be3c.sysmetic.domain.strategy.repository;

import com.be3c.sysmetic.domain.strategy.dto.AdminStrategyGetResponseDto;
import com.be3c.sysmetic.domain.strategy.entity.StrategyApprovalHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StrategyApprovalRepository extends JpaRepository<StrategyApprovalHistory, Long> {
    Optional<StrategyApprovalHistory> findByIdAndStatusCode(Long id, String statusCode);

    @Query("""
        SELECT
            s
        FROM
            StrategyApprovalHistory s
        WHERE
            s.strategy.id = :id
        AND s.statusCode = 'SA001'
    """)
    Optional<StrategyApprovalHistory> findByStrategyIdAndStatusCodeNotApproval(Long id);

    @Query("""
        SELECT
            s
        FROM
            StrategyApprovalHistory s
        WHERE
            s.strategy.id = :id
        AND s.statusCode = 'SA001'
    """)
    Optional<StrategyApprovalHistory> findByStrategyIdNotApproval(Long id);

    // 만약 정말 정말 동일한 시간 대에 승인 요청이 온 것이 있다면, 에러가 날 수 있다.

    /**
     * 1. 동일한 전략
     * 2. 완전히 동일한 시간 대에 발생한 승인 요청
     * 3. 해당 승인 요청만큼 중복된 전략이 반환된다.
     */
    @Query(value = """
            SELECT DISTINCT new com.be3c.sysmetic.domain.strategy.dto.AdminStrategyGetResponseDto(
                s.id,
                s.name,
                m.name,
                s.statusCode,
                coalesce(sa.statusCode, 'SA000'),
                s.createdAt,
                s.method.id,
                null,
                null
            )
            FROM Strategy s
            JOIN s.trader m
            LEFT JOIN (
                SELECT
                    sah.strategy.id AS strategyId,
                    sah.statusCode AS statusCode,
                    sah.modifiedAt AS modifiedAt
                FROM
                    StrategyApprovalHistory sah
                JOIN (
                    SELECT
                        sah2.strategy.id AS strategyId,
                        MAX(sah2.modifiedAt) AS maxModifiedAt
                    FROM
                        StrategyApprovalHistory sah2
                    GROUP BY
                        sah2.strategy.id
                ) sub ON sah.strategy.id = sub.strategyId AND sah.modifiedAt = sub.maxModifiedAt
            ) sa ON sa.strategyId = s.id
            WHERE
                (:openStatus IS NULL OR s.statusCode = :openStatus)
                AND (:approvalStatusCode IS NULL OR COALESCE(sa.statusCode, 'SA000') = :approvalStatusCode)
                AND (:strategyName IS NULL OR s.name LIKE CONCAT('%', :strategyName, '%'))
            ORDER BY s.createdAt ASC
        """,
            countQuery = """
            SELECT
                COUNT(*)
            FROM Strategy s
            JOIN s.trader m
            LEFT JOIN (
                SELECT sah.strategy.id AS strategyId, sah.statusCode AS statusCode, sah.modifiedAt AS modifiedAt
                FROM StrategyApprovalHistory sah
                JOIN (
                    SELECT sah2.strategy.id AS strategyId, MAX(sah2.modifiedAt) AS maxModifiedAt
                    FROM StrategyApprovalHistory sah2
                    GROUP BY sah2.strategy.id
                ) sub ON sah.strategy.id = sub.strategyId AND sah.modifiedAt = sub.maxModifiedAt
            ) sa ON sa.strategyId = s.id
            WHERE
                (:openStatus IS NULL OR s.statusCode = :openStatus)
                AND (:approvalStatusCode IS NULL OR COALESCE(sa.statusCode, 'SA000') = :approvalStatusCode)
                AND (:strategyName IS NULL OR s.name LIKE CONCAT('%', :strategyName, '%'))
            ORDER BY s.createdAt ASC
        """)
    /* 윈도우 함수 사용 쿼리
    @Query(value = """
        WITH LatestApprovalHistory AS (
            SELECT
                sa.strategy_id,
                sa.status_code,
                sa.modified_at,
                ROW_NUMBER() OVER (PARTITION BY sa.strategy_id ORDER BY sa.modified_at DESC) AS row_num
            FROM
                strategy_approval_history sa
        )
        SELECT
            s.id AS strategyId,
            s.strategy_name AS strategyName,
            m.name AS traderName,
            s.status_code AS openStatusCode,
            s.created_at AS strategyCreateDate,
            lah.status_code AS approvalStatusCode
        FROM
            strategy s
        JOIN
            member m ON s.member_id = m.id
        JOIN
            LatestApprovalHistory lah ON lah.strategy_id = s.id
        WHERE
            lah.row_num = 1;
    """, nativeQuery = true)
     */
    Page<AdminStrategyGetResponseDto> findStrategiesAdminPage(
            @Param("openStatus") String openStatus,
            @Param("approvalStatusCode") String approvalStatusCode,
            @Param("strategyName") String strategyName,
            Pageable pageable
    );

    @Query("""
    SELECT
    count(*)
    FROM StrategyApprovalHistory s
    WHERE s.statusCode = 'SA001'
    """)
    Long countWaitingStrategyCount();

    @Modifying
    @Query("DELETE FROM StrategyApprovalHistory h WHERE h.strategy.id = :strategyId")
    void deleteByStrategyId(Long strategyId);
}
