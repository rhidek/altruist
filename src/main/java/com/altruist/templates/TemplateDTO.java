package com.altruist.templates;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemplateDTO {
    @JsonProperty("id")
    private String id;
    @JsonProperty("text")
    private String text;
}
