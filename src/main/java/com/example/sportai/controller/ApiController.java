package com.example.sportai.controller;

import com.example.sportai.dto.Req;
import com.example.sportai.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ai/api")
public class ApiController {

    private final ApiService apiService;

    public ApiController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping("/plan")
    public ResponseEntity<List<Map<String, Map<String, List<String>>>>> getPlan(@RequestBody Req req) {
        return ResponseEntity.ok(apiService.generateWorkout(req));
    }
}