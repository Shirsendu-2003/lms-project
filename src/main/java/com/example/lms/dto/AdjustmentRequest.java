// com.example.lms.dto.AdjustmentRequest.java
package com.example.lms.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustmentRequest {
             // keep for backwards compatibility (optional)
             private Long id;;
    private String adjustedType;    // the OS-chosen leave type to cut from (EL/PL/ML/...)
    private Integer adjustedDays;   // number of days to cut from adjustedType
}
