package com.example.foodsocialproject.services;

import com.example.foodsocialproject.daos.FileUploadUtil;
import com.example.foodsocialproject.entity.Product;
import com.example.foodsocialproject.exception.ResourceNotFoundException;
import com.example.foodsocialproject.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    public List<Product> getList() {
        return productRepository.findAll();
    }

    public void delete(Long id) {
        Optional<Product> result = productRepository.findById(id);
        if (result.isPresent()){
            productRepository.deleteById(id);
        }
        else
            throw new ResourceNotFoundException("Không tìm ID: " + id + "!");
    }

    public Product getProductById(Long id){
        return productRepository.findById(id).orElseThrow();
    }

    public void addProduct(Product product, MultipartFile multipartFile) throws IOException {
        String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        product.setImg(fileName);
        product.setActive(true);
        Product saveProduct = productRepository.save(product);
        String upLoadDir = "product-images/" + saveProduct.getId();
        FileUploadUtil.saveFile(upLoadDir, multipartFile, fileName);
        productRepository.save(product);
    }
    public Page<Product> findPaginated(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        return this.productRepository.findAll(pageable);
    }
    public List<Product> getALlProductsByCategoryId(Long id){
        return productRepository.getAllProductById(id);
    }
    public List<Product> searchProductByName(String keyword){
        return productRepository.searchProductByName(keyword);
    }
    public Product getRandomProduct() {
        long productCount = productRepository.count();
        Random random = new Random();
        long randomProductId = random.nextInt((int) productCount) + 1;
        return productRepository.findById(randomProductId).orElse(null);
    }
}
