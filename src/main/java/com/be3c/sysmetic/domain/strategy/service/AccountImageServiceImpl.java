package com.be3c.sysmetic.domain.strategy.service;

import com.be3c.sysmetic.domain.strategy.dto.AccountImageRequestDto;
import com.be3c.sysmetic.domain.strategy.dto.AccountImageResponseDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.be3c.sysmetic.domain.strategy.entity.AccountImage;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.exception.StrategyBadRequestException;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.repository.AccountImageRepository;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
@Service
public class AccountImageServiceImpl implements AccountImageService {

    /*
    전략 실계좌 이미지 조회

    1. 클라이언트에서 실계좌 정보 조회 API 요청
    2. 트레이더, 전략에 일치하는 실계좌 페이징하여 조회
     */

    private final AccountImageRepository accountImageRepository;
    private final StrategyRepository strategyRepository;
    private final SecurityUtils securityUtils;
    private final Integer size = 10;

    // 실계좌이미지 조회 - PUBLIC 상태인 전략의 실계좌이미지 조회
    @Override
    public PageResponse<AccountImageResponseDto> findAccountImages(Long strategyId, Integer page) {
        Pageable pageable = PageRequest.of(page, size);

        // 전략 상태 PUBLIC 여부 검증
        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));
        if(!strategy.getStatusCode().equals(StrategyStatusCode.PUBLIC.name())) {
            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_STATUS.getMessage());
        }

        Page<AccountImageResponseDto> accountImageResponseDtoPage = accountImageRepository.findAllByStrategyIdOrderByAccountImageCreatedAt(strategyId, pageable).map(this::entityToDto);

        return PageResponse.<AccountImageResponseDto>builder()
                .currentPage(accountImageResponseDtoPage.getPageable().getPageNumber())
                .pageSize(accountImageResponseDtoPage.getPageable().getPageSize())
                .totalElement(accountImageResponseDtoPage.getTotalElements())
                .totalPages(accountImageResponseDtoPage.getTotalPages())
                .content(accountImageResponseDtoPage.getContent())
                .build();
    }

    /*
    실계좌이미지 조회
    1) 트레이더
    본인의 전략이면서 공개, 비공개, 승인대기 상태의 전략 조회 가능
    2) 관리자
    모든 상태의 전략 조회 가능
     */
    @Override
    public PageResponse<AccountImageResponseDto> findTraderAccountImages(Long strategyId, Integer page) {
        Pageable pageable = PageRequest.of(page, size);

        String userRole = securityUtils.getUserRoleInSecurityContext();

        // trader일 경우, 본인의 전략인지 검증
        if(userRole.equals("TRADER")) {
            validUser(strategyId);
        }

        // member일 경우, 권한 없음 처리
        if(userRole.equals("MEMBER")) {
            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_STATUS.getMessage());
        }

        // 전략 상태 NOT_USING_STATE 일 경우 예외 처리
        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));
        if(!strategy.getStatusCode().equals(StrategyStatusCode.NOT_USING_STATE.name())) {
            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_STATUS.getMessage());
        }

        Page<AccountImageResponseDto> accountImageResponseDtoPage = accountImageRepository.findAllByStrategyIdOrderByAccountImageCreatedAt(strategyId, pageable).map(this::entityToDto);

        return PageResponse.<AccountImageResponseDto>builder()
                .currentPage(accountImageResponseDtoPage.getPageable().getPageNumber())
                .pageSize(accountImageResponseDtoPage.getPageable().getPageSize())
                .totalElement(accountImageResponseDtoPage.getTotalElements())
                .totalPages(accountImageResponseDtoPage.getTotalPages())
                .content(accountImageResponseDtoPage.getContent())
                .build();
    }

    // 실계좌이미지 삭제
    public void deleteAccountImage(Long accountImageId) {
        AccountImage accountImage = accountImageRepository.findById(accountImageId).orElseThrow(() -> new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));

        // todo security 적용 후 주석 해제
//        if(securityUtils.getUserIdInSecurityContext() != accountImage.getCreatedBy()) {
//            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_MEMBER.getMessage());
//        }

        accountImageRepository.deleteById(accountImageId);
    }

    // 실계좌이미지 등록
    @Transactional
    public void saveAccountImage(Long strategyId, List<AccountImageRequestDto> requestDtoList) {
        // todo security 적용 후 주석 해제
//        if(securityUtils.getUserIdInSecurityContext() != accountImage.getCreatedBy()) {
//            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_MEMBER.getMessage());
//        }

        List<AccountImage> accountImageList = requestDtoList.stream().map(requestDto -> AccountImage.builder()
                .title(requestDto.getTitle())
                .strategy(findStrategyByStrategyId(strategyId))
                .build()).collect(Collectors.toList());

        // todo. 이미지 파일 S3 업로드 로직 필요
        // requestDtos.get(0).getImage();

        accountImageRepository.saveAll(accountImageList);
    }

    // 현재 로그인한 유저와 전략 업로드한 유저가 일치하는지 검증
    private void validUser(Long strategyId) {
        Long userId = securityUtils.getUserIdInSecurityContext();
        Long uploadedTraderId = strategyRepository.findById(strategyId).get().getTrader().getId();

        if(!uploadedTraderId.equals(userId)) {
            throw new StrategyBadRequestException(StrategyExceptionMessage.INVALID_MEMBER.getMessage());
        }
    }

    private AccountImageResponseDto entityToDto(AccountImage accountImage) {
        return AccountImageResponseDto.builder()
                .accountImageId(accountImage.getId())
                .title(accountImage.getTitle())
                // .imageUrl() todo. 파일 DB 조인 필요
                .build();
    }

    private Strategy findStrategyByStrategyId(Long strategyId) {
        return strategyRepository.findById(strategyId).orElseThrow(() -> new StrategyBadRequestException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));
    }

}
