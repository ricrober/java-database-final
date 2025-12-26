package com.project.code.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;

@Service
public class ServiceClass {
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final InventoryRepository inventoryRepository;

    public ServiceClass(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    };

public boolean validateInventory(Inventory inventory) {
    Inventory result = inventoryRepository.findByProductIdAndStoreId(inventory.getProduct().getId(), inventory.getStore().getId());
    return result == null;
}

public boolean validateProduct(Product product) {
    Product result = productRepository.findByName(product.getName());
    return result == null;
}

public boolean ValidateProductId(Long id) {
    Product result = productRepository.findByid(id);
    System.out.println(result);
    return result != null;
}

public Inventory getInventoryId(Inventory inventory) {
    return inventoryRepository.findByProductIdAndStoreId(inventory.getProduct().getId(), inventory.getStore().getId());
}

}
