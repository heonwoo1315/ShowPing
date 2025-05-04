package com.ssginc.showpingrefactoring.domain.order.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssginc.showpingrefactoring.domain.product.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}