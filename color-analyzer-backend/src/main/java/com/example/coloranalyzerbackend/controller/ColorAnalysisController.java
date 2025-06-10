package com.example.coloranalyzerbackend.controller;

import com.example.coloranalyzerbackend.service.ColorExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin; // Added import

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200") // Added annotation - Allow Angular dev server
@RestController
@RequestMapping("/api") // Base path for this controller
public class ColorAnalysisController {

    private final ColorExtractionService colorExtractionService;

    @Autowired
    public ColorAnalysisController(ColorExtractionService colorExtractionService) {
        this.colorExtractionService = colorExtractionService;
    }

    @GetMapping("/extract-colors")
    public ResponseEntity<?> extractColors(@RequestParam String url, @RequestParam(required = false, defaultValue = "10") int topN) {
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("URL parameter is required.");
        }
        // Basic URL validation (does it look like a URL?)
        if (!url.matches("^https?://.*")) {
            url = "http://" + url; // Attempt to prefix with http if missing
        }

        try {
            Map<String, Integer> colorFrequencies = colorExtractionService.extractColorsFromUrl(url);
            if (colorFrequencies.isEmpty()) {
                // This could mean the URL was invalid, inaccessible, or no colors were found.
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No colors extracted. Check URL or website content.");
            }

            if (topN > 0) {
                List<Map<String, Integer>> topColors = colorExtractionService.getTopNColors(colorFrequencies, topN);
                 return ResponseEntity.ok(topColors);
            } else {
                // Return all color frequencies
                return ResponseEntity.ok(colorFrequencies);
            }

        } catch (Exception e) {
            System.err.println("Error in ColorAnalysisController for URL: " + url + " - " + e.getMessage());
            e.printStackTrace(); // For server logs
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing URL: " + e.getMessage());
        }
    }
}
