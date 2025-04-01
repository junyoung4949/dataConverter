package entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Excel {

    public Excel(String location, String password) {
        this.location = location;
        this.password = password;
    }

    private String location;
    private String password;
}
