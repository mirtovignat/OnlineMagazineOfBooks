package com.example.demo.controller.catalog;

import com.example.demo.dto.joined_to_user.HistoricalMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.service.catalog.HistoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public String getHistory(
            Model model,
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO
    ) {
        List<HistoricalMovieForOwnerViewDTO> items =
                historyService.getCatalog(userForOwnerViewDTO.username());
        model.addAttribute("items", items);
        return "catalog/history";
    }
}