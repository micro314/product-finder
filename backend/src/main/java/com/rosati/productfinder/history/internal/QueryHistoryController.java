package com.rosati.productfinder.history.internal;

import com.rosati.productfinder.history.QueryHistoryService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/query-history")
class QueryHistoryController {
    private final QueryHistoryService queryHistory;

    QueryHistoryController(QueryHistoryService queryHistory) {
        this.queryHistory = queryHistory;
    }

    @GetMapping
    List<QueryHistoryService.QueryHistoryItem> history(Principal principal) {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) principal;
        return queryHistory.findForUser(authentication.getToken().getSubject());
    }

    @DeleteMapping("/{entryId}")
    void delete(@PathVariable Long entryId, Principal principal) {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) principal;
        queryHistory.deleteForUser(authentication.getToken().getSubject(), entryId);
    }

    @DeleteMapping
    void deleteAll(Principal principal) {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) principal;
        queryHistory.deleteAllForUser(authentication.getToken().getSubject());
    }
}
