package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbRegisterDTO;
import com.example.spreadsheetdemo.herb.dto.HerbUpdateDTO;
import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/herb")
public class HerbController {

    private final HerbService herbService;

    @GetMapping("")
    public String herb(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("herbList", herbService.getHerbs(keyword));
        model.addAttribute("keyword", keyword);     // 검색어 유지 목적
        return "herb/inventory";
    }

    @PostMapping("")
    public String registerHerb(@RequestBody HerbRegisterDTO herbRegisterDTO) {
        herbService.insertHerb(herbRegisterDTO);
        return "herb/inventory";
    }

    @PutMapping("")
    public String updateHerb(@RequestBody List<HerbUpdateDTO> updateDTOList) {
        herbService.updateHerbs(updateDTOList);
        return "herb/inventory";
    }

    @DeleteMapping("")
    public String deleteHerb(@RequestBody HerbDTO deleteHerbDTO) {
        System.out.println(deleteHerbDTO);
        herbService.hardDeleteOneHerb(deleteHerbDTO);
        return "herb/inventory";
    }

    @GetMapping("/log")
    public String herbLog(
            Model model,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        model.addAttribute("herbLogViewDTOList", herbService.getHerbLogs(from, to));
        model.addAttribute("from", from);   // 검색 일자 유지 목적
        model.addAttribute("to", to);       // 검색 일자 유지 목적
        return "herb/log";
    }

    @GetMapping("/details")
    public String herbDetails(Model model, @RequestParam String name) {
        model.addAttribute("infoDTO", herbService.getOneHerbInfo(name));
        return "herb/details";
    }
}
