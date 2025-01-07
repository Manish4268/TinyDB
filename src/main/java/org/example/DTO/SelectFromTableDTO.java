package org.example.DTO;

import java.util.List;

public class SelectFromTableDTO extends QueryDTO {
    private List<String> columns;
    private String tableName;
    private String condition;

    public SelectFromTableDTO(String query, List<String> columns, String tableName, String condition) {
        super(query);
        this.columns = columns;
        this.tableName = tableName;
        this.condition = condition;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
