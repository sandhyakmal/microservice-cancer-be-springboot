package com.example.backend.controller;

import com.example.backend.entity.ClassificationResponse;
import com.example.backend.entity.Input;
import com.example.backend.repository.ClassificationRepo;
import com.example.backend.services.PredictServices;
import com.example.backend.utils.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PredictController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PredictController.class);


    @Autowired
    private PredictServices predictServices;

    @GetMapping("/history")
    public List<Input> getHistory() {
        return predictServices.getAllData();
    }

    @GetMapping("/v2/history")
    public ResponseEntity<ApiResponse<List<Input>>> v2GetAllHistory() {
        try {
            List<Input> input = predictServices.getAllData();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String apiResponseJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(input);

            LOGGER.info("Data Input: {}", apiResponseJson);

            if (input == null || input.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Data tidak ditemukan"));
            }

            return ResponseEntity
                    .ok(ApiResponse.success(input, "Data berhasil diambil"));

        } catch (Exception e) {
            LOGGER.error("Terjadi kesalahan saat mengambil data history: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Terjadi kesalahan server: " + e.getMessage()));
        }
    }



    @GetMapping("/history-detail/{id}")
    public Input getById(@PathVariable Long id) {
        return predictServices.getHistoryById(id);
    }

    @GetMapping("/v2/history-detail/{id}")
    public ResponseEntity<ApiResponse<Input>> v2getById(@PathVariable Long id) {
        try{
            Input input = predictServices.getHistoryById(id);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String apiResponseJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(input);

            LOGGER.info("Data Input by ID: {}", apiResponseJson);

            if (input == null) {
                return ResponseEntity.ok(ApiResponse.error(404,"Data tidak ditemukan"));
            }
            return ResponseEntity.ok(ApiResponse.success(input, "Data berhasil diambil"));

        } catch (Exception e){
            LOGGER.error("Terjadi kesalahan saat mengambil data history: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Terjadi kesalahan server: " + e.getMessage()));
        }
    }



    @PostMapping(value = "/predict", consumes = {"multipart/form-data"})
    public ResponseEntity<ClassificationResponse> predict(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestParam("imageDate") String imageDate
    ) throws IOException {

        try {
            ClassificationResponse response = predictServices.predict(name, age, imageDate, file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LOGGER.error("Error  " + e.getMessage());
            throw e;
        }


    }

    @PostMapping(value = "/v2/predict", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<?>> V2Predict(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("age") int age,
            @RequestParam("imageDate") String imageDate
    ) throws IOException {

        ApiResponse<ClassificationResponse> response = predictServices.predictv2(name, age, imageDate, file);

        if ("Success".equalsIgnoreCase(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}
