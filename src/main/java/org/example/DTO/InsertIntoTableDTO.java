package org.example.DTO;

import java.util.List;

public class InsertIntoTableDTO extends QueryDTO {
    private List<String> values;

    private String tableName;

    public InsertIntoTableDTO(String query, List<String> values, String tableName) {
        super(query);
        this.values = values;
        this.tableName = tableName;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
