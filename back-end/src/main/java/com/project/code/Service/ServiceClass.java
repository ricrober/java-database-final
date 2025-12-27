package com.project.code.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;

/**
 * Service class for handling business logic related to Products and Inventory.
 * Provides validation methods to ensure data integrity before persistence.
 */
@Service
public class ServiceClass {
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final InventoryRepository inventoryRepository;

    /**
     * Constructor-based dependency injection.
     * @param inventoryRepository The repository for inventory data access.
     * @param productRepository The repository for product data access. 
     */
    public ServiceClass(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Validates if an inventory record is unique for a specific product at a specific store.
     * <p>
     * This ensures we don't have duplicate inventory entries for the same product/store combination.
     * </p>
     * 
     * @param inventory The inventory object to check.
     * @return true if no existing record is found (valid for creation), false otherwise.
     */
    public boolean validateInventory(Inventory inventory) {
        // Explicitly check for null product or store to prevent NullPointerExceptions.codeApplication
        if (inventory.getProduct() == null || inventory.getStore() == null) {
            return false;
        }

        Inventory result = inventoryRepository.findByProductIdAndStoreId(
            inventory.getProduct().getId(),
            inventory.getStore().getId()
        );

        return result == null;
    }


    /**
     * Checks if a product name is already taken in the database.
     * @param product The product to validate.
     * @return true if the name is unique, false if it already exists.
     */
    public boolean validateProduct(Product product) {
        Product result = productRepository.findByName(product.getName());
        return result == null;
    }

    /**
     * Validates if a product exists based on its Id.
     * @param id The ID of the product to validate.
     * @return true if the product is valid, false otherwise.
     */
    public boolean validateProductId(Long id) {
        //Product result = productRepository.findByid(id);
        //System.out.println(result);
        //return result != null;
        // Optimized check: existsById is usually faster than fetching the whole entity
        return productRepository.existsById(id);
    }


    /**
     * Retrieves an existing inventory record for a specific product-store pair.
     * @param inventory DTO or object containing product and store IDs.
     * @return The found Inventory record, or null if none exists.
     */
    public Inventory getInventoryId(Inventory inventory) {
        return inventoryRepository.findByProductIdAndStoreId(
            inventory.getProduct().getId(),
            inventory.getStore().getId()
        );
    }

}
