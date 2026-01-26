package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/herb")
public class HerbRestController {

    private final HerbService herbService;

    @GetMapping("/all/name")
    public ResponseEntity<Set<String>> getAllHerbNames() {
        return ResponseEntity.ok(herbService.getAllHerbName());
    }
}
