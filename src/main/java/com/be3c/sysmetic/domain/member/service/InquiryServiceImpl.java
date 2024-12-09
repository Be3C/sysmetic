package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.*;
import com.be3c.sysmetic.domain.member.entity.*;
import com.be3c.sysmetic.domain.member.exception.InquiryNotWriterException;
import com.be3c.sysmetic.domain.member.exception.MemberExceptionMessage;
import com.be3c.sysmetic.domain.member.message.InquiryFailMessage;
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

    private final Integer pageSize = 10; // 한 페이지 크기

    // 전략 문의 등록 화면 조회
    @Override
    public InquirySavePageShowResponseDto findStrategyForInquiryPage(Long strategyId) {

        Strategy strategy = strategyRepository.findById(strategyId).orElseThrow(() -> new EntityNotFoundException(StrategyExceptionMessage.DATA_NOT_FOUND.getMessage()));

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
        Inquiry inquiry = inquiryRepository.findByIdAndAndIsOpenInquirer(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

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
        Inquiry inquiry = inquiryRepository.findByIdAndAndIsOpenInquirer(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

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

        inquiryAnswerRepository.deleteByinquiryId(inquiryId);
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));
        inquiryRepository.delete(inquiry);


        return true;
    }


    // 관리자 목록 삭제
    @Override
    @Transactional
    public Map<Long, String> deleteAdminInquiryList(List<Long> inquiryIdList) {

        Map<Long, String> failDelete = new HashMap<>();

        for (Long inquiryId : inquiryIdList) {

            inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));
            inquiryAnswerRepository.deleteByinquiryId(inquiryId);
        }

        inquiryRepository.bulkDelete(inquiryIdList);

        return failDelete;
    }


    // 관리자 검색 조회
    // 전체, 답변 대기, 답변 완료
    // 검색 (전략명, 트레이더, 질문자)
    @Override
    public PageResponse<InquiryAdminListOneShowResponseDto> findInquiriesAdmin(InquiryAdminListShowRequestDto inquiryAdminListShowRequestDto, Integer page) {

        Page<Inquiry> inquiryList = inquiryRepository.adminInquirySearchWithBooleanBuilder(inquiryAdminListShowRequestDto, PageRequest.of(page, 10));

        List<InquiryAdminListOneShowResponseDto> inquiryDtoList = inquiryList.stream()
                .map(this::inquiryToInquiryAdminOneResponseDto).collect(Collectors.toList());

        return PageResponse.<InquiryAdminListOneShowResponseDto>builder()
                .currentPage(page)
                .pageSize(pageSize)
                .totalElement(inquiryList.getTotalElements())
                .totalPages(inquiryList.getTotalPages())
                .content(inquiryDtoList)
                .build();
    }

    @Override
    public InquiryAdminListOneShowResponseDto inquiryToInquiryAdminOneResponseDto(Inquiry inquiry) {

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (!Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.NOT_USING_STATE.getCode())) {
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
                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    @Override
    public InquiryAnswerAdminShowResponseDto inquiryIdToInquiryAnswerAdminShowResponseDto (InquiryDetailAdminShowDto inquiryDetailAdminShowDto) {

        Long inquiryId = inquiryDetailAdminShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailAdminShowDto.getClosed();
        String searchType = inquiryDetailAdminShowDto.getSearchType();
        String searchText = inquiryDetailAdminShowDto.getSearchText();

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

        Optional<Inquiry> previousInquiryOptional = inquiryRepository.adminFindPreviousInquiry(inquiryDetailAdminShowDto);
        Long previousInquiryId;
        String previousInquiryTitle;
        LocalDateTime previousInquiryWriteDate;
        if (previousInquiryOptional.isEmpty()) {
            previousInquiryId = null;
            previousInquiryTitle = null;
            previousInquiryWriteDate = null;
        } else {
            previousInquiryId = previousInquiryOptional.orElse(null).getId();
            previousInquiryTitle = previousInquiryOptional.orElse(null).getInquiryTitle();
            previousInquiryWriteDate = previousInquiryOptional.orElse(null).getInquiryRegistrationDate();
        }

        Optional<Inquiry> nextInquiryOptional = inquiryRepository.adminFindNextInquiry(inquiryDetailAdminShowDto);
        Long nextInquiryId;
        String nextInquiryTitle;
        LocalDateTime nextInquiryWriteDate;
        if (nextInquiryOptional.isEmpty()) {
            nextInquiryId = null;
            nextInquiryTitle = null;
            nextInquiryWriteDate = null;
        } else {
            nextInquiryId = nextInquiryOptional.orElse(null).getId();
            nextInquiryTitle = nextInquiryOptional.orElse(null).getInquiryTitle();
            nextInquiryWriteDate = nextInquiryOptional.orElse(null).getInquiryRegistrationDate();
        }

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
            InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryId(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY_ANSWER.getMessage()));
            inquiryAnswerId = inquiryAnswer.getId();
            answerTitle = inquiryAnswer.getAnswerTitle();
            answerRegistrationDate = inquiryAnswer.getAnswerRegistrationDate();
            answerContent = inquiryAnswer.getAnswerContent();
        }

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (!Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.NOT_USING_STATE.getCode())) {
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

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryAnswerAdminShowResponseDto.builder()
                .closed(String.valueOf(closed))
                .searchType(searchType)
                .searchText(searchText)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerId)

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)

                .inquiryContent(inquiry.getInquiryContent())

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .answerTitle(answerTitle)
                .answerRegistrationDate(answerRegistrationDate)
                .answerContent(answerContent)

                .previousId(previousInquiryId)
                .previousTitle(previousInquiryTitle)
                .previousWriteDate(previousInquiryWriteDate)
                .nextId(nextInquiryId)
                .nextTitle(nextInquiryTitle)
                .nextWriteDate(nextInquiryWriteDate)
                .build();
    }

    @Override
    public InquiryListOneShowResponseDto InquiryToInquiryInquirerOneResponseDto(Inquiry inquiry) {

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.PUBLIC.getCode())) {
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

        return InquiryListOneShowResponseDto.builder()
                .inquiryId(inquiry.getId())
                .inquiryTitle(inquiry.getInquiryTitle())

                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)

                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    @Override
    public InquiryListOneShowResponseDto inquiryToInquirytraderOneResponseDto(Inquiry inquiry) {

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (!Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.NOT_USING_STATE.getCode())) {
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

        return InquiryListOneShowResponseDto.builder()
                .inquiryId(inquiry.getId())
                .inquiryTitle(inquiry.getInquiryTitle())

                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)

                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())
                .build();
    }

    @Override
    public InquiryAnswerInquirerShowResponseDto inquiryIdToInquiryAnswerInquirerShowResponseDto(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        Long userId = securityUtils.getUserIdInSecurityContext();

        Inquiry inquiry = inquiryRepository.findByIdAndAndIsOpenInquirer(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

        inquiryDetailTraderInquirerShowDto.setInquirerId(userId);

        Long previousInquiryId;
        String previousInquiryTitle;
        LocalDateTime previousInquiryWriteDate;
        Long nextInquiryId;
        String nextInquiryTitle;
        LocalDateTime nextInquiryWriteDate;

        if (sort.equals("registrationDate")) {
            Optional<Inquiry> previousInquiryOptional = inquiryRepository.inquirerFindPreviousInquiryRegistrationDate(inquiryDetailTraderInquirerShowDto);
            if (previousInquiryOptional.isEmpty()) {
                previousInquiryId = null;
                previousInquiryTitle = null;
                previousInquiryWriteDate = null;
            } else {
                previousInquiryId = previousInquiryOptional.orElse(null).getId();
                previousInquiryTitle = previousInquiryOptional.orElse(null).getInquiryTitle();
                previousInquiryWriteDate = previousInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }

            Optional<Inquiry> nextInquiryOptional = inquiryRepository.inquirerFindNextInquiryRegistrationDate(inquiryDetailTraderInquirerShowDto);
            if (nextInquiryOptional.isEmpty()) {
                nextInquiryId = null;
                nextInquiryTitle = null;
                nextInquiryWriteDate = null;
            } else {
                nextInquiryId = nextInquiryOptional.orElse(null).getId();
                nextInquiryTitle = nextInquiryOptional.orElse(null).getInquiryTitle();
                nextInquiryWriteDate = nextInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }
        } else if (sort.equals("strategyName")) {
            Optional<Inquiry> previousInquiryOptional = inquiryRepository.inquirerFindPreviousInquiryStrategyName(inquiryDetailTraderInquirerShowDto);
            if (previousInquiryOptional.isEmpty()) {
                previousInquiryId = null;
                previousInquiryTitle = null;
                previousInquiryWriteDate = null;
            } else {
                previousInquiryId = previousInquiryOptional.orElse(null).getId();
                previousInquiryTitle = previousInquiryOptional.orElse(null).getInquiryTitle();
                previousInquiryWriteDate = previousInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }

            Optional<Inquiry> nextInquiryOptional = inquiryRepository.inquirerFindNextInquiryStrategyName(inquiryDetailTraderInquirerShowDto);
            if (nextInquiryOptional.isEmpty()) {
                nextInquiryId = null;
                nextInquiryTitle = null;
                nextInquiryWriteDate = null;
            } else {
                nextInquiryId = nextInquiryOptional.orElse(null).getId();
                nextInquiryTitle = nextInquiryOptional.orElse(null).getInquiryTitle();
                nextInquiryWriteDate = nextInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }

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
            InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryId(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY_ANSWER.getMessage()));
            inquiryAnswerId = inquiryAnswer.getId();
            answerTitle = inquiryAnswer.getAnswerTitle();
            answerRegistrationDate = inquiryAnswer.getAnswerRegistrationDate();
            answerContent = inquiryAnswer.getAnswerContent();
        }

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.PUBLIC.getCode())) {
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

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryAnswerInquirerShowResponseDto.builder()
                .closed(closed)
                .sort(sort)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerId)

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .inquiryContent(inquiry.getInquiryContent())

                .answerTitle(answerTitle)
                .answerRegistrationDate(answerRegistrationDate)
                .answerContent(answerContent)

                .previousId(previousInquiryId)
                .previousTitle(previousInquiryTitle)
                .previousWriteDate(previousInquiryWriteDate)
                .nextId(nextInquiryId)
                .nextTitle(nextInquiryTitle)
                .nextWriteDate(nextInquiryWriteDate)
                .build();
    }

    @Override
    public InquiryAnswerTraderShowResponseDto inquiryIdToInquiryAnswerTraderShowResponseDto(InquiryDetailTraderInquirerShowDto inquiryDetailTraderInquirerShowDto) {

        Long inquiryId = inquiryDetailTraderInquirerShowDto.getInquiryId();
        InquiryStatus closed = inquiryDetailTraderInquirerShowDto.getClosed();
        String sort = inquiryDetailTraderInquirerShowDto.getSort();

        Long userId = securityUtils.getUserIdInSecurityContext();

        Inquiry inquiry = inquiryRepository.findByIdAndAndIsOpenTrader(inquiryId, userId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

        inquiryDetailTraderInquirerShowDto.setTraderId(userId);

        Long previousInquiryId;
        String previousInquiryTitle;
        LocalDateTime previousInquiryWriteDate;
        Long nextInquiryId;
        String nextInquiryTitle;
        LocalDateTime nextInquiryWriteDate;

        if (sort.equals("registrationDate")) {
            Optional<Inquiry> previousInquiryOptional = inquiryRepository.traderFindPreviousInquiryRegistrationDate(inquiryDetailTraderInquirerShowDto);
            if (previousInquiryOptional.isEmpty()) {
                previousInquiryId = null;
                previousInquiryTitle = null;
                previousInquiryWriteDate = null;
            } else {
                previousInquiryId = previousInquiryOptional.orElse(null).getId();
                previousInquiryTitle = previousInquiryOptional.orElse(null).getInquiryTitle();
                previousInquiryWriteDate = previousInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }

            Optional<Inquiry> nextInquiryOptional = inquiryRepository.traderFindNextInquiryRegistrationDate(inquiryDetailTraderInquirerShowDto);
            if (nextInquiryOptional.isEmpty()) {
                nextInquiryId = null;
                nextInquiryTitle = null;
                nextInquiryWriteDate = null;
            } else {
                nextInquiryId = nextInquiryOptional.orElse(null).getId();
                nextInquiryTitle = nextInquiryOptional.orElse(null).getInquiryTitle();
                nextInquiryWriteDate = nextInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }
        } else if (sort.equals("strategyName")) {
            Optional<Inquiry> previousInquiryOptional = inquiryRepository.traderFindPreviousInquiryStrategyName(inquiryDetailTraderInquirerShowDto);
            if (previousInquiryOptional.isEmpty()) {
                previousInquiryId = null;
                previousInquiryTitle = null;
                previousInquiryWriteDate = null;
            } else {
                previousInquiryId = previousInquiryOptional.orElse(null).getId();
                previousInquiryTitle = previousInquiryOptional.orElse(null).getInquiryTitle();
                previousInquiryWriteDate = previousInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }

            Optional<Inquiry> nextInquiryOptional = inquiryRepository.traderFindNextInquiryStrategyName(inquiryDetailTraderInquirerShowDto);
            if (nextInquiryOptional.isEmpty()) {
                nextInquiryId = null;
                nextInquiryTitle = null;
                nextInquiryWriteDate = null;
            } else {
                nextInquiryId = nextInquiryOptional.orElse(null).getId();
                nextInquiryTitle = nextInquiryOptional.orElse(null).getInquiryTitle();
                nextInquiryWriteDate = nextInquiryOptional.orElse(null).getInquiryRegistrationDate();
            }
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }

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
            InquiryAnswer inquiryAnswer = inquiryAnswerRepository.findByInquiryId(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY_ANSWER.getMessage()));
            inquiryAnswerId = inquiryAnswer.getId();
            answerTitle = inquiryAnswer.getAnswerTitle();
            answerRegistrationDate = inquiryAnswer.getAnswerRegistrationDate();
            answerContent = inquiryAnswer.getAnswerContent();
        }

        Long methodId;
        String methodIconPath;
        Character cycle;
        StockListDto stockList;
        Long strategyId;
        String strategyName;
        String statusCode;

        if (!(Objects.equals(inquiry.getStrategy().getStatusCode(), StrategyStatusCode.NOT_USING_STATE.getCode()))) {
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

        Member trader = memberRepository.findById(inquiry.getStrategy().getTrader().getId()).orElse(null);
        String traderNickname;
        if (trader == null) {
            traderNickname = null;
        } else {
            traderNickname = trader.getNickname();
        }
        String traderProfileImagePath = fileService.getFilePath(new FileRequest(FileReferenceType.MEMBER, inquiry.getStrategy().getTrader().getId()));

        return InquiryAnswerTraderShowResponseDto.builder()
                .closed(closed)
                .sort(sort)

                .inquiryId(inquiryId)
                .inquiryAnswerId(inquiryAnswerId)

                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryRegistrationDate(inquiry.getInquiryRegistrationDate())
                .inquirerNickname(inquiry.getInquirer().getNickname())
                .inquiryStatus(inquiry.getInquiryStatus())

                .methodId(methodId)
                .methodIconPath(methodIconPath)
                .cycle(cycle)
                .stockList(stockList)
                .strategyId(strategyId)
                .strategyName(strategyName)
                .statusCode(statusCode)

                .traderId(inquiry.getStrategy().getTrader().getId())
                .traderNickname(traderNickname)
                .traderProfileImagePath(traderProfileImagePath)

                .inquiryContent(inquiry.getInquiryContent())

                .answerTitle(answerTitle)
                .answerRegistrationDate(answerRegistrationDate)
                .answerContent(answerContent)

                .previousId(previousInquiryId)
                .previousTitle(previousInquiryTitle)
                .previousWriteDate(previousInquiryWriteDate)
                .nextId(nextInquiryId)
                .nextTitle(nextInquiryTitle)
                .nextWriteDate(nextInquiryWriteDate)
                .build();
    }

    // 문의자 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    @Override
    public PageResponse<InquiryListOneShowResponseDto> showInquirerInquiry(Integer page, String sort, InquiryStatus inquiryStatus) {

        Long userId = securityUtils.getUserIdInSecurityContext();

        InquiryListShowRequestDto inquiryListShowRequestDto = new InquiryListShowRequestDto();
        inquiryListShowRequestDto.setInquirerId(userId);
        inquiryListShowRequestDto.setClosed(inquiryStatus);

        List<InquiryListOneShowResponseDto> inquiryDtoList;
        PageResponse<InquiryListOneShowResponseDto> inquiryPage;

        if (sort.equals("registrationDate")) {

            Page<Inquiry> inquiryList = inquiryRepository.pageInquirySearchWithBooleanBuilder(inquiryListShowRequestDto, PageRequest.of(page, 10));

            inquiryDtoList = inquiryList.stream()
                    .map(this::InquiryToInquiryInquirerOneResponseDto).collect(Collectors.toList());

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(page)
                    .pageSize(pageSize)
                    .totalElement(inquiryList.getTotalElements())
                    .totalPages(inquiryList.getTotalPages())
                    .content(inquiryDtoList)
                    .build();

        } else if (sort.equals("strategyName")) {

            List<Inquiry> inquiryList = inquiryRepository.listInquirerInquirySearchWithBooleanBuilder(inquiryListShowRequestDto);
            int totalCountInquiry = inquiryList.size(); // 전체 데이터 수

            int totalPageCount; // 전체 페이지 수
            int pageStart = page * pageSize; // 페이지 시작 위치
            int pageEnd;

            if (totalCountInquiry == 0) {
                totalPageCount = 0;
                inquiryDtoList = null;
            } else {
                if (totalCountInquiry % pageSize == 0) {
                    totalPageCount = (int) (totalCountInquiry / (double) pageSize);
                } else {
                    totalPageCount = (int) (totalCountInquiry / (double) pageSize) + 1;
                }

                if (page + 1 != totalPageCount) {
                    pageEnd = (page + 1) * pageSize - 1;
                } else {
                    pageEnd = totalCountInquiry - 1;
                }

                List<Inquiry> inquiryListCut = new ArrayList<>();
                for (int i = pageStart; i <= pageEnd; i++) {
                    System.out.println("i: " + i);
                    inquiryListCut.add(inquiryList.get(i));
                }

                inquiryDtoList = inquiryListCut.stream()
                        .map(this::InquiryToInquiryInquirerOneResponseDto).collect(Collectors.toList());
            }

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(page) // 현재 페이지
                    .pageSize(pageSize) // 한 페이지 크기
                    .totalElement(totalCountInquiry) // 전체 데이터 수
                    .totalPages(totalPageCount) // 전체 페이지 수
                    .content(inquiryDtoList)
                    .build();
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }

        return inquiryPage;
    }

    // 트레이더 검색 조회
    // 정렬 순 셀렉트 박스 (최신순, 전략명)
    // 답변상태 셀렉트 박스 (전체, 답변 대기, 답변 완료)
    @Override
    public PageResponse<InquiryListOneShowResponseDto> showTraderInquiry(Integer page, String sort, InquiryStatus inquiryStatus) {

        Long userId = securityUtils.getUserIdInSecurityContext();

        InquiryListShowRequestDto inquiryListShowRequestDto = new InquiryListShowRequestDto();
        inquiryListShowRequestDto.setTraderId(userId);
        inquiryListShowRequestDto.setClosed(inquiryStatus);

        List<InquiryListOneShowResponseDto> inquiryDtoList;
        PageResponse<InquiryListOneShowResponseDto> inquiryPage;

        if (sort.equals("registrationDate")) {

            Page<Inquiry> inquiryList = inquiryRepository.pageInquirySearchWithBooleanBuilder(inquiryListShowRequestDto, PageRequest.of(page, 10));

            inquiryDtoList = inquiryList.stream()
                    .map(this::inquiryToInquirytraderOneResponseDto).collect(Collectors.toList());

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(page)
                    .pageSize(pageSize)
                    .totalElement(inquiryList.getTotalElements())
                    .totalPages(inquiryList.getTotalPages())
                    .content(inquiryDtoList)
                    .build();

        } else if (sort.equals("strategyName")) {

            List<Inquiry> inquiryList = inquiryRepository.listTraderInquirySearchWithBooleanBuilder(inquiryListShowRequestDto);
            int totalCountInquiry = inquiryList.size(); // 전체 데이터 수

            int totalPageCount; // 전체 페이지 수
            int pageStart = page * pageSize; // 페이지 시작 위치
            int pageEnd;

            if (totalCountInquiry == 0) {
                totalPageCount = 0;
                inquiryDtoList = null;
            } else {
                if (totalCountInquiry % pageSize == 0) {
                    totalPageCount = (int) (totalCountInquiry / (double) pageSize);
                } else {
                    totalPageCount = (int) (totalCountInquiry / (double) pageSize) + 1;
                }

                if (page + 1 != totalPageCount) {
                    pageEnd = (page + 1) * pageSize - 1;
                } else {
                    pageEnd = totalCountInquiry - 1;
                }

                List<Inquiry> inquiryListCut = new ArrayList<>();
                for (int i = pageStart; i <= pageEnd; i++) {
                    System.out.println("i: " + i);
                    inquiryListCut.add(inquiryList.get(i));
                }

                inquiryDtoList = inquiryListCut.stream()
                        .map(this::inquiryToInquirytraderOneResponseDto).collect(Collectors.toList());
            }

            inquiryPage = PageResponse.<InquiryListOneShowResponseDto>builder()
                    .currentPage(page) // 현재 페이지
                    .pageSize(pageSize) // 한 페이지 크기
                    .totalElement(totalCountInquiry) // 전체 데이터 수
                    .totalPages(totalPageCount) // 전체 페이지 수
                    .content(inquiryDtoList)
                    .build();
        } else {
            throw new IllegalArgumentException("정렬순을 지정하세요");
        }

        return inquiryPage;
    }

    @Override
    public InquiryModifyPageShowResponseDto showInquiryModifyPage(Long inquiryId) {

        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow(() -> new EntityNotFoundException(InquiryFailMessage.NOT_FOUND_INQUIRY.getMessage()));

        if(!Objects.equals(securityUtils.getUserIdInSecurityContext(), inquiry.getInquirer().getId())) {
            throw new InquiryNotWriterException(InquiryFailMessage.NOT_INQUIRY_WRITER.getMessage());
        }

        return InquiryModifyPageShowResponseDto.builder()
                .inquiryTitle(inquiry.getInquiryTitle())
                .inquiryContent(inquiry.getInquiryContent())
                .build();
    }
}
