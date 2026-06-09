package com.civic.complaint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDto {
    @NotNull
    private Double latitude;
    @NotNull private Double longitude;
    @NotBlank
    private String address;
    private String ward;
    private String lga;
    private String state;
}
