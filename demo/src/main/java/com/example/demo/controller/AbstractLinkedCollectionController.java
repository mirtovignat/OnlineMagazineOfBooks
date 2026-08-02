package com.example.demo.controller;

import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.AbstractLinkedCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public abstract class AbstractLinkedCollectionController<DTO> {

    @Autowired
    protected final AbstractLinkedCollectionService<?, DTO> linkedCollectionService;

    protected AbstractLinkedCollectionController(
            AbstractLinkedCollectionService<?, DTO> linkedCollectionService
    ) {
        this.linkedCollectionService = linkedCollectionService;
    }

    @GetMapping("/count")
    @ResponseBody
    public int getCount(@SessionAttribute(required = false)
                        UserForOwnerViewDTO userForOwnerViewDTO) {
        return userForOwnerViewDTO == null ? 0 : linkedCollectionService.getCount(userForOwnerViewDTO.username());
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, Object> add(@PathVariable Long id,
                                   @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        linkedCollectionService.add(id, userForOwnerViewDTO.username());
        int count = linkedCollectionService.getCount(userForOwnerViewDTO.username());

        return Map.of(
                "count", count,
                "message", SuccessCode.ADDED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public Map<String, Object> remove(@PathVariable Long id,
                                      @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        linkedCollectionService.remove(id, userForOwnerViewDTO.username());
        int count = linkedCollectionService.getCount(userForOwnerViewDTO.username());

        return Map.of(
                "count", count,
                "message", SuccessCode.REMOVED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clear(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO) {
        linkedCollectionService.removeAll(userForOwnerViewDTO.username());
        return Map.of(
                "count", 0,
                "message", SuccessCode.CLEARED_SUCCESSFULLY.format()
        );
    }
}