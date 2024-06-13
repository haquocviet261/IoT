package com.petshop.services.imp;

import com.petshop.model.dto.response.ProductResponse;
import com.petshop.model.dto.response.ResponseObject;
import com.petshop.services.interfaces.ProductService;

import com.petshop.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImp implements ProductService {
    @Autowired
    ProductRepository productRepository;

    @Override
    public ResponseEntity<ResponseObject> findProductByCategoryID(Integer page , Integer size, Long category_id) {
        List<ProductResponse> products = new ArrayList<>();
        if (page == null || size == null){
            products = productRepository.findProductByCategory(category_id);
        }else {
            products = productRepository.findProductByCategory(PageRequest.of(page,size),category_id);
        }

        return ResponseEntity.ok(new ResponseObject("OK","List all products",products));
    }

    @Override
    public ResponseEntity<ResponseObject> findRandomProducts(Pageable pageable) {
        List<Object[]> products = productRepository.findRandomProducts(pageable);
        List<ProductResponse> list = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            list.add(fromObjectArray(products.get(i)));
        }
        return ResponseEntity.ok(new ResponseObject("OK","List random top product by paging",list));
    }

    @Override
    public ResponseEntity<ResponseObject> findAll() {
        List<ProductResponse> products = productRepository.getProductAndDiscount();

        return ResponseEntity.ok(new ResponseObject("OK","List all products",products));
    }
    public  ResponseEntity<ResponseObject> findTopSaleProduct(Integer page, Integer size){
        List<ProductResponse> products = productRepository.findTopSaleProduct(PageRequest.of(page,size));

        return ResponseEntity.ok(new ResponseObject("OK","List all products desc",products));

    }

    @Override
    public ResponseEntity<ResponseObject> findProductBySubcategoryId(Integer page, Integer size,Long sub_category_id) {
        List<ProductResponse> products = productRepository.findProductBySubcategoryId(PageRequest.of(page,size),sub_category_id);
        return ResponseEntity.ok(new ResponseObject("OK","List all products by sub_category_id",products));
    }

    public ResponseEntity<ResponseObject> findByProductNameContainingIgnoreCase(Integer page ,Integer size,String name) {
        List<ProductResponse> productList = new ArrayList<>();
        if (page == null || size == null){
            productList = productRepository.findByNameContainingIgnoreCase(name);
        }else {
            productList = productRepository.findByNameContainingIgnoreCase(PageRequest.of(page,size),name);
        }

        if (productList.size()==0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("False","Cannot find product with name: "+name,""));
        }
        return ResponseEntity.ok(new ResponseObject("OK","List name of product",productList));
    }

    public ResponseEntity<ResponseObject> findProductBySubCategoryNameOrProductName(Integer page ,Integer size,String name) {

        List<ProductResponse> productListBySubcategories = new ArrayList<>();
        List<ProductResponse> productListByProductName = new ArrayList<>();
        if (page == null || size == null){
            productListBySubcategories = productRepository.findBySubCategoriesContainingIgnoreCase(name);
            productListByProductName =productRepository.findByNameContainingIgnoreCase(name);
        }else {
            productListByProductName =productRepository.findByNameContainingIgnoreCase(PageRequest.of(page,size),name);
            productListBySubcategories = productRepository.findBySubCategoriesContainingIgnoreCase(PageRequest.of(page,size),name);
        }

        if (productListByProductName.size() !=0){

            return ResponseEntity.ok(new ResponseObject("OK","List of product by Name: "+name,productListByProductName));

        } else if (productListBySubcategories.size() !=0) {

            return ResponseEntity.ok(new ResponseObject("OK","List of  by Subcategory: "+name,productListBySubcategories));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ResponseObject("False","Cannot find product with name: "+name,""));

    }

    private static ProductResponse fromObjectArray(Object[] row) {
        Long productId = (Long) row[0];
        Long category_id =(Long) row[1];
        Long subCategoryId = (Long) row[2];
        String productName = (String) row[3];
        int quantity = (int) row[4];
        double price = (double) row[5];
        String description = (String) row[6];
        String productImage = (String) row[7];
        double discountValue = (row[8] != null) ? (double) row[8] : 0.0;

        return new ProductResponse(productId,category_id, subCategoryId, productName, quantity, price, description, productImage, discountValue);
    }

    public ResponseEntity<ResponseObject> findProductBySearchFilter(Integer page, Integer size, String sortOrder, Double minPrice, Double maxPrice,Long selected_category_id, String searchValue) {

            if (minPrice != null || maxPrice != null || page != null || size != null){

                List<ProductResponse> listProductByName = productRepository.findByName(PageRequest.of(page,size),searchValue,sortOrder,minPrice,maxPrice,selected_category_id);
                List<ProductResponse> listProductBySubcategory = productRepository.findBySubCategories(PageRequest.of(page,size),searchValue,sortOrder,minPrice,maxPrice,selected_category_id);
                if (!listProductByName.isEmpty()){
                        return ResponseEntity.ok(new ResponseObject("OK","List product By Name",listProductByName));
                }
                if (!listProductBySubcategory.isEmpty()){
                        return ResponseEntity.ok(new ResponseObject("OK","List product By Name",listProductBySubcategory));
                }
            }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseObject("False","Cannot find product with name: "+searchValue,null));
    }
    public ResponseEntity<ResponseObject> findById(Long product_id){
        return  ResponseEntity.ok(new ResponseObject("OK","Product by id"+product_id,productRepository.findByProduct_id(product_id)));
    }

}
