package com.divacode.ecom_proj.controller;

import com.divacode.ecom_proj.dto.ProductDTO;
import com.divacode.ecom_proj.exception.ProductNotFoundException;
import com.divacode.ecom_proj.model.Product;
import com.divacode.ecom_proj.response.ApiResponse;
import com.divacode.ecom_proj.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    // Assuming you have this injected for direct image serving (if using local fs)
    // @Value("${app.upload.dir}")
    // private String uploadDir; // Only needed if serving locally directly from controller

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>(true, "Products fetched", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable int id) {
        logger.info("Fetching product with id {}", id);
        Product product = productService.getProductById(id); // ProductNotFoundException handled by ControllerAdvice
        return ResponseEntity.ok(new ApiResponse<>(true, "Product fetched", product));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> addProduct(
            @Valid @RequestPart("productDTO") ProductDTO productDTO, // Explicitly name for clarity
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException { // Image is optional
        logger.info("Received ProductDTO for add: {}", productDTO);
        Product product = productService.addProduct(productDTO, imageFile);
        return new ResponseEntity<>(new ApiResponse<>(true, "Product created", product), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable int id,
            @Valid @RequestPart("productDTO") ProductDTO productDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {
        logger.info("Updating product id {}", id);
        Product product = productService.updateProduct(id, productDTO, imageFile);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product updated", product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable int id) {
        logger.info("Deleting product with id {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product deleted", null));
    }

    // --- CHANGE HERE for Image Retrieval ---
    // Instead of returning byte[], you now return the image URL in the Product object.
    // If you need a direct image endpoint for local storage, use this:
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int id) {
        logger.info("Fetching image for product with id {}", id);
        try {
            Product product = productService.getProductById(id);

            if (product.getImageData() == null || product.getImageData().length == 0) {
                // Handle case where no image is associated
                // You can return a 404 Not Found status or a default placeholder image
                return ResponseEntity.notFound().build();
            }

            // Use the imageType from the Product model to set the correct Content-Type header
            MediaType mediaType = MediaType.parseMediaType(product.getImageType());

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(product.getImageData());
        } catch (ProductNotFoundException e) {
            logger.error("Product not found for id {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error retrieving image for product id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> searchProduct(@RequestParam String keyword) {
        System.out.println("Searching with " + keyword);
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Searched Product...", products));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadProducts(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
        }
        try {
            productService.saveProductsFromFile(file);
            return new ResponseEntity<>("Products uploaded successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to upload products: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}