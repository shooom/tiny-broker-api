package com.monolith.api;

import com.monolith.dto.portfolio.PortfolioViewResponse;
import com.monolith.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioViewResponse> getPortfolio(@PathVariable String portfolioId) {
        PortfolioViewResponse response = portfolioService.getPortfolio(portfolioId);

        return ResponseEntity.ok(response);
    }
}
