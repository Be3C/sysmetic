package com.be3c.sysmetic.domain.strategy.repository;

import com.be3c.sysmetic.domain.strategy.entity.StrategyStockReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface StrategyStockReferenceRepository extends JpaRepository<StrategyStockReference, Long> {
    @Query("""
        SELECT sr.stock.id FROM StrategyStockReference sr 
        WHERE sr.strategy.id = :strategyId
        ORDER BY sr.stock.id
    """)
    List<Long> findStockIdsByStrategyId(Long strategyId);

    @Modifying
    @Query("DELETE FROM StrategyStockReference s WHERE s.strategy.id = :strategyId")
    void deleteByStrategyId(Long strategyId);

    @Modifying
    @Query("""
        DELETE FROM StrategyStockReference sr 
        WHERE sr.strategy.id = :strategyId 
        AND sr.stock.id IN :stockIds
    """)
    void deleteByStrategyIdAndStockIds(Long strategyId, Set<Long> stockIds);

    // MainPage에서 사용! - 11/19
    List<StrategyStockReference> findByStrategyId(Long id);

    // 상세 검색에서 사용!
    @Query("SELECT s FROM StrategyStockReference s WHERE s.stock.id IN :ids ORDER BY s.stock.id")
    List<StrategyStockReference> findAllByStockIds(@Param("ids") List<Long> ids);

    // StockGetter에서 사용
    @Query("SELECT s FROM StrategyStockReference s WHERE s.strategy.id = :strategyId ORDER BY s.stock.id")
    List<StrategyStockReference> findAllByStrategyId(@Param("strategyId") Long strategyId);
}