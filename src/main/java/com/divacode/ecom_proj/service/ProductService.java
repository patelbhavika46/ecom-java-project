package com.divacode.ecom_proj.service;

import com.divacode.ecom_proj.dto.ProductDTO;
import com.divacode.ecom_proj.exception.ProductNotFoundException;
import com.divacode.ecom_proj.model.Product;
import com.divacode.ecom_proj.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    ProductRepo repo;

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById(int id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    public Product addProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setDesc(productDTO.getDesc());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setQuantity(productDTO.getQuantity());
        product.setReleaseDate(productDTO.getReleaseDate());
        product.setIsAvailable(productDTO.getIsAvailable());

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
            product.setImageData(imageFile.getBytes());
        }

        return repo.save(product);
    }

    public Product updateProduct(int id, ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setDesc(productDTO.getDesc());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setQuantity(productDTO.getQuantity());
        product.setIsAvailable(productDTO.getIsAvailable());

        if (imageFile != null && !imageFile.isEmpty()) {
            product.setImageName(imageFile.getOriginalFilename());
            product.setImageType(imageFile.getContentType());
            product.setImageData(imageFile.getBytes());
        }
        return repo.save(product);
    }

    public void deleteProduct(int id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        repo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return repo.searchProducts(keyword);
    }
}