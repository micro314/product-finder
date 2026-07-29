package com.rosati.productfinder.history;

import com.rosati.productfinder.product.ProductQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(QueryHistoryService.class)
class QueryHistoryServiceTest {
    @Autowired QueryHistoryService queryHistory;

    @Test
    void storesQueriesSeparatelyForEachUserAndReturnsNewestFirst() {
        queryHistory.record("user-a", new ProductQuery("rtx 5070", 20));
        queryHistory.record("user-b", new ProductQuery("rx 9070", 10));
        queryHistory.record("user-a", new ProductQuery("rtx 5090", 5));

        assertThat(queryHistory.findForUser("user-a"))
                .extracting(QueryHistoryService.QueryHistoryItem::query)
                .containsExactly("rtx 5090", "rtx 5070");
    }

    @Test
    void movesAnExistingQueryToTheFrontWithoutCreatingADuplicate() {
        queryHistory.record("user-a", new ProductQuery("rtx 5070", 20));
        queryHistory.record("user-a", new ProductQuery("rx 9070", 10));
        queryHistory.record("user-a", new ProductQuery("RTX 5070", 5));

        assertThat(queryHistory.findForUser("user-a"))
                .extracting(QueryHistoryService.QueryHistoryItem::query)
                .containsExactly("RTX 5070", "rx 9070");
    }
}
