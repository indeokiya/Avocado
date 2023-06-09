package com.avocado.community.api.controller;

import com.avocado.community.api.service.StyleshotService;
import com.avocado.community.common.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequiredArgsConstructor
public class MyStyleshotController {

    private final JwtUtils jwtUtils;
    private final StyleshotService styleshotService;

    @GetMapping("my-styleshots")
    public ResponseEntity<?> getMyStyleshotList(HttpServletRequest request) {
        Claims claims = jwtUtils.getClaims(request);
        return ResponseEntity.ok(styleshotService.myStyleshotList(claims));
    }

    @GetMapping("my-statistics")
    public ResponseEntity<?> getCounts(HttpServletRequest request) {
        Claims claims = jwtUtils.getClaims(request);
        return ResponseEntity.ok(styleshotService.getCounts(claims));
    }
}
