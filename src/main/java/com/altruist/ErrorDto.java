package com.altruist;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorDto {
    private ErrorCodes code;
    private String message;
}
