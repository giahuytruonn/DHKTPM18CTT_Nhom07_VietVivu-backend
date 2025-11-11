package tourbooking.vietvivu.configuration;

import java.util.Properties; // Giữ lại import này vì nó được sử dụng
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    // CÁC GIÁ TRỊ NÀY NÊN ĐƯỢC ĐƯA RA NGOÀI FILE application.properties
    // vàSỬ DỤNG @Value ĐỂ TIÊM VÀO
    private String username = "vothaiduy19092004@gmail.com";
    private String password = "eekw pxom tcwc ozjg";

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}