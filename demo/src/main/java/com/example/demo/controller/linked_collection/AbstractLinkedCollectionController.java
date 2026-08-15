package com.example.demo.controller.linked_collection;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.user.UserForOwnerViewDTO;
import com.example.demo.exception.SuccessCode;
import com.example.demo.service.linked_collection.AbstractLinkedCollectionService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@AllArgsConstructor
public abstract class AbstractLinkedCollectionController<DTO> {

    protected final AbstractLinkedCollectionService<?, DTO> linkedCollectionService;

    @GetMapping("/count")
    @ResponseBody
    public Long getCount(@SessionAttribute(required = false) UserForOwnerViewDTO userForOwnerViewDTO) {
        return userForOwnerViewDTO == null ? 0 : linkedCollectionService.getCount(userForOwnerViewDTO.username());
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, Object> add(@PathVariable Long id,
                                   @SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                   HttpSession httpSession) {
        linkedCollectionService.add(id, userForOwnerViewDTO.username());
        Long newCount = linkedCollectionService.getCount(userForOwnerViewDTO.username());
        updateBadgeInSession(httpSession, newCount);
        return Map.of(
                "count", newCount,
                "message", SuccessCode.ADDED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public Map<String, Object> remove(@PathVariable Long id,
                                      @SessionAttribute
                                      UserForOwnerViewDTO userForOwnerViewDTO,
                                      HttpSession httpSession) {
        linkedCollectionService.remove(id, userForOwnerViewDTO.username());
        Long newCount = linkedCollectionService.getCount(userForOwnerViewDTO.username());
        updateBadgeInSession(httpSession, newCount);
        return Map.of(
                "count", newCount,
                "message", SuccessCode.REMOVED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clear(@SessionAttribute UserForOwnerViewDTO userForOwnerViewDTO,
                                     HttpSession httpSession) {
        linkedCollectionService.removeAll(userForOwnerViewDTO.username());
        updateBadgeInSession(httpSession, 0L);
        return Map.of(
                "count", 0,
                "message", SuccessCode.CLEARED_SUCCESSFULLY.format()
        );
    }

    private void updateBadgeInSession(HttpSession httpSession, Long newCount) {
        BadgeCountsDTO current = (BadgeCountsDTO) httpSession.getAttribute("badges");
        if (current == null) {
            current = BadgeCountsDTO.empty();
        }
        BadgeCountsDTO updated = linkedCollectionService.updateBadge(current, newCount);
        httpSession.setAttribute("badges", updated);
    }
}