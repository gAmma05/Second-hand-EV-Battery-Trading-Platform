package com.example.SWP.service.buyer;

import com.example.SWP.dto.request.buyer.UpgradeToSellerRequest;

import com.example.SWP.entity.User;
import com.example.SWP.enums.Role;
import com.example.SWP.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class BuyerService {

    UserRepository userRepository;


}
