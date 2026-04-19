package com.smartpark.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.smartpark.backend.model.Stock;
import com.smartpark.backend.service.StockService;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public List<Stock> findAll() {
        return stockService.findAll();
    }

    @GetMapping("/{id}")
    public Stock findById(@PathVariable String id) {
        Stock stock = stockService.findById(id);
        if (stock == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock non trouvé");
        }
        return stock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Stock create(@RequestBody Stock stock) {
        return stockService.create(stock);
    }

    @PutMapping("/{id}")
    public Stock update(@PathVariable String id, @RequestBody Stock stock) {
        Stock existing = stockService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock non trouvé");
        }

        existing.setQuantiteDisponible(stock.getQuantiteDisponible());
        existing.setQuantiteMin(stock.getQuantiteMin());
        existing.setQuantiteMax(stock.getQuantiteMax());
        existing.setDernierReapprovisionnement(stock.getDernierReapprovisionnement());
        existing.setProduitId(stock.getProduitId());

        return stockService.update(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        Stock existing = stockService.findById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock non trouvé");
        }
        stockService.delete(id);
    }
}

