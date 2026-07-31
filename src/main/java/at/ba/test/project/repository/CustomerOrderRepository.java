package at.ba.test.project.repository;

import at.ba.test.project.dto.OrderSummaryDTO;
import at.ba.test.project.entity.CustomerOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    @Query("select o from CustomerOrder o")
    List<CustomerOrder> findAllForNPlusOne();

    @Query("""
       select distinct o
       from CustomerOrder o
       join fetch o.items i
       """)
    List<CustomerOrder> findAllWithItemsFetch();

    @Query("""
       select distinct o
       from CustomerOrder o
       join fetch o.items i
       join fetch i.product p
       """)
    List<CustomerOrder> findAllWithItemsAndProductsFetch();

    // LAZY summary: csak orderek, kapcsolatok érintése nélkül
    @Query("select o from CustomerOrder o")
    List<CustomerOrder> findAllOrderSummaries();

    // OVERFETCH: items felesleges betöltése
    @Query("""
       select distinct o
       from CustomerOrder o
       join fetch o.items i
       """)
    List<CustomerOrder> findAllOrderSummariesWithItemsOverfetch();

    // OVERFETCH: items + product felesleges betöltése
    @Query("""
       select distinct o
       from CustomerOrder o
       join fetch o.items i
       join fetch i.product p
       """)
    List<CustomerOrder> findAllOrderSummariesWithItemsAndProductsOverfetch();

    @EntityGraph(attributePaths = {"items"})
    @Query("select o from CustomerOrder o")
    List<CustomerOrder> findAllWithItemsEntityGraph();

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("select o from CustomerOrder o")
    List<CustomerOrder> findAllWithItemsAndProductsEntityGraph();

    @Query("""
        select new at.ba.test.project.dto.OrderSummaryDTO(o.id, o.createdAt)
        from CustomerOrder o
        """)
    List<OrderSummaryDTO> findAllOrderSummaryDtos();
}