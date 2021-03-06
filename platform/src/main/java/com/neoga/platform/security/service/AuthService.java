package com.neoga.platform.security.service;

import com.neoga.platform.exception.custom.CEmailLoginFailedException;
import com.neoga.platform.exception.custom.CMemberNotFoundException;
import com.neoga.platform.memberstore.member.domain.Members;
import com.neoga.platform.memberstore.member.repository.MemberRepository;
import com.neoga.platform.security.dto.KakaoProfile;
import com.neoga.platform.security.dto.LoginRequestDto;
import com.neoga.platform.security.dto.LoginInfo;
import com.neoga.platform.security.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final KakaoService kakaoService;

    //로그인 정보를 받아 검증 후 jwt 토큰 생성 후 로그인 정보 반환
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        String uid = loginRequest.getUid();
        String passwd = loginRequest.getPasswd();
        Members findMember = memberRepository.findByUid(uid)
                .orElseThrow(CEmailLoginFailedException::new);

        if (!passwordEncoder.matches(passwd, findMember.getPasswd()))
            throw new CEmailLoginFailedException();

        String accessToken = jwtTokenService.createToken(findMember);

        return loginResponseBuilder(accessToken, findMember);
    }

    //소셜 accessToken이용하여 jwt 토큰 생성 후 로그인 정보 반환
    public LoginResponseDto socialLogin(String socialAccessToken, String provider) {
        KakaoProfile profile = kakaoService.getKakaoProfile(socialAccessToken);
        Members findMember = memberRepository.findByUidAndProvider(String.valueOf(profile.getId()), provider)
                .orElseThrow(() -> new CMemberNotFoundException("member not found : you need signup"));

        String accessToken = jwtTokenService.createToken(findMember);

        return loginResponseBuilder(accessToken, findMember);
    }

    // 저장된 인증정보에서 현재 로그인 사용자정보 조회
    public LoginInfo getLoginInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> detail = ((Map<String, Object>) authentication.getDetails());
        return LoginInfo.builder()
                .memberId(Long.valueOf(String.valueOf(detail.get("id"))))
                .uid(String.valueOf(detail.get("uid")))
                .name(String.valueOf(detail.get("name")))
                .role((List) detail.get("authorities")).build();
    }


    public LoginResponseDto loginResponseBuilder(String accessToken, Members findMember) {
        return LoginResponseDto.builder()
                .memberId((findMember.getId()))
                .uid(findMember.getUid())
                .name(findMember.getName())
                .tokenType("Bearer")
                .accessToken(accessToken).build();
    }
}
