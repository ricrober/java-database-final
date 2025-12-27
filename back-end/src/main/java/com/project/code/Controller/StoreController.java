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

/**
 * REST Controller for managing Store operations and order placement.
 * Handles store registration, existence validation, and transaction processing.
 */
@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderService orderService;

    /**
     * Registers a new store in the system.
     * @param store The store entity to be saved.
     * @return A map containing a success message.
     */
    @PostMapping
    public Map<String, String> addStore(@RequestBody Store store) {
        storeRepository.save(store);
        Map<String, String> map = new HashMap<>();
        map.put("message", "Store added successfully");
        return map;
    }

    /**
     * Checks i a store exists in the database.
     * @param storeId The unique identifier of the store.
     * @return True if the store exists, false otherwise.
     */
    @GetMapping("validate/{storeId}")
    public boolean validateStore(@PathVariable Long storeId) {
        return storeRepository.findByid(storeId) != null;
    }

    /**
     * Processes an order placement request.
     * @param placeOrderRequest DTO containing order details.
     * @param response The HTTP response object used to set status codes.
     * @return A map containing a success or error message.
     */
    @PostMapping("/placeOrder")
    public Map<String, String> placeOrder(@RequestBody PlaceOrderRequestDTO placeOrderRequest, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        try {
            orderService.saveOrder(placeOrderRequest);
            map.put("message", "Order placed successfully");
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch(Exception e) {
            map.put("Error", "An error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return map;
    }
   
}