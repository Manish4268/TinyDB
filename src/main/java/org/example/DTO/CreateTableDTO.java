package org.example.DTO;

import java.util.List;
import java.util.Map;

public class CreateTableDTO extends QueryDTO {
    private String tableName;

    public CreateTableDTO(String query, String tableName, List<Map<String, String>> tableStructure,String primary_key,String foreign_key) {
        super(query);
        this.tableName = tableName;
        this.TableStructure = tableStructure;
        this.primary_key = primary_key;
        this.foreign_key = foreign_key;
    }

    private String primary_key;
    private String foreign_key;

    public String getPrimary_key() {
        return primary_key;
    }

    public void setPrimary_key(String primary_key) {
        this.primary_key = primary_key;
    }

    public String getForeign_key() {

        return foreign_key;
    }

    public void setForeign_key(String foreign_key) {
        this.foreign_key = foreign_key;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private List<Map<String, String>>  TableStructure;

    public List<Map<String, String>> getTableStructure() {
        return TableStructure;
    }

    public void setTableStructure(List<Map<String, String>> tableStructure) {
        TableStructure = tableStructure;
    }
}
