package org.example.DTO;

public class DropTableDTO extends QueryDTO {
    private String tableName;

    public DropTableDTO(String query, String tableName) {
        super(query);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
