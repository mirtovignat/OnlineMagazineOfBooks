package com.example.demo.controller.catalog;

import com.example.demo.dto.joined_to_user.LibrarianMovieForOwnerViewDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
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
            @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO
    ) {
        List<LibrarianMovieForOwnerViewDTO> items = libraryService.getCatalog(userForOwnerViewDTO.username());
        model.addAttribute("items", items);
        return "catalog/library";
    }
}