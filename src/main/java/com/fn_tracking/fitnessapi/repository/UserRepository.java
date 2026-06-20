package com.fn_tracking.fitnessapi.repository;

import com.fn_tracking.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Tìm kiếm người dùng bằng username (Sử dụng rất nhiều khi làm Spring Security / JWT)
    Optional<User> findByUsername(String username);
    
    // Kiểm tra xem email đã tồn tại trong database chưa (Phục vụ validate lúc Đăng ký)
    Boolean existsByEmail(String email);
    
    // Kiểm tra xem username đã tồn tại chưa (Phục vụ validate lúc Đăng ký)
    Boolean existsByUsername(String username);
}