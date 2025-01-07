package org.example.DTO;

import java.util.Map;

public class CreateDatabaseDTO extends QueryDTO
{
    private String databaseName;

    public CreateDatabaseDTO(String query, String databaseName) {
        super(query);
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }


}
