package at.ba.test.project.service;

import at.ba.test.project.dto.BenchmarkResultDTO;
import at.ba.test.project.dto.OrderSummaryDTO;
import at.ba.test.project.entity.CustomerOrder;
import at.ba.test.project.entity.OrderItem;
import at.ba.test.project.repository.CustomerOrderRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenchmarkScenarioService {

    private final CustomerOrderRepository customerOrderRepository;
    private final EntityManagerFactory entityManagerFactory;

    private long getUsedMemoryKb() {
        Runtime runtime = Runtime.getRuntime();
        long usedBytes = runtime.totalMemory() - runtime.freeMemory();
        return usedBytes / 1024;
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO countAllOrderItemsNPlusOne() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllForNPlusOne();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("N+1-Problem (Order → Items)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO countAllOrderItemsJoinFetch() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllWithItemsFetch();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("JOIN FETCH (Order → Items)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO calculateTotalValueNestedNPlusOne() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllForNPlusOne();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
            for (OrderItem item : order.getItems()) {
                item.getProduct().getPrice();
            }
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("Verschachteltes N+1 (Order → Items → Produkt)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO calculateTotalValueJoinFetch() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllWithItemsAndProductsFetch();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
            for (OrderItem item : order.getItems()) {
                item.getProduct().getPrice();
            }
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("JOIN FETCH (Order → Items → Produkt)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO loadOrderSummariesLazy() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllOrderSummaries();

        int orderCount = 0;
        for (CustomerOrder order : orders) {
            order.getId();
            order.getCreatedAt();
            orderCount++;
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("Lazy Loading (nur Order-Summary)")
                .orderCount(orderCount)
                .totalItems(0)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO loadOrderSummariesWithOverfetchItemsAndProducts() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllOrderSummariesWithItemsAndProductsOverfetch();

        int orderCount = 0;
        for (CustomerOrder order : orders) {
            order.getId();
            order.getCreatedAt();
            orderCount++;
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        int loadedItems = 0;
        for (CustomerOrder order : orders) {
            loadedItems += order.getItems().size();
        }

        return BenchmarkResultDTO.builder()
                .scenario("Overfetching (Items + Produkt geladen)")
                .orderCount(orderCount)
                .totalItems(loadedItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO countAllOrderItemsEntityGraph() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllWithItemsEntityGraph();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("EntityGraph (Order → Items)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO calculateTotalValueEntityGraph() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<CustomerOrder> orders = customerOrderRepository.findAllWithItemsAndProductsEntityGraph();

        int totalItems = 0;
        for (CustomerOrder order : orders) {
            totalItems += order.getItems().size();
            for (OrderItem item : order.getItems()) {
                item.getProduct().getPrice();
            }
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("EntityGraph (Order → Items → Produkt)")
                .orderCount(orders.size())
                .totalItems(totalItems)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    @Transactional(readOnly = true)
    public BenchmarkResultDTO loadOrderSummariesDtoProjection() {
        SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();

        long usedMemoryBeforeKb = getUsedMemoryKb();
        long start = System.nanoTime();

        List<OrderSummaryDTO> orders = customerOrderRepository.findAllOrderSummaryDtos();

        int orderCount = 0;
        for (OrderSummaryDTO order : orders) {
            order.getId();
            order.getCreatedAt();
            orderCount++;
        }

        long end = System.nanoTime();
        long usedMemoryAfterKb = getUsedMemoryKb();
        long usedMemoryDeltaKb = calculateMemoryDeltaKb(usedMemoryBeforeKb, usedMemoryAfterKb);

        return BenchmarkResultDTO.builder()
                .scenario("DTO-Projektion (Order-Summary)")
                .orderCount(orderCount)
                .totalItems(0)
                .queryCount(statistics.getPrepareStatementCount())
                .durationMs((end - start) / 1_000_000.0)
                .usedMemoryBeforeKb(usedMemoryBeforeKb)
                .usedMemoryAfterKb(usedMemoryAfterKb)
                .usedMemoryDeltaKb(usedMemoryDeltaKb)
                .build();
    }

    private long calculateMemoryDeltaKb(long beforeKb, long afterKb) {
        return Math.max(0, afterKb - beforeKb);
    }
}
