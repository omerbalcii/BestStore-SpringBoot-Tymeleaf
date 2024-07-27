package com.beststore.beststore.Controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.util.List;

import com.beststore.beststore.Model.Product;
import com.beststore.beststore.Model.ProductDto;
import com.beststore.beststore.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


import jakarta.validation.Valid;


@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository repo;

    @GetMapping({"", "/"})
    public ModelAndView showProductsList() {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        ModelAndView mv = new ModelAndView("products/index.html");
        mv.addObject("products", products);

        return mv;
    }

    @GetMapping("/create")
    public ModelAndView showCreatePage(Model model) {
        ModelAndView mv = new ModelAndView("products/CreateProduct.html");
        ProductDto productDto = new ProductDto();
        mv.addObject(productDto);
        return mv;
    }

    @PostMapping("/create")
    public ModelAndView createProduct(@Valid @ModelAttribute ProductDto productDto, BindingResult result) {
        ModelAndView mvFailure = new ModelAndView("products/CreateProduct.html");
        ModelAndView mvRedirect = new ModelAndView("redirect:/products");
        if(productDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("productDto", "imageFile", "The image file is required"));
        }

        if(result.hasErrors()) {
            return mvFailure;
        }

        // to save image to the server
        MultipartFile image = productDto.getImageFile();
        Date createdAt = new Date(2024, 3, 2);
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            try(InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createdAt);
        product.setImageFileName(storageFileName);

        repo.save(product);

        return mvRedirect;
    }

    @GetMapping("/edit")
    public ModelAndView showEditPage(@RequestParam int id) {
        ModelAndView mv = new ModelAndView("products/EditProduct.html");
        ModelAndView mvRedirect = new ModelAndView("redirect:/products");

        try {
            Product product = repo.findById(id).get();
            mv.addObject("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            productDto.setDescription(product.getDescription());
            mv.addObject("productDto", productDto);

        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
            return mvRedirect;
        }


        return mv;
    }

    // 47:37 - update products (post)
    @PostMapping("/edit")
    public ModelAndView updateProduct(@RequestParam int id, @Valid @ModelAttribute ProductDto productDto, BindingResult result) {
        ModelAndView mv = new ModelAndView("redirect:/products");
        ModelAndView mvEdit = new ModelAndView("products/EditProduct");

        try {
            Product product = repo.findById(id).get();
            mv.addObject("product", product);

            if(result.hasErrors()) {
                return mvEdit;
            }

            // check if new image is there(i.e, updated)
            if (!productDto.getImageFile().isEmpty()) {
                // delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // save the new image
                MultipartFile image = productDto.getImageFile();
                Date createdAt = new Date(2024, 4, 4);
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

                try(InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);

        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }


        return mv;

    }

    //to delete a product
    @GetMapping("/delete")
    public ModelAndView deleteProduct(@RequestParam int id) {
        ModelAndView mv = new ModelAndView("redirect:/products");

        try {
            Product product = repo.findById(id).get();

            Path imagePath = Paths.get("public/images/" + product.getImageFileName());
            //delete the product image - before deleting the obj from database
            try {
                Files.delete(imagePath);
            } catch(Exception ex) {
                System.out.println("Exception: " + ex.getMessage());
            }

            //delete the product
            repo.delete(product);

        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }


        return mv;
    }


}





