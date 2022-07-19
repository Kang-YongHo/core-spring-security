package com.corespringsecurity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecurityController {

    @GetMapping("/")
    public String index(){
        return "home";
    }

    @GetMapping("/login-page")
    public String page(){
        return "login-page";
    }

    @GetMapping("/user")
    public String myPage(){
        return "USER ROLE MY PAGE";
    }

    @GetMapping("/admin/pay")
    public String adminPay(){
        return "ADMIN ROLE PAY PAGE";
    }

    @GetMapping("/sys/test")
    public String sysPage(){
        return "ADMIN,SYS ROLE PAGE";
    }
}
