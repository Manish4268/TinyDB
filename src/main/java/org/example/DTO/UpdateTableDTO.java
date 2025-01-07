package org.example.DTO;

import java.util.Map;

public class UpdateTableDTO extends QueryDTO {
    private String tableName;
    private Map<String, String> setValues;
    private String condition;

    public UpdateTableDTO(String query, String tableName, Map<String, String> setValues, String condition) {
        super(query);
        this.tableName = tableName;
        this.setValues = setValues;
        this.condition = condition;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getSetValues() {
        return setValues;
    }

    public void setSetValues(Map<String, String> setValues) {
        this.setValues = setValues;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
