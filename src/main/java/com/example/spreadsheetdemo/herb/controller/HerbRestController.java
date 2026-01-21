package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Slf4j
@RestController
@RequestMapping("/api/google")
@RequiredArgsConstructor
public class HerbRestController {

    private final HerbService herbService;

    @GetMapping({"", "/"})
    public String google() throws IOException, GeneralSecurityException {
        return herbService.getAllHerbs().toString();
    }
}
