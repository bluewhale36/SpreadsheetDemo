package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/herb/log")
public class HerbLogController {

    private final HerbService herbService;

    @GetMapping("/list")
    public String herbLogList(
            Model model,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        model.addAttribute("herbLogViewDTOList", herbService.getHerbLogs(from, to));
        model.addAttribute("from", from);   // 검색 일자 유지 목적
        model.addAttribute("to", to);       // 검색 일자 유지 목적
        return "herb/log";
    }

    @GetMapping("/statistics")
    public String statistics(
            Model model,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        model.addAttribute("herbStatisticsModel", herbService.getHerbLogStatistics(from, to));
        model.addAttribute("from", from);   // 검색 일자 유지 목적
        model.addAttribute("to", to);       // 검색 일자 유지 목적
        return "herb/statistics";
    }
}
