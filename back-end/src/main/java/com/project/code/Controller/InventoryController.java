package com.project.code.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import com.project.code.Service.InventoryService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;


/**
 * Controller for managing Inventory and stock levels across different stores.
 * <p>
 * Provides endpoints for updating stock, validating quantity availability,
 * and filtering products based on store-specific inventory.
 * </p>
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryService inventoryService;

    /**
     * Updates both product details and its associated inventory stock level.
     * @param request A {@link CombinedRequest} containing both Product and Inventory data.
     * @param response The HTTP response object.
     * @return A map with satatus messages.
     */
    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest request, HttpServletResponse response) {
        Product product = request.getProduct();
        Inventory inventory = request.getInventory();

        Map<String, String> map = new HashMap<>();

        System.out.println("Stock Level: " + inventory.getStockLevel());

        if (!serviceClass.ValidateProductId(product.getId())) {
            map.put("message", "Id " + product.getId() + " not present in database");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return map;
        }

        productRepository.save(product);

        map.put("message", "Successfully updated product with id: " + product.getId());
        
        if (inventory != null) {
            Inventory result = serviceClass.getInventoryId(inventory);
            if (result != null) {
                inventory.setId(result.getId());
                //inventoryRepository.save(inventory);
                // This logic was moved to InventoryService
                inventoryService.saveInventory(inventory);
            } else {
                map.put("message", "No data available for this product");
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return map;
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        return map;
    }

    
    /**
     * Records a new inventory entry.
     * @param inventory The inventory object to save.
     * @param response The HTTP response to set status.
     * @return Success or conflict message.
     */
    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        if (serviceClass.validateInventory(inventory)) {
            inventoryService.saveInventory(inventory);
        } else {
            map.put("message", "Data already present in inventory");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return map;
        }

        map.put(("message"), "Product added to inventory successfully");
        response.setStatus(HttpServletResponse.SC_CREATED);
        return map;
    }


    /**
     * Retrieves all products associated with a specific store.
     * @param storeid The ID of the store.
     * @return List of products
     */
    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable Long storeid) {
        Map<String, Object> map = new HashMap<>();

        List<Product> result = productRepository.findProductsByStoreId(storeid);
        map.put("products", result);

        return map;
    }


    /**
     * Filters products based on category, name, and store.
     * <p>
     * Optimized to handle "null" string values provided by the frontend
     * for optional parameters.
     * </p>
     * @param category The category to filter by.
     * @param name The name to filter by.
     * @param storeid The store ID to filter by.
     * @return List of products,
     */
    @GetMapping("filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(
        @PathVariable String category,
        @PathVariable String name,
        @PathVariable Long storeid
    ) {
        Map<String, Object> map = new HashMap<>();

        if (category.equals("null")) {
            map.put("product", productRepository.findByNameLike(storeid, name));
            return map;
        } else if (name.equals("null")) {
            System.out.println("name is null");
            map.put("product", productRepository.findByCategoryAndStoreId(storeid, category));
            return map;
        }

        map.put("product", productRepository.findByNameAndCategory(storeid, name, category));
        return map;
    }


    /**
     * Searches for products by a partial name match.
     * @param name The product name to search.
     * @param storeId The store ID to search.
     * @return A map containing the result of the search.
     */
    @GetMapping("search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable String name, @PathVariable Long storeId) {
        Map<String, Object> map = new HashMap<>();

        map.put("product", productRepository.findByNameLike(storeId, name));

        return map;
    }


    /**
     * Deletes inventory records for a specific product.
     * @param id The product ID to delete.
     * @param response The HTTP response to set status.
     * @return A map containing the results of the search.
     */
    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable Long id, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        if (!serviceClass.ValidateProductId(id)) {
            map.put("message", "Id " + id + " not present in database");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return map;
        }

        inventoryRepository.deleteByProductId(id);
        response.setStatus(HttpServletResponse.SC_OK);
        map.put("message", "Deleted product successfully with id: " + id);

        return map;
    }


    /**
     * Validates if the requested quantity is available in stock.
     * @param quantity The quantity to check for.
     * @param storeId The store ID to check for.
     * @param productId The product ID to check for.
     * @return True if stock level >= quantity. False otherwise.
     */
    @GetMapping("validate/{quantity}/{storeId}/{productId}")
    public Boolean validateQuantity(
        @PathVariable int quantity,
        @PathVariable Long storeId,
        @PathVariable Long productId
    ) {
        //Inventory result = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
        // This logic was moved to InventoryService
        return inventoryService.checkStockAvailability() >= quantity;
    }


}
