package tir.alex.doors.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tir.alex.doors.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneNumber(String phone);
}
