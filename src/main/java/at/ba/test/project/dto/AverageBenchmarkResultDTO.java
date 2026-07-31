package at.ba.test.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AverageBenchmarkResultDTO {

    private String scenario;
    private int runs;
    private int orderCount;
    private int totalItems;
    private double averageQueryCount;
    private double averageDurationMs;

    private double averageUsedMemoryBeforeKb;
    private double averageUsedMemoryAfterKb;
    private double averageUsedMemoryDeltaKb;

    private double durationStdDevMs;
    private double durationVarianceMs;

    private double memoryDeltaStdDevKb;
    private double memoryDeltaVarianceKb;

    private double averageMemoryTimeProduct;
}