package at.ba.test.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderSummaryDTO {
    private Long id;
    private LocalDateTime createdAt;
}