package com.civic.complaint.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    private double latitude;
    private double longitude;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String ward;

    @Column(length = 100)
    private String lga;

    @Column(length = 100)
    private String state;
}
