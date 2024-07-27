package com.beststore.beststore.Repository;

import com.beststore.beststore.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
