package org.techhub.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.techhub.entity.Item;
import org.techhub.entity.Product;
import org.techhub.entity.Role;
import org.techhub.entity.User;
import org.techhub.repository.ItemRepository;
import org.techhub.repository.ProductRepository;
import org.techhub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default Admin user if not present
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@zestindia.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(admin);
            logger.info("Default ADMIN user initialized: username=admin, password=admin123");
        }

        // Seed default Regular user if not present
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .email("user@zestindia.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.ROLE_USER)
                    .build();
            userRepository.save(user);
            logger.info("Default USER initialized: username=user, password=user123");
        }

        // Seed sample product data if empty
        if (productRepository.count() == 0) {
            Product laptop = Product.builder()
                    .productName("Dell XPS 15 Laptop")
                    .createdBy("admin")
                    .createdOn(LocalDateTime.now())
                    .build();
            Product savedLaptop = productRepository.save(laptop);

            Item laptopItem1 = Item.builder()
                    .product(savedLaptop)
                    .quantity(15)
                    .build();
            Item laptopItem2 = Item.builder()
                    .product(savedLaptop)
                    .quantity(30)
                    .build();
            itemRepository.saveAll(List.of(laptopItem1, laptopItem2));

            Product phone = Product.builder()
                    .productName("Samsung Galaxy S24")
                    .createdBy("admin")
                    .createdOn(LocalDateTime.now())
                    .build();
            Product savedPhone = productRepository.save(phone);

            Item phoneItem = Item.builder()
                    .product(savedPhone)
                    .quantity(50)
                    .build();
            itemRepository.save(phoneItem);

            logger.info("Sample products and items initialized.");
        }
    }
}
