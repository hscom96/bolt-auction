package com.neoga.boltauction.security.controller;


import com.neoga.boltauction.memberstore.member.controller.MemberController;
import com.neoga.boltauction.security.dto.LoginUserDto;
import com.neoga.boltauction.security.dto.RetKakaoAuth;
import com.neoga.boltauction.security.service.KakaoService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/social")
public class KakaoContoller {
    private final KakaoService kakaoService;
    @Value("${kakao.loginURL}")
    private String kakaoLoginURL;
    @Value("${base.URL}")
    private String baseUrl;
    @Value("${kakao.clientId}")
    private String kakaoClientId;
    @Value("${kakao.redirectURI}")
    private String kakaoRedirectURI;

    @ApiOperation(value = "카카오 로그인페이지 주소", notes = "카카오 로그인 페이지 주소 반환합니다. 이 주소를 이용하여 요청하면 토큰 발행")
    @GetMapping("/kakao/login")
    public String socialLogin() {
        StringBuilder loginUrl = new StringBuilder()
                .append(kakaoLoginURL)
                .append("?client_id=").append(kakaoClientId)
                .append("&response_type=code")
                .append("&redirect_uri=").append(baseUrl).append(kakaoRedirectURI);
        return loginUrl.toString();
    }

    @ApiOperation(value = "카카오 리다이렉션", notes = "프론트분들 신경안쓰셔도 됩니다.(카카오 로그인 성공시 리다이렉션)")
    @GetMapping(value = "/kakao")
    public ResponseEntity redirectKakao(@RequestParam String code) {
        RetKakaoAuth token = kakaoService.getKakaoToken(code);

        EntityModel<LoginUserDto> entityModel = new EntityModel(token);
        entityModel.add(linkTo(methodOn(KakaoContoller.class).redirectKakao(code)).withSelfRel());
        entityModel.add(new Link("/swagger-ui.html#/auth%20API/signinByProviderUsingPOST").withRel("profile"));
        entityModel.add(linkTo(MemberController.class).slash("/social").withRel("socialSignup"));
        entityModel.add(linkTo(AuthController.class).slash("/login/social").withRel("socialLogin"));

        return ResponseEntity.ok().body(entityModel);
    }
}