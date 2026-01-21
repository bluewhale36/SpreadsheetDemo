package com.example.spreadsheetdemo.herb.controller;

import com.example.spreadsheetdemo.common.exception.GoogleSpreadsheetsAPIException;
import com.example.spreadsheetdemo.common.exception.RollbackFailedException;
import com.example.spreadsheetdemo.herb.dto.HerbDTO;
import com.example.spreadsheetdemo.herb.dto.HerbLogViewDTO;
import com.example.spreadsheetdemo.herb.dto.HerbRegisterDTO;
import com.example.spreadsheetdemo.herb.dto.HerbUpdateDTO;
import com.example.spreadsheetdemo.herb.service.HerbService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/herb")
public class HerbController {

    private final HerbService herbService;

    @GetMapping("")
    public String herb(Model model, @RequestParam(required = false) String keyword) {

        List<HerbDTO> allHerbList = herbService.getAllHerbs();

        // 2. 검색어 필터링
        List<HerbDTO> filteredHerbs = allHerbList;
        if (keyword != null && !keyword.isBlank()) {
            filteredHerbs = allHerbList.stream()
                    .filter(h -> h.getName().contains(keyword))
                    .collect(Collectors.toList());
        }

        model.addAttribute("herbList", filteredHerbs);
        // 검색어 유지
        model.addAttribute("keyword", keyword);
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

    @GetMapping("/log")
    public String herbLog(Model model) {
        List<HerbLogViewDTO> herbLogViewDTOList = herbService.getAllHerbLogs();
        herbLogViewDTOList.sort(
                (l1, l2) -> l2.getLoggedDate().compareTo(l1.getLoggedDate())
        );
        model.addAttribute("herbLogList", herbLogViewDTOList);
        return "herb/log";
    }
}
