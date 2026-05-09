package miniLu.demo.control;

import miniLu.demo.dto.AuthDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Slf4j
@Controller
public class AuthController {
    @GetMapping("/auth")
    public String homePage(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("userAuth", new AuthDTO());

        if (error != null) {
            model.addAttribute("errorMessage", "Неверный логин или пароль");
        }

        log.debug("Отображение главной аутенфикации");
        return "auth";
    }
}