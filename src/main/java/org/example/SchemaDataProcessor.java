package org.example;

import org.example.DTO.Table;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SchemaDataProcessor
{
    public static List<Table> processFolder(String folderPath) throws IOException {
        List<Table> tables = new ArrayList<>();
        File folder = new File(folderPath);
        File[] schemaFiles = folder.listFiles((dir, name) -> name.endsWith("_schema"));

        if (schemaFiles == null) {
            System.out.println("No schema files found in the folder.");
            return tables;
        }

        for (File schemaFile : schemaFiles) {
            String baseName = schemaFile.getName().replace("_schema", "");
            File dataFile = new File(folderPath + "/" + baseName);

            if (dataFile.exists()) {
                List<String> schemaLines = readLinesFromFile(schemaFile.getPath());
                List<String[]> dataLines = readDataFromFile(dataFile.getPath());

                if (!schemaLines.isEmpty() && !dataLines.isEmpty()) {
                    String tableName = baseName;
                    String createTableQuery = generateCreateTableQuery(tableName, schemaLines);
                    List<String> insertQueries = generateInsertQueries(tableName, dataLines);
                    tables.add(new Table(tableName, createTableQuery, insertQueries));
                } else {
                    System.out.println("Schema or data file for " + baseName + " is empty.");
                }
            } else {
                System.out.println("No matching data file found for " + baseName + "_schema.txt");
            }
        }

        return tables;
    }


    private static List<String> readLinesFromFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static List<String[]> readDataFromFile(String filePath) throws IOException {
        List<String[]> dataLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\|");
                dataLines.add(values);
            }
        }
        return dataLines;
    }

    public static String generateCreateTableQuery(String tableName, List<String> schemaLines) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(tableName);
        sb.append(" (");

        List<String> columnDefinitions = new ArrayList<>();
        List<String> primaryKeyColumns = new ArrayList<>();
        List<String> foreignKeyConstraints = new ArrayList<>();

        // First line: Column definitions
        if (!schemaLines.isEmpty()) {
            String[] columns = schemaLines.get(0).split("\\|");
            for (String column : columns) {
                columnDefinitions.add(column.trim());
            }
        }

        // Second line: PRIMARY KEY
        if (schemaLines.size() > 1) {
            String[] pkColumns = schemaLines.get(1).split("=");
            if(pkColumns.length > 1) {
                primaryKeyColumns.add(pkColumns[1].trim());

            }

        }

        // Third line: FOREIGN KEY
        if (schemaLines.size() > 2) {
            String[] fkParts = schemaLines.get(2).split("=");
            if (fkParts.length == 2) {
                String[] part = fkParts[1].split("\\|");
                if(part.length > 1)
                {
                    foreignKeyConstraints.add("FOREIGN KEY (" + part[0].trim() + ") REFERENCES " + part[1] + "(" + part[0].trim() + ")");
                }

            }
        }

        // Append column definitions
        sb.append(String.join(", ", columnDefinitions));

        // Append PRIMARY KEY if defined
        if (!primaryKeyColumns.isEmpty()) {
            sb.append(", PRIMARY KEY (");
            sb.append(String.join(", ", primaryKeyColumns));
            sb.append(")");
        }

        // Append FOREIGN KEY constraints if defined
        if (!foreignKeyConstraints.isEmpty()) {
            sb.append(", ");
            sb.append(String.join(", ", foreignKeyConstraints));
        }

        sb.append(");");

        return sb.toString();
    }

    public static List<String> generateInsertQueries(String tableName, List<String[]> dataLines) {
        List<String> insertQueries = new ArrayList<>();
        for (int i = 1; i < dataLines.size(); i++) { // Start from the second line
            String[] values = dataLines.get(i);
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            sb.append(tableName);
            sb.append(" VALUES (");

            for (String value : values) {
                sb.append("'");
                sb.append(value.trim());
                sb.append("', ");
            }
            sb.delete(sb.length() - 2, sb.length()); // Remove last comma and space
            sb.append(");");
            insertQueries.add(sb.toString());
        }
        return insertQueries;
    }

    public static void writeSQLToFile(List<Table> tables, String outputFilePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
            for (Table table : tables) {
                writer.write("-- SQL Dump for " + table.getName() + "\n");
                writer.write(table.getCreateTableQuery() + "\n\n");

                for (String insertQuery : table.getInsertQueries()) {
                    writer.write(insertQuery + "\n");
                }

                writer.write("\n");
            }
        }
    }
}
