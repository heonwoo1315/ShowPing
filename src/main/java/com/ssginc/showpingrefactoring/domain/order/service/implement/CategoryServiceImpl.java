package com.ssginc.showpingrefactoring.domain.order.service.implement;

import com.ssginc.showpingrefactoring.domain.product.entity.Category;
import com.ssginc.showpingrefactoring.domain.order.repository.CategoryRepository;
import com.ssginc.showpingrefactoring.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}