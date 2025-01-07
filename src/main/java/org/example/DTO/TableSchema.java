package org.example.DTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableSchema
{
    public List<Map<String, String>> columns;

    public String primary_key;

    public String foreign_key;

    public String foreign_key_table;

    public TableSchema() {
        this.columns = new ArrayList<>();
        this.primary_key = null;
        this.foreign_key = null;
        this.foreign_key_table = null;
    }

    public void addColumn(String name, String type) {
        Map<String, String> column = new HashMap<>();
        column.put(name, type);
        columns.add(column);
    }

    @Override
    public String toString() {
        StringBuilder schemaString = new StringBuilder();
        for (Map<String, String> column : columns) {
            for (Map.Entry<String, String> entry : column.entrySet()) {
                schemaString.append(entry.getKey()).append(" ").append(entry.getValue()).append(",");
            }
        }
        return schemaString.length() > 0 ? schemaString.substring(0, schemaString.length() - 1) : "";
    }

    public List<Map<String, String>> getColumns() {
        return columns;
    }
    public TableSchema clone() {
        TableSchema copy = new TableSchema();
        for (Map<String, String> column : this.columns) {
            Map<String, String> columnCopy = new HashMap<>(column);
            copy.columns.add(columnCopy);
        }
        return copy;
    }

    public void setColumns(List<Map<String, String>> columns) {
        this.columns = columns;
    }
}
