package com.example.coloranalyzerbackend.controller;

import com.example.coloranalyzerbackend.service.ColorExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ColorAnalysisController.class)
public class ColorAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ColorExtractionService colorExtractionService;

    @Test
    void testExtractColors_Success_ReturnsTopNColors() throws Exception {
        Map<String, Integer> frequencies = new HashMap<>();
        frequencies.put("#FF0000", 10); // Standardized to uppercase hex
        frequencies.put("#0000FF", 5); // Standardized to uppercase hex for "blue"

        // Prepare the list of single-entry maps as expected from the service
        List<Map<String, Integer>> topColors = Arrays.asList(
                Map.of("#FF0000", 10),
                Map.of("#0000FF", 5)
        );

        when(colorExtractionService.extractColorsFromUrl(anyString())).thenReturn(frequencies);
        // Ensure the mock for getTopNColors matches the actual return type
        when(colorExtractionService.getTopNColors(frequencies, 2)).thenReturn(topColors);

        mockMvc.perform(get("/api/extract-colors")
                .param("url", "http://example.com")
                .param("topN", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]['#FF0000']").value(10)) // Adjusted JSONPath for actual output
                .andExpect(jsonPath("$[1]['#0000FF']").value(5));  // Adjusted JSONPath for actual output
    }

    @Test
    void testExtractColors_Success_ReturnsAllColors() throws Exception {
        Map<String, Integer> frequencies = new HashMap<>();
        frequencies.put("#FF0000", 10); // Standardized
        frequencies.put("#0000FF", 5);  // Standardized

        when(colorExtractionService.extractColorsFromUrl(anyString())).thenReturn(frequencies);

        // Test without topN param, or topN <= 0 to get all frequencies
        mockMvc.perform(get("/api/extract-colors")
                .param("url", "http://example.com")
                .param("topN", "0")) // This path returns the raw map
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.#FF0000").value(10))
                .andExpect(jsonPath("$.#0000FF").value(5));
    }

    @Test
    void testExtractColors_MissingUrl() throws Exception {
        mockMvc.perform(get("/api/extract-colors").param("url", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("URL parameter is required."));
    }

    @Test
    void testExtractColors_ServiceThrowsException() throws Exception {
        when(colorExtractionService.extractColorsFromUrl(anyString())).thenThrow(new RuntimeException("Service failure"));

        mockMvc.perform(get("/api/extract-colors").param("url", "http://example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing URL: Service failure"));
    }

    @Test
    void testExtractColors_NoColorsFound() throws Exception {
        when(colorExtractionService.extractColorsFromUrl(anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(get("/api/extract-colors").param("url", "http://example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No colors extracted. Check URL or website content."));
    }
}
