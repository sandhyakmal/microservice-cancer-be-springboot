package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClassificationResponse {

    @JsonProperty("predicted_class")
    private String predictedClass;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("probabilities")
    private Probabilites probabilites;

    public String getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(String predictedClass) {
        this.predictedClass = predictedClass;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Probabilites getProbabilites() {
        return probabilites;
    }

    public void setProbabilites(Probabilites probabilites) {
        this.probabilites = probabilites;
    }
}
