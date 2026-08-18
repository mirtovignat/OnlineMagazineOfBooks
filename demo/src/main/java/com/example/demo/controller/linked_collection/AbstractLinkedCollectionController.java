package com.example.demo.controller.linked_collection;

import com.example.demo.dto.badges.BadgeCountsDTO;
import com.example.demo.dto.user.SessionUser;
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
    public Long getCount(@SessionAttribute(required = false) SessionUser sessionUser) {
        Long userId = sessionUser.id();
        return userId == null ? 0 : linkedCollectionService.getCount(userId);
    }

    @PostMapping("/add/{id}")
    @ResponseBody
    public Map<String, Object> add(@PathVariable Long id,
                                   @SessionAttribute SessionUser sessionUser,
                                   HttpSession httpSession) {
        Long userId = sessionUser.id();
        linkedCollectionService.add(id, userId);
        Long newCount = linkedCollectionService.getCount(userId);
        updateBadgeInSession(httpSession, newCount);
        return Map.of(
                "count", newCount,
                "message", SuccessCode.ADDED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/remove/{id}")
    @ResponseBody
    public Map<String, Object> remove(@PathVariable Long id,
                                      @SessionAttribute SessionUser sessionUser,
                                      HttpSession httpSession) {
        Long userId = sessionUser.id();
        linkedCollectionService.remove(id, userId);
        Long newCount = linkedCollectionService.getCount(userId);
        updateBadgeInSession(httpSession, newCount);
        return Map.of(
                "count", newCount,
                "message", SuccessCode.REMOVED_SUCCESSFULLY.format(id)
        );
    }

    @PostMapping("/clear")
    @ResponseBody
    public Map<String, Object> clear(@SessionAttribute SessionUser sessionUser,
                                     HttpSession httpSession) {
        Long userId = sessionUser.id();
        linkedCollectionService.removeAll(userId);
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