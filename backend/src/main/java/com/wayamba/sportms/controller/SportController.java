package com.wayamba.sportms.controller;

import com.wayamba.sportms.dto.SportResponse;
import com.wayamba.sportms.dto.SportSlotsResponse;
import com.wayamba.sportms.service.SlotService;
import com.wayamba.sportms.service.SportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sports")
public class SportController {

    private final SportService sportService;
    private final SlotService slotService;

    public SportController(SportService sportService, SlotService slotService) {
        this.sportService = sportService;
        this.slotService = slotService;
    }

    @GetMapping
    public ResponseEntity<List<SportResponse>> getSports(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(sportService.getSports(type));
    }

    @GetMapping("/{sportId}/slots")
    public ResponseEntity<SportSlotsResponse> getSlots(
            @PathVariable Long sportId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(slotService.getSlotsForSportAndDate(sportId, date));
    }
}

