package tir.alex.doors.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tir.alex.doors.entity.User;
import tir.alex.doors.repo.UserRepository;
import tir.alex.doors.service.UserService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserRepository userRepository;

    @Override
    public User findByPhoneNumber(String phoneNumber){
        return repository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    @Override
    public User createUser(String phoneNumber) {
        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setCreatedAt(LocalDate.now());
        return userRepository.save(user);
    }
}
