package com.be3c.sysmetic.domain.strategy.repository;

import com.be3c.sysmetic.domain.strategy.dto.MethodGetResponseDto;
import com.be3c.sysmetic.domain.strategy.entity.Method;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MethodRepository extends JpaRepository<Method, Long> {
    Optional<Method> findById(Long id);
    Optional<Method> findByName(String name);
    Optional<Method> findByIdAndStatusCode(Long id, String statusCode);
    Optional<Method> findByNameAndStatusCode(String name, String statusCode);

    // 추후 f.file_path 추가 생각중.
    @Query("SELECT new com.be3c.sysmetic.domain.strategy.dto.MethodGetResponseDto(m.id, m.name, null) " +
           "FROM Method m WHERE m.statusCode = :statusCode")
    Page<MethodGetResponseDto> findAllByStatusCode(Pageable pageable, String statusCode);

    // 전략관리 페이지 매매방식 조회
    @Query("SELECT new com.be3c.sysmetic.domain.strategy.dto.MethodGetResponseDto(m.id, m.name, null) " +
            "FROM Method m WHERE m.statusCode = :statusCode")
    List<MethodGetResponseDto> findAllByStatusCode(String statusCode);
}
