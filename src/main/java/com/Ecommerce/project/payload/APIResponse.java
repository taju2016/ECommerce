package com.Ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Normalized;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse {
    public String message;
    public boolean status;
}
