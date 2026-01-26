package com.ecommerce.project.payloads.response;

import com.ecommerce.project.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    List<ProductDTO> content;

    private Integer pageNumber;
    private Integer pageSize;
    private long pageElement;
    private Integer totalPages;
    private boolean lastPage;
}
