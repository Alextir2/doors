package tir.alex.doors.service;

import tir.alex.doors.entity.User;

public interface UserService {

    User findByPhoneNumber(String phoneNumber);

    User createUser(String phoneNumber);
}
