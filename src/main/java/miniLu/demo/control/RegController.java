package miniLu.demo.control;

import miniLu.demo.dto.RegisterDTO;
import miniLu.demo.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Slf4j
@Controller
public class RegController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    public RegController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/reg")
    public String regPage(Model model) {
        model.addAttribute("reg", new RegisterDTO());
        log.debug("Отображение страницы регистрации");
        return "registry";
    }

    @PostMapping("/reg")
    public String registry(@ModelAttribute("reg") RegisterDTO registerDTO, Model model) {
        log.debug("Попытка Регистрации");
        if (registerDTO.getEmail().split("@").length != 2) {
            model.addAttribute("emailError", true);
            return "registry";
        }
        if (!registerDTO.getPassword().equals(registerDTO.getPasswordCheck())) {
            model.addAttribute("passwordError", true);
            return "registry";
        }
        redisTemplate.opsForList().leftPush("newUsers", registerDTO.getEmail());
        System.out.println(userService.addUser(userService.map(registerDTO)));
        log.debug("Попытка Регистрации успешна");
        return "redirect:/auth";
    }
}
