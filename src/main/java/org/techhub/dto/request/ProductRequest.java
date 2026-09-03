package org.techhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String productName;
}
