package com.dbmatrix.pipeline.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableInfo {
    private String tableName;
    private String schemaName;
    private String databaseName;
}


