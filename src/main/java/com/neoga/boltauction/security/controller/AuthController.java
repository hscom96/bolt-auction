package com.neoga.boltauction.security.controller;

import com.neoga.boltauction.exception.custom.CMemberNotFoundException;
import com.neoga.boltauction.memberstore.member.controller.MemberController;
import com.neoga.boltauction.security.dto.LoginRequestDto;
import com.neoga.boltauction.security.dto.LoginResponseDto;
import com.neoga.boltauction.security.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;


@RequiredArgsConstructor
@Api(tags = {"auth API"})
@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {
    private final AuthService authService;

    @ApiOperation(value = "일반 로그인", notes = "로그인을 하며 jwt 토큰 발행 \n" +
            "403(Forbidden) - 아이디 혹은 비밀번호 오류")
    @PostMapping(value = "/login")
    public ResponseEntity login(@RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto loginResponse = authService.login(loginRequest);

        Resource<LoginResponseDto> entityModel = new Resource(loginResponse);
        entityModel.add(linkTo(methodOn(AuthController.class).login(loginRequest)).withSelfRel());
        entityModel.add(new Link("/swagger-ui.html#/auth%20API/loginUsingPOST").withRel("profile"));

        return ResponseEntity.ok().body(entityModel);
    }

    @ApiOperation(value = "소셜 로그인", notes = "소셜 회원 로그인을 한다. \n" +
            "401(Unauthorized) - 소셜 회원가입 안되어있음 (회원가입 리다이렉션 주소 반환)")
    @PostMapping(value = "/login/social/{provider}")
    public ResponseEntity loginByProvider(
            @ApiParam(value = "서비스 제공자 provider", required = true, defaultValue = "kakao") @PathVariable String provider,
            @ApiParam(value = "소셜 access_token", required = true) @RequestParam String accessToken) {
        LoginResponseDto loginResponse;
        Resource entityModel = null;

        try {
            loginResponse = authService.socialLogin(accessToken, provider);
        }catch(CMemberNotFoundException e){
            entityModel = new Resource(e.getMessage());
            entityModel.add(linkTo(MemberController.class).slash("/social").withRel("socialSignup"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(entityModel);
        }

        entityModel = new Resource(loginResponse);
        entityModel.add(linkTo(methodOn(AuthController.class).loginByProvider(provider, accessToken)).withSelfRel());
        entityModel.add(new Link("/swagger-ui.html#/kakao-contoller/socialLoginUsingGET").withRel("profile"));

        return ResponseEntity.ok().body(entityModel);
    }

}
