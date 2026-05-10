package miniLu.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.Repository.UserRepository;
import miniLu.demo.dto.RegisterDTO;
import miniLu.demo.entity.User;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User map(RegisterDTO registerDTO) {
        return new User(
            registerDTO.getEmail(), 
            passwordEncoder.encode(registerDTO.getPassword()),
            registerDTO.getName()
        );
    }

    public User addUser(User user) {
        return userRepository.save(user);
    }
}