package com.project.code.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;

/**
 * Service class to manage order placement logic.
 * Handles customer retrieval, store validation, and inventory deduction.
 */
@Service
public class OrderService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderDetailsRepository orderDetailsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;


    /**
     * Processes a complete customer order.
     * <p>
     * This method is transactional; if any part of the order processing fails
     * (e.g., insufficient stock), all database changes will be rolled back.
     * </p>
     * 
     * @param placeOrderRequest DTO containing customer info, store ID, and products.
     * @throws RuntimeException if the store is not found or inventory is insufficient.
     */
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest) {

        Customer customer = getOrCreaCustomer(placeOrderRequest);

        Store store = storeRepository.findById(
            placeOrderRequest.getStoreId()
        ).orElseThrow(
            () -> new RuntimeException("Store not found with ID: " + placeOrderRequest.getStoreId())
        );

        OrderDetails orderDetails = createOrderHeader(customer, store, placeOrderRequest.getTotalPrice());

        processOrderItems(placeOrderRequest.getPurchaseProduct(), orderDetails, store.getId());

    }
   

    /**
     * Retrieves an existing customer by email or creates a new one.
     * @param request The {@link PlaceOrderRequestDTO}.
     * @return The existing Customer or the new one created.
     */
    private Customer getOrCreaCustomer(PlaceOrderRequestDTO request) {
        Customer existing = customerRepository.findByEmail(request.getCustomerEmail());

        if (existing != null) {
            return existing;
        }

        Customer newCustomer = new Customer();

        newCustomer.setName(request.getCustomerName());
        newCustomer.setEmail(request.getCustomerEmail());
        newCustomer.setPhone(request.getCustomerPhone());

        return newCustomer;
    }


    /**
     * Saves the parent OrderDetails record.
     * @param customer The customer to add to the order.
     * @param store The store to add to the order.
     * @param total The total of the order.
     * @return Created OrderDetails Object.
     */
    private OrderDetails createOrderHeader(Customer customer, Store store, double total) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(total);
        orderDetails.setDate(LocalDateTime.now());
        
        return orderDetailsRepository.save(orderDetails);
    }


    /**
     * Processes each product in the order, updates stock levels, and saves OrderItems.
     * @param products The products to process.
     * @param order The order to process.
     * @param storeId The ID of the store.
     * 
     * @throws RuntimeException if requested quantity exeeds available stock.
     */
    private void processOrderItems(List<PurchaseProductDTO> products, OrderDetails order, Long storeId) {
        for (PurchaseProductDTO productDTO : products) {
            //Retrieve inventory for the specific store
            Inventory inventory = inventoryRepository.findByProductIdAndStoreId(productDTO.getId(), storeId);

            //Stock validation
            if (inventory == null || inventory.getStockLevel() < productDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product ID: " + productDTO.getId());
            }

            //Deduct stock and save
            inventory.setStockLevel(inventory.getStockLevel() - productDTO.getQuantity());
            inventoryRepository.save(inventory);

            //Save the individual order item
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(productRepository.findByid(productDTO.getId()));
            item.setQuantity(productDTO.getQuantity());
            item.setPrice(productDTO.getPrice() * productDTO.getQuantity());
            orderItemRepository.save(item);
        }
    }
}