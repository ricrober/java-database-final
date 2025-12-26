package com.project.code.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/store")
public class StoreController {

@Autowired
private StoreRepository storeRepository;

@Autowired
private OrderService orderService;

@PostMapping
public Map<String, String> addStore(@RequestBody Store store) {
    storeRepository.save(store);
    Map<String, String> map = new HashMap<>();
    map.put("message", "Store added successfully");
    return map;
}

@GetMapping("validate/{storeId}")
public boolean validateStore(@PathVariable Long storeId) {
    Store store = storeRepository.findByid(storeId);

    return store != null;
}

@PostMapping("/placeOrder")
public Map<String, String> placeOrderMap(@RequestBody PlaceOrderRequestDTO placeOrderRequest, HttpServletResponse response) {
    Map<String, String> map = new HashMap<>();

    try {
        orderService.saveOrder(placeOrderRequest);
        map.put("message", "Order placed successfully");
        response.setStatus(HttpServletResponse.SC_CREATED);
    } catch(Error e) {
        map.put("Error", "An error occurred: " + e.getMessage());
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    return map;
}
   
}