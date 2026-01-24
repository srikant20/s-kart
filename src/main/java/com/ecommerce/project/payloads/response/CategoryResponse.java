package com.ecommerce.project.payloads.response;

import com.ecommerce.project.dto.CategoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private List<CategoryDTO> content;

    private Integer pageNumber;
    private Integer pageSize;
    private long pageElement;
    private Integer totalPages;
    private boolean lastPage;
}
