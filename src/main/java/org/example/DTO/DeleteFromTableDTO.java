package org.example.DTO;

public class DeleteFromTableDTO extends QueryDTO {
    private String tableName;
    private String whereCondition;

    public DeleteFromTableDTO(String query, String tableName, String whereCondition) {
        super(query);
        this.tableName = tableName;
        this.whereCondition = whereCondition;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getWhereCondition() {
        return whereCondition;
    }

    public void setWhereCondition(String whereCondition) {
        this.whereCondition = whereCondition;
    }
}
