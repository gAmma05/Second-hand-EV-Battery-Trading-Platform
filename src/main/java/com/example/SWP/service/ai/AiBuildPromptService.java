package com.example.SWP.service.ai;

import com.example.SWP.dto.request.user.ai.AiProductRequest;
import com.example.SWP.enums.ProductType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class AiBuildPromptService {

    public String buildPromptSuggestPrice(AiProductRequest request) {
        if (request.getProductType() == ProductType.VEHICLE) {
            return String.format("""
                            You are an expert in Vietnam's second-hand electric motorcycle market.
                            Your task is to **estimate the realistic resale price (in Vietnamese Dong)** for a used electric motorcycle.
                            
                            Consider the following:
                            - Popular electric motorcycle brands in Vietnam (VinFast, Yadea, Pega, Dat Bike, Dibao, Detech, Anbico…)
                            - Year of manufacture and mileage (older or high mileage = lower price)
                            - Condition from description and color (scratches, battery condition, performance)
                            - Typical depreciation rate of electric motorcycles in Vietnam: 10–25%% per year
                            
                            Electric Motorcycle:
                            Brand: %s
                            Model: %s
                            Year: %s
                            Mileage: %s km
                            Color: %s
                            Description: %s
                            
                            Respond ONLY with a single numeric value in VND (no words, no symbols).
                            """,
                    request.getVehicleBrand(),
                    request.getModel(),
                    request.getYearOfManufacture(),
                    request.getMileage(),
                    request.getColor(),
                    request.getDescription()
            );
        }

        else {
            return String.format("""
                            You are an expert in Vietnam's used electric motorcycle battery market.
                            Your task is to **estimate the fair resale price (in Vietnamese Dong)** for this used motorcycle battery.
                            
                            Consider:
                            - Valid battery brands in Vietnam (VinFast, Dat Bike, Anbico, LG, Panasonic, CATL, Lishen…)
                            - Capacity (Ah) and voltage (V) as direct price factors
                            - Age & condition from description
                            - Typical depreciation of 15–30%% per year
                            
                            Battery details:
                            Brand: %s
                            Type: %s
                            Capacity: %s Ah
                            Voltage: %s V
                            Description: %s
                            
                            Respond ONLY with a single numeric value in VND.
                            """,
                    request.getBatteryBrand(),
                    request.getBatteryType(),
                    request.getCapacity(),
                    request.getVoltage(),
                    request.getDescription()
            );
        }
    }

    public String buildPromptValidateProduct(AiProductRequest request) {
        if (request.getProductType() == ProductType.VEHICLE) {
            return String.format("""
            Role: You are a strict Database Auditor for the Vietnamese Electric Motorcycle Market (Year 2025).
            Task: Verify if the following Brand and Model combination exists as a real commercial product.

            Input Data:
            - Brand: "%s"
            - Model: "%s"

            STRICT VALIDATION LOGIC:
            1. NONSENSE FILTER: Reject toys, gibberish, or non-vehicle names.
            2. FUEL TYPE CHECK: Reject gasoline motorcycles (Vision, AirBlade, Wave, SH...) immediately.
            3. CATALOG MATCH: Ensure the Brand officially manufactures this specific Model.

            ---
            ABSOLUTE INSTRUCTION:
            1. RETURN ONLY ONE LINE.
            2. If VALID: Output the single word "Valid" and NOTHING ELSE.
            3. If INVALID: Output "Invalid: [Reason in Vietnamese]".
            ---

            RESPONSE FORMAT:
            - If passed: Return exactly "Valid".
            - If failed: Return "Invalid: [Reason in Vietnamese]".
            
            """,
                    request.getVehicleBrand(),
                    request.getModel()
            );
        }

        return String.format("""
            Role: You are a Technical Safety Inspector for EV Batteries in Vietnam.
            Task: Verify if the Brand and Type describe a legitimate traction battery.

            Input Data:
            - Brand: "%s"
            - Type: "%s"

            STRICT VALIDATION LOGIC:
            1. BRAND CHECK: Must be a real battery/EV brand.
            2. TYPE CHECK: Must be a valid spec/model.
            3. PAIRING CHECK: Brand must produce this Type.

            ---
            ABSOLUTE INSTRUCTION:
            1. RETURN ONLY ONE LINE.
            2. If VALID: Output the single word "Valid" and NOTHING ELSE.
            3. If INVALID: Output "Invalid: [Reason in Vietnamese]".
            ---
            
            RESPONSE FORMAT:
            - If passed: Return exactly "Valid".
            - If failed: Return "Invalid: [Reason in Vietnamese]".
            """,
                request.getBatteryBrand(),
                request.getBatteryType()
        );
    }

    public String buildPromptCompare(AiProductRequest r1, AiProductRequest r2) {
        if (r1.getProductType() != r2.getProductType()) {
            return "Cả hai bài đăng phải cùng loại sản phẩm để so sánh.";
        }

        if (r1.getProductType() == ProductType.VEHICLE) {
            return String.format("""
                            You are an expert in Vietnam's electric motorcycle market.
                            Compare these two electric motorcycles (max 5 sentences and only using Vietnamese), focusing on:
                            - Year
                            - Mileage
                            - Brand reputation
                            - Overall value
                            
                            Respond concisely and conclude which is the better deal.
                            
                            Motorcycle 1:
                            Brand: %s, Model: %s, Year: %s, Color: %s, Mileage: %s km
                            Description: %s
                            
                            Motorcycle 2:
                            Brand: %s, Model: %s, Year: %s, Color: %s, Mileage: %s km
                            Description: %s
                            """,
                    r1.getVehicleBrand(), r1.getModel(), r1.getYearOfManufacture(), r1.getColor(), r1.getMileage(), r1.getDescription(),
                    r2.getVehicleBrand(), r2.getModel(), r2.getYearOfManufacture(), r2.getColor(), r2.getMileage(), r2.getDescription()
            );
        }

        return String.format("""
                        You are an expert in electric motorcycle batteries in Vietnam.
                        Compare these two batteries (max 5 sentences only using Vietnamese), focusing on:
                        - Capacity
                        - Voltage
                        - Brand reputation
                        - Durability and value
                        
                        Respond concisely and conclude which is the better choice.
                        
                        Battery 1:
                        Brand: %s, Type: %s, Capacity: %s Ah, Voltage: %s V
                        Description: %s
                        
                        Battery 2:
                        Brand: %s, Type: %s, Capacity: %s Ah, Voltage: %s V
                        Description: %s
                        """,
                r1.getBatteryBrand(), r1.getBatteryType(), r1.getCapacity(), r1.getVoltage(), r1.getDescription(),
                r2.getBatteryBrand(), r2.getBatteryType(), r2.getCapacity(), r2.getVoltage(), r2.getDescription()
        );
    }
}
