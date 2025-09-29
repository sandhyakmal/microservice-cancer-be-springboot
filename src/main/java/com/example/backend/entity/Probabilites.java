package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Probabilites {

    @JsonProperty("Cancer")
    private Double cancer;

    @JsonProperty("Non-Cancer")
    private Double nonCancer;

    public Double getCancer() {
        return cancer;
    }

    public void setCancer(Double cancer) {
        this.cancer = cancer;
    }

    public Double getNonCancer() {
        return nonCancer;
    }

    public void setNonCancer(Double nonCancer) {
        this.nonCancer = nonCancer;
    }
}
