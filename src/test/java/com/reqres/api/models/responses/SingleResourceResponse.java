package com.reqres.api.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reqres.api.models.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleResourceResponse {
    private Resource data;
}
