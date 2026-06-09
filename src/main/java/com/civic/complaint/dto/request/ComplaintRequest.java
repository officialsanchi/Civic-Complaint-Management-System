package com.civic.complaint.dto.request;

import com.civic.complaint.enums.ComplaintStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class ComplaintRequest {


    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @Valid
    @NotNull
    private LocationDto location;

    private List<String> imageUrls = new ArrayList<>();
}
