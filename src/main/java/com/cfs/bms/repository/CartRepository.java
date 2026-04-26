package com.cfs.bms.repository;

import com.cfs.bms.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCartId(String cartId);
    List<Cart> findByUserId(Long userId);
    List<Cart> findByUserIdAndStatus(Long userId, String status);
}
