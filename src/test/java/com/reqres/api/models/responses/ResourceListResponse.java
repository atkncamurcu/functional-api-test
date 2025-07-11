package com.reqres.api.models.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.reqres.api.models.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceListResponse {
    private Integer page;
    private Integer per_page;
    private Integer total;
    private Integer total_pages;
    private List<Resource> data;
}
