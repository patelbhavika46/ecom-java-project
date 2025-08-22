package com.divacode.ecom_proj.controller;

import com.divacode.ecom_proj.dto.ProductDTO;
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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse<>(true, "Products fetched", products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable int id) {
        logger.info("Fetching product with id {}", id);
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Product fetched", product));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Object>> addProduct(
            @Valid @RequestPart ProductDTO productDTO,
            @RequestPart MultipartFile imageFile) throws IOException {
        logger.info(String.valueOf(productDTO));
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

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int id) {
        logger.info("product with id {}", id);
        Product product = productService.getProductById(id);
        byte[] imageFile = product.getImageData();
        return ResponseEntity.ok().contentType(MediaType.valueOf(product.getImageType())).body(imageFile);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Product>>> searchProduct(@RequestParam String keyword) {
        System.out.println("Searching with " + keyword);
        List<Product> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Searched Product...", products));
    }
}