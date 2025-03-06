package com.group16.uno;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController

public class HelloController {
    @GetMapping("/")
    public String index() {
        return "Hello world!";
    }

}
