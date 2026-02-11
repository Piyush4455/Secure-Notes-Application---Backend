package com.secure.notes.controllers;

import com.secure.notes.dtos.AiConvertRequest;
import com.secure.notes.services.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    // POST /api/notes/convert-hinglish
    @PostMapping("/convert-hinglish")
    public Map<String, String> convertToHinglish(@RequestBody AiConvertRequest request) {
        // FIX: was using Map<String, String> and calling request.get("content"),
        // but the frontend sends { "text": "..." }.
        // Switched to the AiConvertRequest DTO you already have — it has getText()
        // which maps correctly to the "text" field.
        String convertedText = aiService.convertToHinglish(request.getText());
        return Map.of("convertedText", convertedText);
    }
}