package com.smartpark.backend.service;

import org.springframework.stereotype.Service;
import com.smartpark.backend.model.Stock;
import com.smartpark.backend.repository.StockRepository;

import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Stock> findAll() {
        return stockRepository.findAll();
    }

    public Stock findById(String id) {
        return stockRepository.findById(id).orElse(null);
    }

    public Stock create(Stock stock) {
        stock.setId(null);
        return stockRepository.save(stock);
    }

    public Stock update(Stock stock) {
        return stockRepository.save(stock);
    }

    public void delete(String id) {
        stockRepository.deleteById(id);
    }
}

