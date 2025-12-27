package com.project.code.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import com.project.code.Service.ProductService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;


/**
 * REST Controller for managing Product lifecycle and searches.
 * <p>
 * This controller handles CRUD operations and integrates with
 * {@link ServiceClass} and {@link ProductService} to ensure business
 * logic and data integrity.
 * </p>
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceClass serviceClass;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductService productService;


    /**
     * Creates a new product after validating its existence.
     * @param product The product entity to persist.
     * @param response The HTTP response for status setting.
     * @return A map containing success or conflict messages.
     */
    @PostMapping
    public Map<String, String> addProduct(@RequestBody Product product, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        if (!serviceClass.validateProduct(product)) {
            map.put("message", "Product already present in database");
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return map;
        }

        productRepository.save(product);
        map.put("message", "Product added successfully");
        response.setStatus(HttpServletResponse.SC_CREATED);

        return map;
    }


    /**
     * Retrieves a single product by ist unique ID.
     * @param id The ID of the product.
     * @param response The HTTP response for 404 handling.
     * @return The product data or an error message.
     */
    @GetMapping("/product/{id}")
    public Map<String, Object> getProductById(@PathVariable Long id, HttpServletResponse response) {
        Map<String, Object> map = new HashMap<>();

        Product result = productRepository.findByid(id);

        if (result == null) {
            map.put("message", "Product not found with id: " + id);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return map;
        }

        map.put("products", result);

        return map;
    }


    /**
     * Updates an existing product.
     * @param product The product data to update.
     * @param response The HTTP response for status setting.
     * @return Success or Error message.
     */
    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>();

        try {
            productRepository.save(product);
            map.put("message", "Data updated successfully");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            map.put("message", "Error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return map;
    }

    
    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterByCategoryProduct(@PathVariable String name, @PathVariable String category) {
        Map<String, Object> map = new HashMap<>();

        if (name.equals("null")) {
            map.put("products", productRepository.findByCategory(category));
            return map;
        } else if (category.equals("null")) {
            map.put("products", productRepository.findProductBySubNameAndCategory(name, category));
            return map;
        }

        map.put("products", productRepository.findProductBySubName(name));

        return map;
    }


    /**
     * Lists all products available in the system.
     * @return A map containing a list of all products.
     */
    @GetMapping
    public Map<String, Object>  listProduct() {
        Map<String, Object> map = new HashMap<>();

        map.put("products", productRepository.findAll());

        return map;
    }


    /**
     * Filters product by category and store.
     * @param category The category to filter by.
     * @param storeId The store ID to filter by.
     * @return A map containing a list of products filtered by category and store ID.
     */
    @GetMapping("filter/{category}/{storeId}")
    public Map<String, Object> getProductByCategoryAndStoreId(@PathVariable String category, @PathVariable Long storeId) {
        Map<String, Object> map = new HashMap<>();

        List<Product> result = productRepository.findProductByCategory(category, storeId);

        map.put("product", result);

        return map;
    }


    /**
     * Deletes a product and its associated inventory entries.
     * @param id The ID of the product to remove.
     * @return Status message regarding the deletion.
     */
    @DeleteMapping("/{id}")
    @Transactional
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        Map<String, String> map = new HashMap<>();

        if (!serviceClass.ValidateProductId(id)) {
            map.put("message", "Id " + id + " not present in database");
            return map;
        }

        //inventoryRepository.deleteByProductId(id);
        //productRepository.deleteById(id);
        // This logic was moved to ProductService to mantain transactional integrity and separation of concerns
        productService.deleteProductAndInventory(id);

        map.put("message", "Deleted product successfully with id: " + id);

        return map;
    }


    /**
     * Searches for products by a partial name match.
     * @param name The name to search for.
     * @return A map containing the result of the search.
     */
    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable String name) {
        Map<String, Object> map = new HashMap<>();
        
        map.put("products", productRepository.findProductBySubName(name));

        return map;
    }
     
}