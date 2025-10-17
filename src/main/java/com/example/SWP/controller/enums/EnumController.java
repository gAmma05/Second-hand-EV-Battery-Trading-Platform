package com.example.SWP.controller.enums;

import com.example.SWP.enums.ProductType;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/enums")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class EnumController {

    @GetMapping("/product-types")
    public List<String> getProductTypes() {
        return Arrays.stream(ProductType.values())
                .map(Enum::name)
                .toList();
    }


}
