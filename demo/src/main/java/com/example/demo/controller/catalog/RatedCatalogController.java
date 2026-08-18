package com.example.demo.controller.catalog;

import com.example.demo.dto.catalog.RatedMovieForOwnerViewDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.service.catalog.RatedCatalogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/rated/history")
public class RatedCatalogController {

    private final RatedCatalogService ratedCatalogService;

    @GetMapping
    public String getRatedHistory(
            Model model,
            @SessionAttribute SessionUser sessionUser
    ) {
        Long userId = sessionUser.id();
        List<RatedMovieForOwnerViewDTO> items = ratedCatalogService.getCatalog(userId);
        model.addAttribute("items", items);
        return "catalog/rated";
    }
}