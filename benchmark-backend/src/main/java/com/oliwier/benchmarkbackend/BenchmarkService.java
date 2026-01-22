package com.oliwier.benchmarkbackend;

import io.minio.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.UUID;

@Service
public class BenchmarkService {

    private final MinioClient minioClient;
    private final String BUCKET = "benchmark-bucket";

    public BenchmarkService(MinioClient minioClient) {
        this.minioClient = minioClient;
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize bucket", e);
        }
    }

    public BenchmarkResult runBenchmark(int fileCount, int sizeInMB) {
        byte[] data = new byte[sizeInMB * 1024 * 1024];
        new Random().nextBytes(data); // Generate random data

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < fileCount; i++) {
            String filename = "test-" + UUID.randomUUID() + ".bin";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(filename)
                        .stream(bais, data.length, -1)
                        .build());
            } catch (Exception e) {
                throw new RuntimeException("Upload failed", e);
            }
        }

        long endTime = System.currentTimeMillis();
        double durationSeconds = (endTime - startTime) / 1000.0;
        double totalDataMB = (double) fileCount * sizeInMB;
        double throughput = totalDataMB / durationSeconds;

        return new BenchmarkResult(fileCount, sizeInMB, durationSeconds, throughput);
    }

    // Helper record for JSON response
    public record BenchmarkResult(int files, int sizeMB, double timeSeconds, double throughputMBps) {}
}