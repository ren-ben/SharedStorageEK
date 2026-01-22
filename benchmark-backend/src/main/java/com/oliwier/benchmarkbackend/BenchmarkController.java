package com.oliwier.benchmarkbackend;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class BenchmarkController {

    private final BenchmarkService service;

    public BenchmarkController(BenchmarkService service) {
        this.service = service;
    }

    @PostMapping("/benchmark")
    public BenchmarkService.BenchmarkResult startBenchmark(
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "10") int sizeMB) {
        return service.runBenchmark(count, sizeMB);
    }
}