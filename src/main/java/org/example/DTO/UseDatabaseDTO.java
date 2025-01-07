package org.example.DTO;

public class UseDatabaseDTO extends QueryDTO {
    private String databaseName;

    public UseDatabaseDTO(String query, String databaseName) {
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
