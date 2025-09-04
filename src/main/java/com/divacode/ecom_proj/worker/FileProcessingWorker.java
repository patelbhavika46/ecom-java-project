package com.divacode.ecom_proj.worker;

import com.divacode.ecom_proj.dto.ProductDTO;
import com.divacode.ecom_proj.model.Product;
import com.divacode.ecom_proj.repository.ProductRepo;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileProcessingWorker {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingWorker.class);

    private static final String REDIS_QUEUE = "file_processing_queue";
    private static final String REDIS_STATUS_PREFIX = "file_status:";
    private static final String UPLOAD_DIR = "uploads/";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductRepo productRepository;

    @Scheduled(fixedRate = 5000) // Poll the queue every 5 seconds
    public void processFile() {
        logger.info("Polling Redis queue for files...");
        String fileId = redisTemplate.opsForList().rightPop(REDIS_QUEUE);

        if (fileId != null) {
            String statusKey = REDIS_STATUS_PREFIX + fileId;
            File fileToProcess = new File(UPLOAD_DIR + fileId + ".csv");

            try {
                logger.info("Processing file: {}", fileId);
                // Update status to PROCESSING
                redisTemplate.opsForHash().put(statusKey, "status", "PROCESSING");

                if (fileToProcess.exists()) {
                    logger.info("File found at: {}", fileToProcess.getAbsolutePath());
                    saveProductsFromFile(fileToProcess);
                    logger.info("Successfully processed and saved data from file: {}", fileId);

                    // Update status to COMPLETED
                    redisTemplate.opsForHash().put(statusKey, "status", "COMPLETED");
                } else {
                    // File not found, update status
                    logger.error("File not found: {}", fileToProcess.getAbsolutePath());
                    redisTemplate.opsForHash().put(statusKey, "status", "FAILED");
                    redisTemplate.opsForHash().put(statusKey, "error", "File not found on server.");
                }
            } catch (Exception e) {
                // Catch any exception during processing and update status to FAILED
                logger.error("Error processing file {}: {}", fileId, e.getMessage(), e);
                redisTemplate.opsForHash().put(statusKey, "status", "FAILED");
                redisTemplate.opsForHash().put(statusKey, "error", "Processing failed: " + e.getMessage());
            } finally {
                // Clean up the file, regardless of success or failure
                deleteFile(fileToProcess);
            }
        }
    }

    private void saveProductsFromFile(File file) throws IOException, CsvException {
        try (Reader reader = new BufferedReader(new FileReader(file))) {
            CsvToBean<ProductDTO> csvToBean = new CsvToBeanBuilder<ProductDTO>(reader)
                    .withType(ProductDTO.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ProductDTO> productDTOs = csvToBean.parse();

            // Convert DTOs to Entities and save
            List<Product> products = productDTOs.stream()
                    .map(this::convertToProductEntity)
                    .collect(Collectors.toList());

            productRepository.saveAll(products);

        } catch (IOException e) {
            // Log and re-throw for better error handling in the main loop
            logger.error("I/O Error during CSV parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Product convertToProductEntity(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDesc(productDTO.getDesc());
        product.setBrand(productDTO.getBrand());
        product.setPrice(productDTO.getPrice());
        product.setCategory(productDTO.getCategory());
        product.setReleaseDate(productDTO.getReleaseDate());
        product.setIsAvailable(productDTO.getIsAvailable());
        product.setQuantity(productDTO.getQuantity());

        // Assuming image data is not in the CSV
        product.setImageName(null);
        product.setImageType(null);
        product.setImageData(null);

        return product;
    }

    private void deleteFile(File file) {
        if (file.exists()) {
            if (file.delete()) {
                logger.info("Successfully deleted file: {}", file.getAbsolutePath());
            } else {
                logger.warn("Failed to delete file: {}", file.getAbsolutePath());
            }
        }
    }
}
