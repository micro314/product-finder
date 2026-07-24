package com.rosati.productfinder.search.internal;

import com.rosati.productfinder.history.QueryHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductSearchController.class)
class ProductSearchControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ProductSearchService service;
    @MockitoBean QueryHistoryService queryHistory;

    @Test
    void delegatesValidSearches() throws Exception {
        when(service.search(any())).thenReturn(new SearchResponse(null, null));
        mvc.perform(get("/api/products/search").param("q", "rtx 5070").param("limit", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products").isArray());
    }

    @Test
    void reportsInvalidSearchesAsProblemDetails() throws Exception {
        mvc.perform(get("/api/products/search").param("q", " "))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Invalid search request"));
    }
}
