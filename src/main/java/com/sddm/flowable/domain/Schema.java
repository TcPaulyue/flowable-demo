package com.sddm.flowable.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schema {
    private String id;
    private JSONObject schemaContent;
    private Status status;

    public Schema(JSONObject schemaContent) {
        this.schemaContent = schemaContent;
    }

    @Override
    public String toString() {
        return String.format(
                "Schema[id='%s',schemaContent='%s',schemaStatus='%s']",
                id, schemaContent, status);
    }
}