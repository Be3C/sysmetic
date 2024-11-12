package com.be3c.sysmetic.domain.member.service;

import com.be3c.sysmetic.domain.member.dto.MemberPatchInfoRequestDto;
import com.be3c.sysmetic.domain.member.dto.MemberPutPasswordRequestDto;
import com.be3c.sysmetic.domain.member.entity.Member;
import com.be3c.sysmetic.domain.member.entity.ResetPasswordLog;
import com.be3c.sysmetic.domain.member.repository.MemberRepository;
import com.be3c.sysmetic.domain.member.repository.ResetPasswordLogRepository;
import com.be3c.sysmetic.global.common.Code;
import com.be3c.sysmetic.global.util.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MemberInfoServiceImpl implements MemberInfoService {

    private final SecurityUtils securityUtils;

    private final MemberRepository memberRepository;

    private final ResetPasswordLogRepository resetPasswordLogRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean changePassword(MemberPutPasswordRequestDto memberPutPasswordRequestDto, Long userId, HttpServletRequest request) {
        if(memberPutPasswordRequestDto.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Member member = findMemberById(userId);

        if(
            // 현재 비밀번호가 제대로 입력되었는지 확인
            passwordEncoder
                .matches(
                        memberPutPasswordRequestDto.getCurrentPassword(),
                        member.getPassword()
                ) &&
            // 비밀번호 확인과 비밀번호가 일치하는지 확인.
            memberPutPasswordRequestDto
                    .getNewPassword()
                    .equals(memberPutPasswordRequestDto
                            .getNewPasswordConfirm())
        ) {
            member.setPassword(passwordEncoder.encode(memberPutPasswordRequestDto.getNewPassword()));

            saveChangePasswordLog(request, member, Code.PASSWORD_CHANGE_SUCCESS.getCode());
        }
        saveChangePasswordLog(request, member, Code.PASSWORD_CHANGE_FAIL.getCode());

        return false;
    }

    @Override
    public boolean changeMemberInfo(MemberPatchInfoRequestDto memberPatchInfoRequestDto, Long userId) {
        if(memberPatchInfoRequestDto.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Member member = findMemberById(userId);

        if(memberPatchInfoRequestDto.getNicknameDuplCheck()) {
            member.setNickname(memberPatchInfoRequestDto.getNickname());
        }

        member.setPhoneNumber(memberPatchInfoRequestDto.getPhone_number());

        // 수정 필요. 너무 구리다.
        if(memberPatchInfoRequestDto.getReceiveInfoConsent()) {
            member.setReceiveInfoConsent(Code.RECEIVE_MAIL.getCode());
        } else {
            member.setReceiveInfoConsent(Code.NOT_RECEIVE_MAIL.getCode());
        }

        if(memberPatchInfoRequestDto.getReceiveMarketingConsent()) {
            member.setReceiveMarketingConsent(Code.RECEIVE_MAIL.getCode());
        } else {
            member.setReceiveMarketingConsent(Code.NOT_RECEIVE_MAIL.getCode());
        }

        memberRepository.save(member);

        return true;
    }

    @Override
    public boolean deleteUser(Long userId, HttpServletRequest request) {
        if(!securityUtils.getUserIdInSecurityContext().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Member member = findMemberById(userId);

        if(member.getRoleCode().equals(Code.ROLE_USER.getCode())) {
            deleteUser(userId);
        } else if(member.getRoleCode().equals(Code.ROLE_TRADER.getCode())) {
            deleteTrader(userId);
        }

        memberRepository.delete(member);

        return false;
    }

    private void deleteUser(Long userId) {

    }

    private void deleteTrader(Long userId) {

    }

    private void saveChangePasswordLog(HttpServletRequest request, Member member, String resultCode) {
        ResetPasswordLog passwordLog = ResetPasswordLog.builder()
                .tryIp(request.getHeader("X-FORWARDED-FOR"))
                .member(member)
                .result(resultCode)
                .build();

        resetPasswordLogRepository.save(passwordLog);
    }

    private Member findMemberById(Long userId) {
        return memberRepository
                .findByIdAndStatusCode(
                        userId,
                        Code.USING_STATE.getCode())
                .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다."));
    }
}