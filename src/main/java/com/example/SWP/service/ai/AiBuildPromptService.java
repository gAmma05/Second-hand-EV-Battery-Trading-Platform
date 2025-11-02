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
            You are an expert in Vietnam's second-hand electric vehicle market.
            Your task is to **estimate the realistic resale price (in Vietnamese Dong)** for a used electric car or motorcycle.

            Please consider these factors carefully:
            - Brand and model popularity in Vietnam (e.g., VinFast, Yadea, Pega, Detech, Dibao, Anbico, etc.)
            - Manufacturing year and mileage (older year or high mileage = lower price)
            - Condition based on color and description (e.g., scratches, battery condition, performance)
            - General depreciation rate for electric motorcycles in Vietnam (10–25%% per year)

            If any field seems invalid, inconsistent (e.g., brand not found in Vietnam, year in the future, mileage unrealistic), 
            or the description is unrelated to vehicles, respond only with "0".

            Vehicle details:
            Brand: %s
            Model: %s
            Year: %s
            Mileage: %s km
            Color: %s
            Description: %s

            Respond ONLY with a single numeric value in VND (no text, no unit, no symbol).
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
            You are an expert in Vietnam's used electric vehicle battery market.
            Your job is to **estimate the fair resale price (in Vietnamese Dong)** for this used EV battery.

            Consider the following:
            - Battery brand and model common in Vietnam (e.g., VinFast, Pega, Yadea, LFP, Lithium-ion, etc.)
            - Capacity (Ah) and voltage (V) directly affect price.
            - Age and condition from description (newer or well-kept = higher price).
            - Typical depreciation of 15–30%% per year for used batteries.

            If any information appears incorrect, illogical (e.g., invalid brand, unrealistic specs, non-battery description), 
            or unrelated to batteries, respond only with "0".

            Battery details:
            Brand: %s
            Type: %s
            Capacity: %s Ah
            Voltage: %s V
            Description: %s

            Respond ONLY with a single numeric value in VND (no text, no unit, no symbol).
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

    public String buildPromptValidateProduct(AiProductRequest request) {
        if (request.getProductType() == ProductType.VEHICLE) {
            return String.format("""
        You are a strict and knowledgeable validator for electric motorcycle listings in Vietnam.
        Your task is to verify whether the following listing describes a *realistic, existing electric motorcycle*
        that could actually be sold on the Vietnamese market (as of 2025).

        Brand: %s
        Model: %s
        Year: %s
        Color: %s
        Mileage: %s km
        Description: %s

        Validation rules:
        - Accept only real brands and models available in Vietnam (e.g., VinFast, Yadea, Pega, Dat Bike, Anbico, Detech, MBI, YADEA).
        - Reject any fake, unknown, or fictional brands (e.g., “SuperSpeed”, “DragonFly”, “SkyBike”).
        - Manufacturing year must be between 2010 and the current year.
        - Mileage must be between 0 and 100,000 km.
        - Reject if description refers to gasoline vehicles, cars, phones, laptops, or unrelated items.
        - Reject immediately if description contains any impossible or unrealistic claims such as:
          “bay”, “fly”, “tự sạc”, “không cần pin”, “năng lượng lượng tử”, “quantum”, “vĩnh cửu”, “chạy vô hạn”, “never charges”, “infinite range”.
          Even if the brand is real or numeric values are valid, the listing must be marked as Invalid.

        Respond *only* with one exact word (no explanation, no punctuation):
        Valid
        Invalid
        """,
                    request.getVehicleBrand(),
                    request.getModel(),
                    request.getYearOfManufacture(),
                    request.getColor(),
                    request.getMileage(),
                    request.getDescription()
            );
        } else {
            return String.format("""
        You are a strict validator for electric vehicle battery listings in Vietnam.
        Your task is to confirm whether this describes a *real, commercially available EV battery*
        suitable for electric scooters or motorcycles in Vietnam (as of 2025).

        Battery Brand: %s
        Battery Type: %s
        Capacity: %s Ah
        Voltage: %s V
        Description: %s

        Validation rules:
        - Accept only well-known EV battery brands: VinFast, CATL, Lishen, LG, Panasonic, BYD, Pylontech, Gotion, Dat Bike, Anbico.
        - Reject unknown or fictional brands (e.g., “SuperPower”, “Quantum”, “TeslaNano”).
        - Voltage must be between 12V and 100V (typical: 48V, 60V, 72V).
        - Capacity must be between 10Ah and 100Ah.
        - Reject immediately if description contains any unrealistic or impossible claims such as:
          “tự sạc”, “năng lượng lượng tử”, “quantum energy”, “never needs charging”, “vĩnh cửu”, “không cần sạc”.
          Even if the brand is real or numeric values are valid, the listing must be marked as Invalid.
        - Reject if unrelated to batteries.

        Respond *only* with one exact word (no explanation, no punctuation):
        Valid
        Invalid
        """,
                    request.getBatteryBrand(),
                    request.getBatteryType(),
                    request.getCapacity(),
                    request.getVoltage(),
                    request.getDescription()
            );
        }
    }

    public String buildPromptCompare(AiProductRequest request1, AiProductRequest request2) {
        if (request1.getProductType() != request2.getProductType()) {
            return "Both posts must have the same product type to compare.";
        }

        if (request1.getProductType() == ProductType.VEHICLE) {
            return String.format("""
        You are an expert in the used electric vehicle market in Vietnam.
        Compare the following two vehicles briefly (max 5 sentences) focusing on:
        - Year of manufacture
        - Mileage
        - Brand reputation
        - Overall value for money

        Respond in natural English, concise and objective.
        Conclude clearly which vehicle is the better deal and why.

        Vehicle 1:
        Brand: %s
        Model: %s
        Year: %s
        Color: %s
        Mileage: %s km
        Description: %s

        Vehicle 2:
        Brand: %s
        Model: %s
        Year: %s
        Color: %s
        Mileage: %s km
        Description: %s
        """,
                    request1.getVehicleBrand(), request1.getModel(), request1.getYearOfManufacture(), request1.getColor(), request1.getMileage(), request1.getDescription(),
                    request2.getVehicleBrand(), request2.getModel(), request2.getYearOfManufacture(), request2.getColor(), request2.getMileage(), request2.getDescription()
            );
        }

        if (request1.getProductType() == ProductType.BATTERY) {
            return String.format("""
        You are an expert in electric vehicle batteries in Vietnam.
        Compare the following two batteries briefly (max 5 sentences), focusing on:
        - Capacity
        - Voltage
        - Brand reputation
        - Durability and value

        Respond concisely in English and clearly conclude which battery is the better choice and why.

        Battery 1:
        Brand: %s
        Type: %s
        Capacity: %s Ah
        Voltage: %s V
        Description: %s

        Battery 2:
        Brand: %s
        Type: %s
        Capacity: %s Ah
        Voltage: %s V
        Description: %s
        """,
                    request1.getBatteryBrand(), request1.getBatteryType(), request1.getCapacity(), request1.getVoltage(), request1.getDescription(),
                    request2.getBatteryBrand(), request2.getBatteryType(), request2.getCapacity(), request2.getVoltage(), request2.getDescription()
            );
        }

        return "Unsupported product type for comparison.";
    }



}
