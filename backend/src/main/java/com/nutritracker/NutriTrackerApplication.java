package com.nutritracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NutriTrackerApplication {
  public static void main(String[] args) {
    SpringApplication.run(NutriTrackerApplication.class, args);
  }
}
