package com.qingxiang.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BatchResultVO {

    private int successCount;
    private int failCount;
    private List<Failure> failures = new ArrayList<>();

    public void success() {
        successCount++;
    }

    public void failure(Long id, String message) {
        failCount++;
        failures.add(new Failure(id, message));
    }

    @Data
    public static class Failure {
        private final Long id;
        private final String message;

        public Failure(Long id, String message) {
            this.id = id;
            this.message = message;
        }
    }
}
