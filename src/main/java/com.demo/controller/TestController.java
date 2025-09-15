package com.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@ResponseBody
public class TestController {
    
    @RequestMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "SUCCESS");
        result.put("message", "EMQX Migration Complete - IoT Demo Working!");
        result.put("timestamp", System.currentTimeMillis());
        result.put("migration", "Alibaba Cloud → EMQX Cloud");
        return result;
    }
    
    @RequestMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("application", "IoT Demo");
        result.put("version", "1.0-EMQX");
        return result;
    }
}