package top.decade.boot.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.decade.boot.util.Register;

@RestController
public class MainController {

    @RequestMapping("test")
    public String test( ){

        return "hello test";
    }

}
