package com.example.SWP.service.seller;

import com.example.SWP.entity.PriorityPackage;
import com.example.SWP.repository.PriorityPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class PriorityPackageService {

    PriorityPackageRepository priorityPackageRepository;

    public List<PriorityPackage> getAllPriorityPackages() {
        return priorityPackageRepository.findAll();
    }
}
