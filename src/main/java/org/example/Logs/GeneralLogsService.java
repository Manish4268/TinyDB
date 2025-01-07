package org.example.Logs;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class GeneralLogsService {
    private static final String LOG_FILE = "TinyDB/GeneralLogs.txt";
    public static void logQueryExecutionTime(String query, long executionTime) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\""+ Instant.now().toString()+"\"").append(",");
        logEntry.append("\"logtype\":\"query_execution_time\",");
        logEntry.append("\"query\":").append("\"" + query + "\"").append(",");
        logEntry.append("\"execution_time\":").append("\"" + executionTime).append("ms").append("\"");
        logEntry.append("}");
        System.out.println("Query execution time: " + executionTime + "ms");

        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logDatabaseState(Map<String, Integer> dbState){
        StringBuilder stateBuilder = new StringBuilder();
        stateBuilder.append("{\n");
        stateBuilder.append("\"timestamp\":").append("\""+Instant.now().toString()+"\"").append(",");
        stateBuilder.append("\"logtype\":\"database_state\",");
        stateBuilder.append("\"Number of Tables\": \"").append(dbState.size()).append("\",");
        stateBuilder.append("\"tables\": \"\n");

        for (Map.Entry<String, Integer> tableEntry : dbState.entrySet()) {
            stateBuilder.append("Table Name: ").append(tableEntry.getKey()).append("\n");
            stateBuilder.append("Number of Records: ").append(tableEntry.getValue()).append("\n");
        }

        stateBuilder.append("\"");
        stateBuilder.append("}");

        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(stateBuilder.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logUserRegistered(String username){
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\""+ Instant.now().toString()+"\"").append(",");
        logEntry.append("\"logtype\":\"user_registered\",");
        logEntry.append("\"username\":").append("\"" + username + "\"").append("");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logUserLoggedIn(String username){
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\""+ Instant.now().toString()+"\"").append(",");
        logEntry.append("\"logtype\":\"user_logged_in\",");
        logEntry.append("\"username\":").append("\"" + username + "\"").append("");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
