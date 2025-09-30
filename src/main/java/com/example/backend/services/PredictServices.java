package com.example.backend.services;

import com.example.backend.entity.ClassificationResponse;
import com.example.backend.entity.Input;
import com.example.backend.entity.Probabilites;
import com.example.backend.repository.ClassificationRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class PredictServices {

    private static final Logger LOGGER = Logger.getLogger(PredictServices.class.getName());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ClassificationRepo classificationRepo;

    @Value("${url.predict}")
    private String urlPredict;

    @Value("${app.upload.dir}")
    private String uploadDir;


    public List<Input> getAllData() {
        return classificationRepo.findAll();
    }

    public ResponseEntity<Input> getHistoryById(Long id) {
        try {
            Optional<Input> inputOpt = classificationRepo.findById(id);

            if (inputOpt.isPresent()) {
                ObjectMapper objectMapper = new ObjectMapper();
                String databyID = objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(inputOpt.get());
                LOGGER.info("Input Opt JSON: \n" + databyID);

                return ResponseEntity.ok(inputOpt.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ClassificationResponse predict(String name, int age, String imageDate, MultipartFile file) throws IOException {
        // Validasi input
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File tidak boleh kosong");
        }

        File directory = new File(uploadDir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Gagal membuat direktori upload: " + directory.getAbsolutePath());
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String cleanFileName = UUID.randomUUID().toString() + extension;

        File savedFile = new File(directory, cleanFileName);
        LOGGER.info("Menyimpan file di: " + savedFile.getAbsolutePath());
        file.transferTo(new File(savedFile.getAbsolutePath()));

        try {
            // Siapkan request ke API prediksi
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(savedFile));

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = 
                new HttpEntity<>(body, headers);

            // Panggil API prediksi
            ResponseEntity<ClassificationResponse> response = restTemplate.exchange(
                    urlPredict,
                    HttpMethod.POST,
                    requestEntity,
                    ClassificationResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("Gagal mendapatkan respons dari API prediksi");
            }

            ClassificationResponse result = response.getBody();
            // Log hasil prediksi dalam format JSON
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String apiResponseJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(result);
                LOGGER.info("Hasil prediksi dari API (JSON):\n" + apiResponseJson);
            } catch (Exception e) {
                LOGGER.warning("Gagal mengkonversi hasil API ke JSON: " + e.getMessage());
                LOGGER.info("Hasil prediksi dari API (toString): " + result);
            }

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String tanggalInput = now.format(dateTimeFormatter);

            Input input = new Input();
            input.setName(name);
            input.setAge(age);
            input.setImage(cleanFileName);
            input.setImagePath(uploadDir + cleanFileName);
            input.setResult(result.getPredictedClass());
            input.setConfidence(result.getConfidence());
            input.setDateInput(tanggalInput);
            input.setImageDate(imageDate);

            // Set probabilitas
            if (result.getProbabilites() != null) {
                input.setCancerProbability(result.getProbabilites().getCancer());
                input.setNonCancerProbability(result.getProbabilites().getNonCancer());
            }
            // Log data sebelum disimpan
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String inputJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(input);
                LOGGER.info("Data yang akan disimpan ke database:\n" + inputJson);
            } catch (Exception e) {
                LOGGER.warning("Gagal mengkonversi data ke JSON: " + e.getMessage());
                LOGGER.info("Data input (format toString): " + input);
            }

            // Simpan ke database
            classificationRepo.save(input);
            return result;
            
        } catch (Exception e) {
            // Hapus file yang sudah diupload jika terjadi error
            if (savedFile.exists() && !savedFile.delete()) {
                LOGGER.warning("Gagal menghapus file sementara: " + savedFile.getAbsolutePath());
            }
            throw new RuntimeException("Terjadi kesalahan saat melakukan prediksi: " + e.getMessage(), e);
        }
    }
}
