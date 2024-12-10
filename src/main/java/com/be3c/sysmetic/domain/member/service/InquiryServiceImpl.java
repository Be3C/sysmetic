package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.*;
import com.be3c.sysmetic.domain.member.exception.InquiryBadRequestException;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.message.InquiryExceptionMessage;
import com.be3c.sysmetic.domain.member.repository.InquiryAnswerRepository;
import com.be3c.sysmetic.domain.member.repository.InquiryRepository;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.strategy.dto.StockListDto;
import com.be3c.sysmetic.domain.strategy.dto.StrategyStatusCode;
import com.be3c.sysmetic.domain.strategy.entity.Strategy;
import com.be3c.sysmetic.domain.strategy.exception.StrategyExceptionMessage;
import com.be3c.sysmetic.domain.strategy.repository.StrategyRepository;
import com.be3c.sysmetic.domain.strategy.util.StockGetter;
import com.be3c.sysmetic.global.common.response.PageResponse;
import com.be3c.sysmetic.global.util.SecurityUtils;
import com.be3c.sysmetic.global.util.file.dto.FileReferenceType;
import com.be3c.sysmetic.global.util.file.dto.FileRequest;
import com.be3c.sysmetic.global.util.file.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final SecurityUtils securityUtils;

    private final InquiryRepository inquiryRepository;
    private final InquiryAnswerRepository inquiryAnswerRepository;
    private final MemberRepository memberRepository;
    private final StrategyRepository strategyRepository;

    private final FileService fileService;
    private final StockGetter stockGetter;

    private final Integer PAGE_SIZE = 10; // 한 페이지 크기

    // 전략 문의 등록 화면 조회
    @Override
    public InquirySavePageShowResponseDto getStrategyInquiryPage(Long strategyId) {

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new EntityNotFoundException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));

        if (strategy.getStatusCode().equals(StrategyStatusCode.NOT_USING_STATE.getCode())) {
            throw new EntityNotFoundException(StrategyExceptionMessage.INVALID_STATUS.getMessage());
        }

        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, strategy.getTrader().getId()));
        String methodIconPath = fileService.getFilePathNullable(new FileRequest(FileReferenceType.METHOD, strategy.getMethod().getId()));
        StockListDto stockList = stockGetter.getStocks(strategy.getId());

        return InquirySavePageShowResponseDto.builder()
                .methodId(strategy.getMethod().getId())
                .methodIconPath(methodIconPath)
                .cycle(strategy.getCycle())
                .stockList(stockList)
                .strategyId(strategy.getId())
                .strategyName(strategy.getName())
                .statusCode(strategy.getStatusCode())
                .traderId(strategy.getTrader().getId())
                .traderNickname(strategy.getTrader().getNickname())
                .traderProfileImagePath(traderProfileImagePath)
                .build();
    }

    // 등록
    @Override
    @Transactional
    public boolean registerInquiry(Long strategyId, InquirySaveRequestDto inquirySaveRequestDto) {

        Long memberId = securityUtils.getUserIdInSecurityContext();

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new EntityNotFoundException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));

        if (strategy.getStatusCode().equals(StrategyStatusCode.NOT_USING_STATE.getCode())) {
            throw new EntityNotFoundException(StrategyExceptionMessage.INVALID_STATUS.getMessage());
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException(MemberExceptionMessage.DATA_NOT_FOUND.getMessage()));

        Inquiry inquiry = Inquiry.createInquiry(strategy, member, inquirySaveRequestDto.getInquiryTitle(), inquirySaveRequestDto.getInquiryContent());

        inquiryRepository.save(inquiry);

        return true;
    }

    // 수정
    @Override
    @Transactional
    public boolean modifyInquiry(Long inquiryId, InquiryModifyRequestDto inquiryModifyRequestDto) {

        Long userId = securityUtils.getUserIdInSecurityContext();
        Inquiry inquiry = inquiryRepository.findByIdAndInquirerAndStatusCode(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(!userId.equals(inquiry.getInquirer().getId())) {
            throw new InquiryBadRequestException(InquiryExceptionMessage.NOT_INQUIRY_WRITER.getMessage());
        }

        if (inquiry.getInquiryStatus() == InquiryStatus.unclosed) {
            inquiry.setInquiryTitle(inquiryModifyRequestDto.getInquiryTitle());
            inquiry.setInquiryContent(inquiryModifyRequestDto.getInquiryContent());
            inquiryRepository.save(inquiry);
        } else {
            throw new IllegalStateException("답변이 등록된 문의는 수정할 수 없습니다.");
        }

        return true;
    }

//    질문자 삭제
    @Override
    @Transactional
    public boolean deleteInquiry(Long inquiryId) {

        Long userId = securityUtils.getUserIdInSecurityContext();
        Inquiry inquiry = inquiryRepository.findByIdAndInquirerAndStatusCode(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(!userId.equals(inquiry.getInquirer().getId())) {
            throw new InquiryBadRequestException(InquiryExceptionMessage.NOT_INQUIRY_WRITER.getMessage());
        }

        if (inquiry.getInquiryStatus() == InquiryStatus.unclosed) {
            inquiryRepository.delete(inquiry);
        } else {
            throw new IllegalStateException("답변이 등록된 문의는 삭제할 수 없습니다.");
        }

        return true;
    }

    // 관리자 삭제
    @Override
    @Transactional
    public boolean deleteAdminInquiry(Long inquiryId) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));
        inquiryAnswerRepository.deleteByinquiryId(inquiryId);
        inquiryRepository.delete(inquiry);


        return true;
    }


    // 관리자 목록 삭제
    @Override
    @Transactional
    public Map<Long, String> deleteAdminInquiryList(InquiryAdminListDeleteRequestDto inquiryAdminListDeleteRequestDto) {

        List<Long> inquiryIdList = inquiryAdminListDeleteRequestDto.getInquiryIdList();

        if (inquiryIdList == null || inquiryIdList.isEmpty()) {
            throw new IllegalArgumentException("문의가 한 개도 선택되지 않았습니다.");
        }

        Map<Long, String> failDelete = new HashMap<>();
        List<Long> successDelete = new ArrayList<>();

        for (Long inquiryId : inquiryIdList) {

            try {

                inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));
                inquiryAnswerRepository.deleteByinquiryId(inquiryId);
                successDelete.add(inquiryId);
            }
            catch (EntityNotFoundException e) {
                failDelete.put(inquiryId, InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage());
            }
        }


        inquiryRepository.bulkDelete(successDelete);

        return failDelete;
    }


    // 관리자 검색 조회
    // 전체, 답변 대기, 답변 완료
    // 검색 (전략명, 트레이더, 질문자)
    @Override
    public PageResponse<InquiryAdminListOneShowResponseDto> findInquiriesAdmin(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Integer page) {

        Page<Inquiry> inquiryList = inquiryRepository.adminInquirySearchWithBooleanBuilder(inquiryAdminListShowRequestDto, PageRequest.of(page, PAGE_SIZE));

        List<InquiryAdminListOneShowResponseDto> inquiryDtoList = inquiryList.stream()
                .map(this::getInquiryAdminOneResponseDto).collect(Collectors.toList());

        return PageResponse.<InquiryAdminListOneShowResponseDto>builder()
                .currentPage(inquiryList.getNumber())
                .pageSize(PAGE_SIZE)
                .totalElement(inquiryList.getTotalElements())
                .totalPages(inquiryList.getTotalPages())
                .content(inquiryDtoList)
                .build();
    }

    private InquiryAdminListOneShowResponseDto getInquiryAdminOneResponseDto(Inquiry inquiry) {

        InquiryStrategyDataDto inquiryStrategyDataDto = adminTraderGetInquiryStrategyDataDto(inquiry);

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }

        return InquiryAdminListOneShowResponseDto.builder()
                .inquiryId(inquiry.getId())
                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    private InquiryPreviousNextDto getInquiryPreviousNextDto(Inquiry inquiry) {

        Long inquiryId;
        String inquiryTitle;
        LocalDateTime inquiryWriteDate;

        if (inquiry == null) {
            inquiryId = null;
            inquiryTitle = null;
            inquiryWriteDate = null;
        } else {
            inquiryId = inquiry.getId();
            inquiryTitle = inquiry.getInquiryTitle();
            inquiryWriteDate = inquiry.getInquiryRegistrationDate();
        }

        return InquiryPreviousNextDto.builder()
                .inquiryId(inquiryId)
                .inquiryTitle(inquiryTitle)
                .inquiryWriteDate(inquiryWriteDate)
                .build();
    }

    private InquiryStrategyDataDto adminTraderGetInquiryStrategyDataDto(Inquiry inquiry) {

        Long methodId = inquiry.getStrategy().getMethod().getId();
        String methodIconPath = fileService.getFilePathNullable(new FileRequest(FileReferenceType.METHOD, methodId));
        Character cycle = inquiry.getStrategy().getCycle();
        StockListDto stockList = stockGetter.getStocks(inquiry.getStrategy().getId());
        Long strategyId = inquiry.getStrategy().getId();
        String strategyName = inquiry.getStrategy().getName();
        String statusCode = inquiry.getStrategy().getStatusCode();

        return InquiryStrategyDataDto.builder()
                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)
                .build();
    }

    private InquiryStrategyDataDto inquirerGetInquiryStrategyDataDto(Inquiry inquiry) {

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (inquiry.getStrategy().getStatusCode().equals(StrategyStatusCode.PUBLIC.getCode())) {
            methodId = inquiry.getStrategy().getMethod().getId();
            methodIconPath = fileService.getFilePathNullable(new FileRequest(FileReferenceType.METHOD, methodId));
            cycle = inquiry.getStrategy().getCycle();
            stockList = stockGetter.getStocks(inquiry.getStrategy().getId());
            strategyId = inquiry.getStrategy().getId();
            strategyName = inquiry.getStrategy().getName();
            statusCode = inquiry.getStrategy().getStatusCode();
        } else {
            methodId = null;
            methodIconPath = null;
            cycle = null;
            stockList = null;
            strategyId = null;
            strategyName = null;
            statusCode = null;
        }

        return InquiryStrategyDataDto.builder()
                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)
                .build();
    }

    private InquiryAnswerDataDto getInquiryAnswerDataDto(Inquiry inquiry, Long inquiryId) {

        Long inquiryAnswerId;
        String answerTitle;
        LocalDateTime answerRegistrationDate;
        String answerContent;

        if (inquiry.getInquiryStatus() == InquiryStatus.unclosed) {
            inquiryAnswerId = null;
            answerTitle = null;
            answerRegistrationDate = null;
            answerContent = null;
        } else {
            InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryId(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY_ANSWER.getMessage()));
            inquiryAnswerId = inquiryAnswer.getId();
            answerTitle = inquiryAnswer.getAnswerTitle();
            answerRegistrationDate = inquiryAnswer.getAnswerRegistrationDate();
            answerContent = inquiryAnswer.getAnswerContent();
        }

        return InquiryAnswerDataDto.builder()
                .inquiryAnswerId(inquiryAnswerId)
                .answerTitle(answerTitle)
                .answerContent(answerContent)
                .answerRegistrationDate(answerRegistrationDate)
                .build();
    }


    @Override
    public InquiryDetailAdminShowResponseDto getInquiryAdminDetail(InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

        Long inquiryId = inquiryDetailAdminShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailAdminShowDto.getClosed();
        String searchType = inquiryDetailAdminShowDto.getSearchType();
        String searchText = inquiryDetailAdminShowDto.getSearchText();

        Inquiry inquiry = inquiryRepository.findByIdAndStatusCode(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        Inquiry previousInquiry = inquiryRepository.adminFindPreviousInquiry(inquiryDetailAdminShowDto);
        InquiryPreviousNextDto inquiryPreviousDto = getInquiryPreviousNextDto(previousInquiry);

        Inquiry nextInquiry = inquiryRepository.adminFindNextInquiry(inquiryDetailAdminShowDto);
        InquiryPreviousNextDto inquiryNextDto = getInquiryPreviousNextDto(nextInquiry);

        InquiryAnswerDataDto inquiryAnswerDataDto = getInquiryAnswerDataDto(inquiry, inquiryId);

        InquiryStrategyDataDto inquiryStrategyDataDto = adminTraderGetInquiryStrategyDataDto(inquiry);

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryDetailAdminShowResponseDto.builder()
                .closed(String.valueOf(closed))
                .searchType(searchType)
                .searchText(searchText)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerDataDto.getInquiryAnswerId())

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())

                .inquiryContent(inquiry.getInquiryContent())

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .answerTitle(inquiryAnswerDataDto.getAnswerTitle())
                .answerRegistrationDate(inquiryAnswerDataDto.getAnswerRegistrationDate())
                .answerContent(inquiryAnswerDataDto.getAnswerContent())

                .previousId(inquiryPreviousDto.getInquiryId())
                .previousTitle(inquiryPreviousDto.getInquiryTitle())
                .previousWriteDate(inquiryPreviousDto.getInquiryWriteDate())
                .nextId(inquiryNextDto.getInquiryId())
                .nextTitle(inquiryNextDto.getInquiryTitle())
                .nextWriteDate(inquiryNextDto.getInquiryWriteDate())
                .build();
    }

    private InquiryListOneShowResponseDto getInquirerInquiryListOneResponseDto(Inquiry inquiry) {

        InquiryStrategyDataDto inquiryStrategyDataDto = inquirerGetInquiryStrategyDataDto(inquiry);

        return InquiryListOneShowResponseDto.builder()
                .inquiryId(inquiry.getId())
                .inquiryTitle(inquiry.getInquiryTitle())

                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())

                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    private InquiryListOneShowResponseDto getTraderInquiryListOneResponseDto(Inquiry inquiry) {

        InquiryStrategyDataDto inquiryStrategyDataDto = adminTraderGetInquiryStrategyDataDto(inquiry);

        return InquiryListOneShowResponseDto.builder()
                .inquiryId(inquiry.getId())
                .inquiryTitle(inquiry.getInquiryTitle())

                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())

                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    @Override
    public InquiryDetailInquirerShowResponseDto getInquirerInquiryDetail(InquiryDetailInquirerShowDto inquiryDetailInquirerShowDto) {

        Long inquiryId = inquiryDetailInquirerShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailInquirerShowDto.getClosed();
        String sort = inquiryDetailInquirerShowDto.getSort();

        Long userId = securityUtils.getUserIdInSecurityContext();

        Inquiry inquiry = inquiryRepository.findByIdAndInquirerAndStatusCode(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        inquiryDetailInquirerShowDto.setInquirerId(userId);

        InquiryPreviousNextDto inquiryPreviousDto;
        InquiryPreviousNextDto inquiryNextDto;

        if (sort.equals("registrationDate")) {
            Inquiry previousInquiry = inquiryRepository.inquirerFindPreviousInquiryRegistrationDate(inquiryDetailInquirerShowDto);
            inquiryPreviousDto = getInquiryPreviousNextDto(previousInquiry);

            Inquiry nextInquiry = inquiryRepository.inquirerFindNextInquiryRegistrationDate(inquiryDetailInquirerShowDto);
            inquiryNextDto = getInquiryPreviousNextDto(nextInquiry);

        } else { // strategyName
            Inquiry previousInquiry = inquiryRepository.inquirerFindPreviousInquiryStrategyName(inquiryDetailInquirerShowDto);
            inquiryPreviousDto = getInquiryPreviousNextDto(previousInquiry);

            Inquiry nextInquiry = inquiryRepository.inquirerFindNextInquiryStrategyName(inquiryDetailInquirerShowDto);
            inquiryNextDto = getInquiryPreviousNextDto(nextInquiry);

        }

        InquiryAnswerDataDto inquiryAnswerDataDto = getInquiryAnswerDataDto(inquiry, inquiryId);

        InquiryStrategyDataDto inquiryStrategyDataDto = inquirerGetInquiryStrategyDataDto(inquiry);

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryDetailInquirerShowResponseDto.builder()
                .closed(closed)
                .sort(sort)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerDataDto.getInquiryAnswerId())

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .inquiryContent(inquiry.getInquiryContent())

                .answerTitle(inquiryAnswerDataDto.getAnswerTitle())
                .answerRegistrationDate(inquiryAnswerDataDto.getAnswerRegistrationDate())
                .answerContent(inquiryAnswerDataDto.getAnswerContent())

                .previousId(inquiryPreviousDto.getInquiryId())
                .previousTitle(inquiryPreviousDto.getInquiryTitle())
                .previousWriteDate(inquiryPreviousDto.getInquiryWriteDate())
                .nextId(inquiryNextDto.getInquiryId())
                .nextTitle(inquiryNextDto.getInquiryTitle())
                .nextWriteDate(inquiryNextDto.getInquiryWriteDate())
                .build();
    }

    @Override
    public InquiryDetailTraderShowResponseDto getTraderInquiryDetail(InquiryDetailTraderShowDto inquiryDetailTraderShowDto) {

        Long inquiryId = inquiryDetailTraderShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailTraderShowDto.getClosed();
        String sort = inquiryDetailTraderShowDto.getSort();

        Long userId = securityUtils.getUserIdInSecurityContext();

        Inquiry inquiry = inquiryRepository.findByIdAndTraderAndStatusCode(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        inquiryDetailTraderShowDto.setTraderId(userId);

        InquiryPreviousNextDto inquiryPreviousDto;
        InquiryPreviousNextDto inquiryNextDto;

        if (sort.equals("registrationDate")) {
            Inquiry previousInquiry = inquiryRepository.traderFindPreviousInquiryRegistrationDate(inquiryDetailTraderShowDto);
            inquiryPreviousDto = getInquiryPreviousNextDto(previousInquiry);

            Inquiry nextInquiry = inquiryRepository.traderFindNextInquiryRegistrationDate(inquiryDetailTraderShowDto);
            inquiryNextDto = getInquiryPreviousNextDto(nextInquiry);

        } else { // strategyName
            Inquiry previousInquiry = inquiryRepository.traderFindPreviousInquiryStrategyName(inquiryDetailTraderShowDto);
            inquiryPreviousDto = getInquiryPreviousNextDto(previousInquiry);

            Inquiry nextInquiry = inquiryRepository.traderFindNextInquiryStrategyName(inquiryDetailTraderShowDto);
            inquiryNextDto = getInquiryPreviousNextDto(nextInquiry);

        }

        InquiryAnswerDataDto inquiryAnswerDataDto = getInquiryAnswerDataDto(inquiry, inquiryId);

        InquiryStrategyDataDto inquiryStrategyDataDto = adminTraderGetInquiryStrategyDataDto(inquiry);

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryDetailTraderShowResponseDto.builder()
                .closed(closed)
                .sort(sort)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerDataDto.getInquiryAnswerId())

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(inquiryStrategyDataDto.getMethodId())
                .methodIconPath(inquiryStrategyDataDto.getMethodIconPath())
                .cycle(inquiryStrategyDataDto.getCycle())
                .stockList(inquiryStrategyDataDto.getStockList())
                .strategyId(inquiryStrategyDataDto.getStrategyId())
                .strategyName(inquiryStrategyDataDto.getStrategyName())
                .statusCode(inquiryStrategyDataDto.getStatusCode())

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .inquiryContent(inquiry.getInquiryContent())

                .answerTitle(inquiryAnswerDataDto.getAnswerTitle())
                .answerRegistrationDate(inquiryAnswerDataDto.getAnswerRegistrationDate())
                .answerContent(inquiryAnswerDataDto.getAnswerContent())

                .previousId(inquiryPreviousDto.getInquiryId())
                .previousTitle(inquiryPreviousDto.getInquiryTitle())
                .previousWriteDate(inquiryPreviousDto.getInquiryWriteDate())
                .nextId(inquiryNextDto.getInquiryId())
                .nextTitle(inquiryNextDto.getInquiryTitle())
                .nextWriteDate(inquiryNextDto.getInquiryWriteDate())
                .build();

    }

    // 문의자 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    @Override
    public PageResponse<InquiryListOneShowResponseDto> showInquirerInquiry(Integer page, String sort, InquiryStatus inquiryStatus) {

        Long userId = securityUtils.getUserIdInSecurityContext();

        InquiryInquirerListShowRequestDto inquiryInquirerListShowRequestDto = new InquiryInquirerListShowRequestDto();
        inquiryInquirerListShowRequestDto.setInquirerId(userId);
        inquiryInquirerListShowRequestDto.setClosed(inquiryStatus);

        List<InquiryListOneShowResponseDto> inquiryDtoList;
        PageResponse<InquiryListOneShowResponseDto> inquiryPage;

        if (sort.equals("registrationDate")) {

            Page<Inquiry> inquiryList = inquiryRepository.registrationDateInquirerInquirySearchWithBooleanBuilder(inquiryInquirerListShowRequestDto, PageRequest.of(page, PAGE_SIZE));

            inquiryDtoList = inquiryList.stream()
                    .map(this::getInquirerInquiryListOneResponseDto).collect(Collectors.toList());

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(inquiryList.getNumber())
                    .pageSize(PAGE_SIZE)
                    .totalElement(inquiryList.getTotalElements())
                    .totalPages(inquiryList.getTotalPages())
                    .content(inquiryDtoList)
                    .build();

        } else { // strategyName

            List<Inquiry> inquiryList = inquiryRepository.strategyNameInquirerInquirySearchWithBooleanBuilder(inquiryInquirerListShowRequestDto);
            int totalCountInquiry = inquiryList.size(); // 전체 데이터 수

            int totalPageCount; // 전체 페이지 수
            int pageStart = page * PAGE_SIZE; // 페이지 시작 위치
            int pageEnd;

            if (totalCountInquiry == 0) {
                totalPageCount = 0;
                inquiryDtoList = null;
            } else {
                if (totalCountInquiry % PAGE_SIZE == 0) {
                    totalPageCount = (int) (totalCountInquiry / (double) PAGE_SIZE);
                } else {
                    totalPageCount = (int) (totalCountInquiry / (double) PAGE_SIZE) + 1;
                }

                if (page + 1 != totalPageCount) {
                    pageEnd = (page + 1) * PAGE_SIZE - 1;
                } else {
                    pageEnd = totalCountInquiry - 1;
                }

                List<Inquiry> inquiryListCut = new ArrayList<>();
                for (int i = pageStart; i <= pageEnd; i++) {
                    System.out.println("i: " + i);
                    inquiryListCut.add(inquiryList.get(i));
                }

                inquiryDtoList = inquiryListCut.stream()
                        .map(this::getInquirerInquiryListOneResponseDto).collect(Collectors.toList());
            }

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(page) // 현재 페이지
                    .pageSize(PAGE_SIZE) // 한 페이지 크기
                    .totalElement(totalCountInquiry) // 전체 데이터 수
                    .totalPages(totalPageCount) // 전체 페이지 수
                    .content(inquiryDtoList)
                    .build();
        }

        return inquiryPage;
    }

    // 트레이더 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    @Override
    public PageResponse<InquiryListOneShowResponseDto> showTraderInquiry(Integer page, String sort, InquiryStatus inquiryStatus) {

        Long userId = securityUtils.getUserIdInSecurityContext();

        InquiryTraderListShowRequestDto inquiryTraderListShowRequestDto = new InquiryTraderListShowRequestDto();
        inquiryTraderListShowRequestDto.setTraderId(userId);
        inquiryTraderListShowRequestDto.setClosed(inquiryStatus);

        List<InquiryListOneShowResponseDto> inquiryDtoList;
        PageResponse<InquiryListOneShowResponseDto> inquiryPage;

        if (sort.equals("registrationDate")) {

            Page<Inquiry> inquiryList = inquiryRepository.registrationDateTraderInquirySearchWithBooleanBuilder(inquiryTraderListShowRequestDto, PageRequest.of(page, PAGE_SIZE));

            inquiryDtoList = inquiryList.stream()
                    .map(this::getTraderInquiryListOneResponseDto).collect(Collectors.toList());

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(inquiryList.getNumber())
                    .pageSize(PAGE_SIZE)
                    .totalElement(inquiryList.getTotalElements())
                    .totalPages(inquiryList.getTotalPages())
                    .content(inquiryDtoList)
                    .build();

        } else { // strategyName

            Page<Inquiry> inquiryList = inquiryRepository.strategyNameTraderInquirySearchWithBooleanBuilder(inquiryTraderListShowRequestDto, PageRequest.of(page, PAGE_SIZE));

            inquiryDtoList = inquiryList.stream()
                    .map(this::getTraderInquiryListOneResponseDto).collect(Collectors.toList());

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(inquiryList.getNumber())
                    .pageSize(PAGE_SIZE)
                    .totalElement(inquiryList.getTotalElements())
                    .totalPages(inquiryList.getTotalPages())
                    .content(inquiryDtoList)
                    .build();
        }

        return inquiryPage;
    }

    @Override
    public InquiryModifyPageShowResponseDto showInquiryModifyPage(Long inquiryId) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryExceptionMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(!securityUtils.getUserIdInSecurityContext().equals(inquiry.getInquirer().getId())) {
            throw new InquiryBadRequestException(InquiryExceptionMessage.NOT_INQUIRY_WRITER.getMessage());
        }

        return InquiryModifyPageShowResponseDto.builder()
                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryContent(inquiry.getInquiryContent())
                .build();
    }
}
