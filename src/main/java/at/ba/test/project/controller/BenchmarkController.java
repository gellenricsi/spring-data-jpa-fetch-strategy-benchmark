package at.ba.test.project.controller;

import at.ba.test.project.dto.AverageBenchmarkResultDTO;
import at.ba.test.project.dto.BenchmarkResultDTO;
import at.ba.test.project.service.BenchmarkScenarioService;
import at.ba.test.project.service.OrderBenchmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BenchmarkController {

    private final BenchmarkScenarioService benchmarkScenarioService;
    private final OrderBenchmarkService orderBenchmarkService;

    @GetMapping("/benchmark/nplusone/items")
    public BenchmarkResultDTO benchmarkNPlusOneItems() {
        return benchmarkScenarioService.countAllOrderItemsNPlusOne();
    }

    @GetMapping("/benchmark/joinfetch/items")
    public BenchmarkResultDTO benchmarkJoinFetchItems() {
        return benchmarkScenarioService.countAllOrderItemsJoinFetch();
    }

    @GetMapping("/benchmark/nplusone/items-products")
    public BenchmarkResultDTO benchmarkNestedNPlusOne() {
        return benchmarkScenarioService.calculateTotalValueNestedNPlusOne();
    }

    @GetMapping("/benchmark/joinfetch/items-products")
    public BenchmarkResultDTO benchmarkJoinFetchItemsProducts() {
        return benchmarkScenarioService.calculateTotalValueJoinFetch();
    }

    @GetMapping("/benchmark/run-all")
    public List<BenchmarkResultDTO> runAllBenchmarks() {
        return orderBenchmarkService.runAllBenchmarks();
    }

    @GetMapping("/benchmark/run-all-averaged")
    public List<AverageBenchmarkResultDTO> runAllBenchmarksAveraged() {
        return orderBenchmarkService.runAllBenchmarksAveraged();
    }

    @GetMapping(value = "/benchmark/run-all-averaged/csv", produces = "text/csv")
    public String runAllBenchmarksAveragedCsv() {
        List<AverageBenchmarkResultDTO> results = orderBenchmarkService.runAllBenchmarksAveraged();

        StringBuilder csv = new StringBuilder();
        csv.append("scenario,runs,orderCount,totalItems,averageQueryCount,averageDurationMs,durationStdDevMs,durationVarianceMs,averageUsedMemoryDeltaKb,memoryDeltaStdDevKb,memoryDeltaVarianceKb,averageMemoryTimeProduct\n");

        for (AverageBenchmarkResultDTO result : results) {
            csv.append(result.getScenario()).append(",")
                    .append(result.getRuns()).append(",")
                    .append(result.getOrderCount()).append(",")
                    .append(result.getTotalItems()).append(",")
                    .append(result.getAverageQueryCount()).append(",")
                    .append(result.getAverageDurationMs()).append(",")
                    .append(result.getDurationStdDevMs()).append(",")
                    .append(result.getDurationVarianceMs()).append(",")
                    .append(result.getAverageUsedMemoryDeltaKb()).append(",")
                    .append(result.getMemoryDeltaStdDevKb()).append(",")
                    .append(result.getMemoryDeltaVarianceKb()).append(",")
                    .append(result.getAverageMemoryTimeProduct()).append("\n");
        }

        return csv.toString();
    }

    @GetMapping("/benchmark/run-all-save")
    public List<AverageBenchmarkResultDTO> runAllAndSave() throws Exception {
        List<AverageBenchmarkResultDTO> results =
                orderBenchmarkService.runAllBenchmarksAveraged();

        Path path = Paths.get("benchmark-results_5000_orders.csv").toAbsolutePath();

        StringBuilder csv = new StringBuilder();
        csv.append("scenario,runs,orderCount,totalItems,averageQueryCount,averageDurationMs,durationStdDevMs,durationVarianceMs,averageUsedMemoryDeltaKb,memoryDeltaStdDevKb,memoryDeltaVarianceKb,averageMemoryTimeProduct\n");

        for (AverageBenchmarkResultDTO r : results) {
            csv.append(r.getScenario()).append(",")
                    .append(r.getRuns()).append(",")
                    .append(r.getOrderCount()).append(",")
                    .append(r.getTotalItems()).append(",")
                    .append(r.getAverageQueryCount()).append(",")
                    .append(r.getAverageDurationMs()).append(",")
                    .append(r.getDurationStdDevMs()).append(",")
                    .append(r.getDurationVarianceMs()).append(",")
                    .append(r.getAverageUsedMemoryDeltaKb()).append(",")
                    .append(r.getMemoryDeltaStdDevKb()).append(",")
                    .append(r.getMemoryDeltaVarianceKb()).append(",")
                    .append(r.getAverageMemoryTimeProduct()).append("\n");
        }

        Files.writeString(path, csv.toString());

        System.out.println("CSV saved to: " + path);
        System.out.println("RUN SAVE CALLED at: " + System.currentTimeMillis());

        return results;
    }

    @GetMapping("/benchmark/lazy/summary")
    public BenchmarkResultDTO benchmarkLazySummary() {
        return benchmarkScenarioService.loadOrderSummariesLazy();
    }

    @GetMapping("/benchmark/overfetch/summary-items-products")
    public BenchmarkResultDTO benchmarkOverfetchSummaryItemsProducts() {
        return benchmarkScenarioService.loadOrderSummariesWithOverfetchItemsAndProducts();
    }

    @GetMapping("/benchmark/entitygraph/items")
    public BenchmarkResultDTO benchmarkEntityGraphItems() {
        return benchmarkScenarioService.countAllOrderItemsEntityGraph();
    }

    @GetMapping("/benchmark/entitygraph/items-products")
    public BenchmarkResultDTO benchmarkEntityGraphItemsProducts() {
        return benchmarkScenarioService.calculateTotalValueEntityGraph();
    }

    @GetMapping("/benchmark/dto/summary")
    public BenchmarkResultDTO benchmarkDtoSummary() {
        return benchmarkScenarioService.loadOrderSummariesDtoProjection();
    }
}