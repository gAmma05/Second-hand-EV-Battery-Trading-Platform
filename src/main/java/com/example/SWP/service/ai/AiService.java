package com.example.SWP.service.ai;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiService {

    ChatClient chatClient;

    public double suggestPrice(AiProductRequest request) {
        String prompt = buildPromptSuggestPrice(request);
        String response = chatClient.prompt(prompt).call().content();

        if (response == null || response.isBlank()) {
            return 0.0;
        }

        try {
            System.out.println("AI response (price): " + response);
            String numeric = response.replaceAll("[^0-9.]", "");
            return numeric.isEmpty() ? 0.0 : Double.parseDouble(numeric);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String buildPromptSuggestPrice(AiProductRequest request) {
        if (request.getProductType() == ProductType.VEHICLE) {
            return String.format("""
                You are an assistant that estimates the market price for used electric motorcycles in Vietnam.
                Please estimate a reasonable selling price (in VND) for this vehicle:

                Brand: %s
                Model: %s
                Year: %s
                Mileage: %s km
                Color: %s
                Description: %s

                Respond only with a numeric value (no text or units).
                """,
                    request.getVehicleBrand(),
                    request.getModel(),
                    request.getYearOfManufacture(),
                    request.getMileage(),
                    request.getColor(),
                    request.getDescription()
            );
        } else if (request.getProductType() == ProductType.BATTERY) {
            return String.format("""
                You are an assistant that estimates the market price for used electric vehicle batteries in Vietnam.
                Please estimate a reasonable selling price (in VND) for this battery:

                Battery brand: %s
                Battery type: %s
                Capacity: %s Ah
                Voltage: %s V
                Condition description: %s

                Respond only with a numeric value (no text or units).
                """,
                    request.getBatteryBrand(),
                    request.getBatteryType(),
                    request.getCapacity(),
                    request.getVoltage(),
                    request.getDescription()
            );
        } else {
            return "Please return 0.";
        }
    }
}
