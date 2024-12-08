package com.be3c.sysmetic.domain.strategy.repository;

import com.be3c.sysmetic.domain.strategy.dto.MonthlyForRepo;
import com.be3c.sysmetic.domain.strategy.entity.Monthly;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.YearMonth;
import java.util.Optional;

@Repository
public interface MonthlyRepository extends JpaRepository<Monthly, Long> {

    // 특정 년월의 월간분석 데이터 조회
    // year, month null일 경우 전체 조회
    @Query("""
        SELECT m FROM Monthly m 
        WHERE m.strategy.id = :strategyId 
        AND (
            :startYearMonth IS NULL OR 
            (
                m.yearNumber > :#{#startYearMonth?.year} OR 
                (m.yearNumber = :#{#startYearMonth?.year} AND m.monthNumber >= :#{#startYearMonth?.monthValue})
            )
        )
        AND (
            :endYearMonth IS NULL OR 
            (
                m.yearNumber < :#{#endYearMonth?.year} OR 
                (m.yearNumber = :#{#endYearMonth?.year} AND m.monthNumber <= :#{#endYearMonth?.monthValue})
            )
        )
        ORDER BY m.yearNumber DESC, m.monthNumber DESC
    """)
    Page<Monthly> findAllByStrategyIdAndDateBetween(
            @Param("strategyId") Long strategyId,
            @Param("startYearMonth") YearMonth startYearMonth,
            @Param("endYearMonth") YearMonth endYearMonth,
            Pageable pageable
    );


    // 특정 전략의 월간분석 데이터 삭제
    void deleteAllByStrategyId(@Param("strategyId") Long strategyId);

    /* 엑셀을 위한 메서드 */
    List<Monthly> findAllByStrategyIdOrderByYearNumberAscMonthNumberAsc(Long strategyId);

    // 전략 상세 페이지에서 사용!
    @Query("SELECT new com.be3c.sysmetic.domain.strategy.dto.MonthlyForRepo(m.yearNumber, m.monthNumber, m.accumulatedProfitLossRate) From Monthly m "
            + "WHERE m.strategy.id = :strategyId ORDER BY m.yearNumber DESC, m.monthNumber ASC")
    List<MonthlyForRepo> findAllMonthlyRecord(@Param("strategyId") Long strategyId);

    @Modifying
    @Query("DELETE FROM Monthly m WHERE m.strategy.id = :strategyId")
    void deleteByStrategyId(Long strategyId);

    @Query("SELECT m FROM Monthly m WHERE m.strategy.id = :strategyId AND m.yearNumber = :year AND m.monthNumber = :month")
    Optional<Monthly> findByYearAndMonth(@Param("strategyId") Long strategyId, @Param("year") int year, @Param("month") int month);

    @Modifying
    @Query("DELETE FROM Monthly m WHERE m.strategy.id = :strategyId AND m.yearNumber = :year AND m.monthNumber = :month")
    void deleteSpecificMonthlyData(@Param("strategyId") Long strategyId, @Param("year") int year, @Param("month") int month);
}
