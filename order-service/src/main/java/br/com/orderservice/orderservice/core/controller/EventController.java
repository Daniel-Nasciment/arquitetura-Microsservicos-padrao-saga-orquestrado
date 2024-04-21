package br.com.orderservice.orderservice.core.controller;

import br.com.orderservice.orderservice.core.dto.EventFilters;
import br.com.orderservice.orderservice.core.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<?> findByFilters(@RequestBody EventFilters filters){
        return ResponseEntity.ok(eventService.findByFilters(filters));
    }

    @GetMapping("/all")
    public ResponseEntity<?> findAll(){
        return ResponseEntity.ok(eventService.findAll());
    }

}
