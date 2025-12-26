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

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;



@RestController
@RequestMapping("/inventory")
public class InventoryController {

@Autowired
private ProductRepository productRepository;

@Autowired
private InventoryRepository inventoryRepository;

@Autowired
private ServiceClass serviceClass;

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
            inventoryRepository.save(inventory);

        } else {
            map.put("message", "No data available for this product");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return map;
        }
    }

    response.setStatus(HttpServletResponse.SC_OK);
    return map;
}

@PostMapping
public Map<String, String> saveInventory(@RequestBody Inventory inventory, HttpServletResponse response) {
    Map<String, String> map = new HashMap<>();

    if (serviceClass.validateInventory(inventory)) {
        inventoryRepository.save(inventory);
    } else {
        map.put("message", "Data already present in inventory");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return map;
    }

    map.put(("message"), "Product added to inventory successfully");
    response.setStatus(HttpServletResponse.SC_CREATED);
    return map;
}

@GetMapping("/{storeid}")
public Map<String, Object> getAllProducts(@PathVariable Long storeid) {
    Map<String, Object> map = new HashMap<>();

    List<Product> result = productRepository.findProductsByStoreId(storeid);
    map.put("products", result);

    return map;
}

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

@GetMapping("search/{name}/{storeId}")
public Map<String, Object> searchProduct(@PathVariable String name, @PathVariable Long storeId) {
    Map<String, Object> map = new HashMap<>();

    map.put("product", productRepository.findByNameLike(storeId, name));

    return map;
}

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

@GetMapping("validate/{quantity}/{storeId}/{productId}")
public Boolean validateQuantity(
    @PathVariable int quantity,
    @PathVariable Long storeId,
    @PathVariable Long productId
) {
    Inventory result = inventoryRepository.findByProductIdAndStoreId(productId, storeId);
    return result.getStockLevel() >= quantity;
}


}
