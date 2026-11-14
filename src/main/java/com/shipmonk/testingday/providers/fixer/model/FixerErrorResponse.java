package com.shipmonk.testingday.providers.fixer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FixerErrorResponse {

    private Integer code;
    private String type;
    private String info;

}
