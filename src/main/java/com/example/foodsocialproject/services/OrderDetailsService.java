package com.example.foodsocialproject.services;

import com.example.foodsocialproject.entity.OrderDetails;
import com.example.foodsocialproject.repository.OderDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailsService {
    @Autowired
    OderDetailsRepository oderDetailsRepository;
    public List<OrderDetails> getAllOrderDetailsByOrderId(Long id){
        return oderDetailsRepository.findAllOrderDetailsByOrderId(id);
    }
}
