package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.domain.HerbLogPagination;
import com.example.spreadsheetdemo.herb.dto.HerbLogViewDTO;
import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/herb/log")
public class HerbLogRestController {

    private final HerbService herbService;

//    @GetMapping("/{endRowNum}")
//    public ResponseEntity<HerbLogPagination> herbLog(@PathVariable int endRowNum) {
//        HerbLogPagination pagination = herbService.getHerbLogs(endRowNum);
//        System.out.println(pagination.getData().toString());
//        return ResponseEntity.ok(pagination);
//    }
}
