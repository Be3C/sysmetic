package com.be3c.sysmetic.domain.member.exception;

public enum MemberExceptionMessage {
    // 회원가입 관련
    ALREADY_USE_EMAIL("이미 사용 중인 이메일입니다. 다른 이메일을 입력해 주세요."),
    INVALID_EMAIL_CODE("인증코드가 일치하지 않습니다. 올바른 인증코드로 입력해 주세요."),
    UNVERIFIED_EMAIL_ERROR("이메일 인증이 완료되지 않았습니다. 인증 후 다시 시도해 주세요."),
    REGISTRATION_FAILED("회원가입에 실패했습니다. 관리자에게 문의해 주세요."),
    NICKNAME_ALREADY_IN_USE("이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해 주세요."),

    // 로그인 관련
    INVALID_CREDENTIALS("입력하신 이메일 또는 비밀번호가 올바르지 않습니다. 다시 확인해주세요."),
    MEMBER_NOT_FOUND("해당 이메일로 등록된 회원 정보를 찾을 수 없습니다."),
    PASSWORD_MISMATCH("비밀번호가 일치하지 않습니다."),
    FAIL_PASSWORD_CHANGE("비밀번호 변경에 실패했습니다. 관리자에게 문의해 주세요."),

    // 회원 관리 페이지 관련
    NOT_FOUND_MEMBER("일치하는 회원 정보를 찾을 수 없습니다."),
    FAIL_ROLE_CHANGE("회원의 등급변경에 실패했습니다."),
    INVALID_PAGE("유효하지 않은 페이지 값입니다."),

    // Redis 관련 오류
    ERROR_REDIS_EMAIL_CODE("Redis 이메일 인증코드 관련 오류 발생"),

    // 이메일 관련 오류
    ERROR_EMAIL("이메일 관련 오류 발생"),

    // 멤버 조회할 때 존재하지 않을 때
    DATA_NOT_FOUND("존재하지 않는 회원입니다.")

    ;

    private final String message;

    MemberExceptionMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

}