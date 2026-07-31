package at.ba.test.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BenchmarkResultDTO {

    private String scenario;
    private int orderCount;
    private int totalItems;
    private long queryCount;
    private double durationMs;

    private long usedMemoryBeforeKb;
    private long usedMemoryAfterKb;
    private long usedMemoryDeltaKb;
}