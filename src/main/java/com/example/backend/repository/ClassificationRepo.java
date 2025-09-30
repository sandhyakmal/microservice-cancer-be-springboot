package com.example.backend.repository;

import com.example.backend.entity.Input;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassificationRepo extends JpaRepository<Input, Long> {

}
