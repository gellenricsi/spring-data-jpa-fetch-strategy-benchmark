package at.ba.test.project.config;

import at.ba.test.project.entity.*;
import at.ba.test.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private static final int ORDER_COUNT = 5000;

    @Bean
    CommandLineRunner loadData(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            CustomerOrderRepository customerOrderRepository,
            ReviewRepository reviewRepository
    ) {
        return args -> {

            if (categoryRepository.count() > 0) {
                return;
            }

            Random random = new Random();

            List<Category> categories = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                Category category = Category.builder()
                        .name("Category " + i)
                        .build();
                categories.add(category);
            }
            categoryRepository.saveAll(categories);

            List<Product> products = new ArrayList<>();
            for (int i = 1; i <= 500; i++) {
                Category randomCategory = categories.get(random.nextInt(categories.size()));

                Product product = Product.builder()
                        .name("Product " + i)
                        .price(BigDecimal.valueOf(10 + random.nextInt(500)))
                        .category(randomCategory)
                        .build();

                products.add(product);
            }
            productRepository.saveAll(products);

            List<Customer> customers = new ArrayList<>();
            for (int i = 1; i <= 200; i++) {
                Customer customer = Customer.builder()
                        .name("Customer " + i)
                        .email("customer" + i + "@mail.com")
                        .build();
                customers.add(customer);
            }
            customerRepository.saveAll(customers);

            List<CustomerOrder> orders = new ArrayList<>();

            for (int i = 1; i <= ORDER_COUNT; i++) {
                Customer randomCustomer = customers.get(random.nextInt(customers.size()));

                CustomerOrder order = CustomerOrder.builder()
                        .createdAt(LocalDateTime.now().minusDays(random.nextInt(30)))
                        .customer(randomCustomer)
                        .build();

                List<OrderItem> items = new ArrayList<>();
                int itemCount = 5 + random.nextInt(6);

                for (int j = 0; j < itemCount; j++) {
                    Product randomProduct = products.get(random.nextInt(products.size()));

                    OrderItem item = OrderItem.builder()
                            .quantity(1 + random.nextInt(3))
                            .order(order)
                            .product(randomProduct)
                            .build();

                    items.add(item);
                }

                order.setItems(items);
                orders.add(order);
            }

            customerOrderRepository.saveAll(orders);

            List<Review> reviews = new ArrayList<>();
            for (int i = 1; i <= 2000; i++) {
                Product randomProduct = products.get(random.nextInt(products.size()));
                Customer randomCustomer = customers.get(random.nextInt(customers.size()));

                Review review = Review.builder()
                        .rating(1 + random.nextInt(5))
                        .comment("Review text " + i)
                        .product(randomProduct)
                        .customer(randomCustomer)
                        .build();

                reviews.add(review);
            }

            reviewRepository.saveAll(reviews);

            System.out.println("=== TEST DATA LOADED SUCCESSFULLY ===");
        };
    }
}