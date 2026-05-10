package miniLu.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Link {
    private Long id;
    private String link;
    private String shortLink;
}
