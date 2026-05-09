package miniLu.demo.dto;


import miniLu.demo.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterDTO {
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @UniqueEmail(message = "Email уже используется")
    private String email;
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
    @NotBlank(message = "Подтверждение пароля не может быть пустым")
    private String passwordCheck;
    private String name;
}
