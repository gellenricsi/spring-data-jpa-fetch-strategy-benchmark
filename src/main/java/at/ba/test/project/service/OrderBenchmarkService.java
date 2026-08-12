package at.ba.test.project.service;

import at.ba.test.project.dto.AverageBenchmarkResultDTO;
import at.ba.test.project.dto.BenchmarkResultDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class OrderBenchmarkService {

    private static final int WARMUP_RUNS = 4;
    private static final int MEASURED_RUNS = 10;

    private final BenchmarkScenarioService benchmarkScenarioService;
    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    private void resetPersistenceContext() {
        entityManager.clear();
    }

    private void resetHibernateStatistics() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        sessionFactory.getStatistics().clear();
    }

    private void prepareBenchmarkRun() {
        resetPersistenceContext();
        resetHibernateStatistics();
    }

    public List<BenchmarkResultDTO> runAllBenchmarks() {
        prepareBenchmarkRun();
        BenchmarkResultDTO r0 = benchmarkScenarioService.loadOrderSummariesLazy();

        prepareBenchmarkRun();
        BenchmarkResultDTO r00 = benchmarkScenarioService.loadOrderSummariesDtoProjection();

        prepareBenchmarkRun();
        BenchmarkResultDTO r1 = benchmarkScenarioService.countAllOrderItemsNPlusOne();

        prepareBenchmarkRun();
        BenchmarkResultDTO r2 = benchmarkScenarioService.countAllOrderItemsJoinFetch();

        prepareBenchmarkRun();
        BenchmarkResultDTO r2b = benchmarkScenarioService.countAllOrderItemsEntityGraph();

        prepareBenchmarkRun();
        BenchmarkResultDTO r3 = benchmarkScenarioService.calculateTotalValueNestedNPlusOne();

        prepareBenchmarkRun();
        BenchmarkResultDTO r4 = benchmarkScenarioService.calculateTotalValueJoinFetch();

        prepareBenchmarkRun();
        BenchmarkResultDTO r4b = benchmarkScenarioService.calculateTotalValueEntityGraph();

        prepareBenchmarkRun();
        BenchmarkResultDTO r5 = benchmarkScenarioService.loadOrderSummariesWithOverfetchItemsAndProducts();

        return List.of(r0, r00, r1, r2, r2b, r3, r4, r4b, r5);
    }

    public List<AverageBenchmarkResultDTO> runAllBenchmarksAveraged() {
        return List.of(
                averageScenario("Lazy Loading (nur Order-Summary)", benchmarkScenarioService::loadOrderSummariesLazy),
                averageScenario("DTO-Projektion (Order-Summary)", benchmarkScenarioService::loadOrderSummariesDtoProjection),
                averageScenario("N+1-Problem (Order → Items)", benchmarkScenarioService::countAllOrderItemsNPlusOne),
                averageScenario("JOIN FETCH (Order → Items)", benchmarkScenarioService::countAllOrderItemsJoinFetch),
                averageScenario("EntityGraph (Order → Items)", benchmarkScenarioService::countAllOrderItemsEntityGraph),
                averageScenario("Verschachteltes N+1 (Order → Items → Produkt)", benchmarkScenarioService::calculateTotalValueNestedNPlusOne),
                averageScenario("JOIN FETCH (Order → Items → Produkt)", benchmarkScenarioService::calculateTotalValueJoinFetch),
                averageScenario("EntityGraph (Order → Items → Produkt)", benchmarkScenarioService::calculateTotalValueEntityGraph),
                averageScenario("Overfetching (Items + Produkt geladen)", benchmarkScenarioService::loadOrderSummariesWithOverfetchItemsAndProducts)
        );
    }

    private AverageBenchmarkResultDTO averageScenario(
            String scenarioName,
            Supplier<BenchmarkResultDTO> benchmarkSupplier
    ) {
        long totalQueries = 0;
        double totalDuration = 0;
        long totalMemoryBefore = 0;
        long totalMemoryAfter = 0;
        long totalMemoryDelta = 0;

        int orderCount = 0;
        int totalItems = 0;

        List<Double> durations = new ArrayList<>();
        List<Double> memoryDeltas = new ArrayList<>();
        List<Double> memoryTimeProducts = new ArrayList<>();

        // warm-up
        for (int i = 0; i < WARMUP_RUNS; i++) {
            prepareBenchmarkRun();
            benchmarkSupplier.get();
        }

        // measured runs
        for (int i = 0; i < MEASURED_RUNS; i++) {
            prepareBenchmarkRun();
            BenchmarkResultDTO result = benchmarkSupplier.get();

            totalQueries += result.getQueryCount();
            totalDuration += result.getDurationMs();
            totalMemoryBefore += result.getUsedMemoryBeforeKb();
            totalMemoryAfter += result.getUsedMemoryAfterKb();
            totalMemoryDelta += result.getUsedMemoryDeltaKb();

            durations.add(result.getDurationMs());
            memoryDeltas.add((double) result.getUsedMemoryDeltaKb());
            memoryTimeProducts.add(result.getDurationMs() * result.getUsedMemoryDeltaKb());

            System.out.println(
                    scenarioName
                            + " | Run " + (i + 1)
                            + " | Memory before: " + result.getUsedMemoryBeforeKb() + " KB"
                            + " | Memory after: " + result.getUsedMemoryAfterKb() + " KB"
                            + " | Memory delta: " + result.getUsedMemoryDeltaKb() + " KB"
                            + " | Duration: " + result.getDurationMs() + " ms"
            );

            orderCount = result.getOrderCount();
            totalItems = result.getTotalItems();
        }

        double avgDuration = totalDuration / MEASURED_RUNS;
        double avgMemoryDelta = (double) totalMemoryDelta / MEASURED_RUNS;

        double durationVariance = variance(durations, avgDuration);
        double durationStdDev = Math.sqrt(durationVariance);

        double memoryVariance = variance(memoryDeltas, avgMemoryDelta);
        double memoryStdDev = Math.sqrt(memoryVariance);

        double avgMemoryTimeProduct = memoryTimeProducts.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return AverageBenchmarkResultDTO.builder()
                .scenario(scenarioName)
                .runs(MEASURED_RUNS)
                .orderCount(orderCount)
                .totalItems(totalItems)
                .averageQueryCount((double) totalQueries / MEASURED_RUNS)
                .averageDurationMs(avgDuration)
                .durationStdDevMs(durationStdDev)
                .durationVarianceMs(durationVariance)
                .averageUsedMemoryBeforeKb((double) totalMemoryBefore / MEASURED_RUNS)
                .averageUsedMemoryAfterKb((double) totalMemoryAfter / MEASURED_RUNS)
                .averageUsedMemoryDeltaKb(avgMemoryDelta)
                .memoryDeltaStdDevKb(memoryStdDev)
                .memoryDeltaVarianceKb(memoryVariance)
                .averageMemoryTimeProduct(avgMemoryTimeProduct)
                .build();
    }

    private double variance(List<Double> values, double mean) {
        if (values.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sum += diff * diff;
        }

        return sum / values.size();
    }
}