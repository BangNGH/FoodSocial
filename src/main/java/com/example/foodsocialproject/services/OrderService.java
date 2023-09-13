package com.example.foodsocialproject.services;


import com.example.foodsocialproject.entity.Orders;
import com.example.foodsocialproject.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    public List<Orders> getAllOrders(){
        return orderRepository.findAll();
    }
    public List<Orders> getAllOrdersByUserId(Long userId){
        return orderRepository.findAllOrderByUserId(userId);
    }
    public Orders getOrderById(Long id){
        return orderRepository.findById(id).orElseThrow();
    }

    public Page<Orders> findPaginated(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.orderRepository.findAll(pageable);
    }

    public Page<Orders> findUserOrdersPage(UUID id, int pageNo, int pageSize, String sortField, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortField).ascending() : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize, sort);
        return this.orderRepository.getOrdersByUserId(id, pageable);
    }
}
