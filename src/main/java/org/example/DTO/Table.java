package org.example.DTO;

import java.util.List;
public class Table {
    private String name;
    private String createTableQuery;
    private List<String> insertQueries;

    public Table(String name, String createTableQuery, List<String> insertQueries) {
        this.name = name;
        this.createTableQuery = createTableQuery;
        this.insertQueries = insertQueries;
    }

    public String getName() {
        return name;
    }

    public String getCreateTableQuery() {
        return createTableQuery;
    }

    public List<String> getInsertQueries() {
        return insertQueries;
    }
}
