package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.domain.HerbLogPagination;
import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/herb/log")
public class HerbLogRestController {

    private final HerbService herbService;

    @GetMapping("/{date}")
    public ResponseEntity<HerbLogPagination> getLogs(
            @PathVariable(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(herbService.getHerbLogs(date));
    }
}
