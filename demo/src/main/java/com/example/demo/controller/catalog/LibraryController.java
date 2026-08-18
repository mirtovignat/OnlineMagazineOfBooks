package com.example.demo.controller.catalog;

import com.example.demo.dto.catalog.LibrarianMovieForOwnerViewDTO;
import com.example.demo.dto.user.SessionUser;
import com.example.demo.service.catalog.LibraryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public String getLibrary(
            Model model,
            @SessionAttribute SessionUser sessionUser
    ) {
        Long userId = sessionUser.id();
        List<LibrarianMovieForOwnerViewDTO> items = libraryService.getCatalog(userId);
        model.addAttribute("items", items);
        return "catalog/library";
    }
}