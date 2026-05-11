package com.ruoyi.web.controller.bitable;

import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;

@RestController
@RequestMapping("/api/bitable/test")
@Anonymous
public class BitableTestController
{
    @GetMapping("/hello")
    public String hello()
    {
        return "Hello from Bitable!";
    }
}
