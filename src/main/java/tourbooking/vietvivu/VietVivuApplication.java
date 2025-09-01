package tourbooking.vietvivu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import tourbooking.vietvivu.entity.User;


import java.time.LocalDate;

@SpringBootApplication
public class VietVivuApplication {

    public static void main(String[] args) {

        SpringApplication.run(VietVivuApplication.class, args);

    }

}
