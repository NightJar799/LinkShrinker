package miniLu.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import miniLu.demo.Repository.UserRepository;
import miniLu.demo.dto.RegisterDTO;
import miniLu.demo.dto.UserDto;
import miniLu.demo.entity.User;
import miniLu.demo.util.Mapper;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, Mapper mapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    // public User map(RegisterDTO registerDTO) {
    //     return new User(
    //         registerDTO.getEmail(), 
    //         passwordEncoder.encode(registerDTO.getPassword()),
    //         registerDTO.getName()
    //     );
    // }

    public User addUser(User user) {
        return userRepository.save(user);
    }

    public User registerUser(RegisterDTO registerDTO) {
        registerDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        return userRepository.save(mapper.map(registerDTO, User.class));
    }

    public Optional<UserDto> findUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return Optional.ofNullable(new UserDto(user.getEmail(), user.getPassword(), user.getName()));
    }
}