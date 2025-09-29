package com.example.backend.controller;

import com.example.backend.entity.ClassificationResponse;
import com.example.backend.entity.Input;
import com.example.backend.repository.ClassificationRepo;
import com.example.backend.services.PredictServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class PredictController {

    private static final Logger LOGGER = Logger.getLogger(PredictController.class.getName());

    @Autowired
    private PredictServices predictServices;

    @GetMapping("/history")
    public List<Input> getHistory() {
        return predictServices.getAllData();
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
            LOGGER.severe("Error  " + e.getMessage());
            throw e;
        }


    }

}
