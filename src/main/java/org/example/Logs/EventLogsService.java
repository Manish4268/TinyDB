package org.example.Logs;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class EventLogsService {
    static final String LOG_FILE = "TinyDB/EventLogs.txt";

    public static void logCreateTable(String newTableName,String userName) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logEntry.append("\"logtype\":\"create_table\",");
        logEntry.append("\"tablename\":").append("\"" + newTableName + "\"").append(",");
        logEntry.append("\"user\":").append("\"" + userName + "\"").append("");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logDropTable(String tableName,String userName) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logEntry.append("\"logtype\":\"drop_table\",");
        logEntry.append("\"tablename\":").append("\"" + tableName + "\"").append(",");
        logEntry.append("\"user\":").append("\"" + userName + "\"").append("");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void logCreateDatabase(String newDatabaseName,String userName) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logEntry.append("\"logtype\":\"create_database\",");
        logEntry.append("\"databasename\":").append("\"" + newDatabaseName + "\"").append(",");
        logEntry.append("\"user\":").append("\"" + userName + "\"").append("");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void logTransactionStarted(String databaseName , String userName){
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("{");
        logBuilder.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logBuilder.append("\"logtype\":\"start_transaction_log\",");
        logBuilder.append("\"databasename\":").append("\"" + databaseName + "\"").append(",");
        logBuilder.append("\"user\":").append("\"" + userName + "\"").append("");

        logBuilder.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logBuilder.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logTransactionCommited(String databaseName , String userName) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("{");
        logBuilder.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logBuilder.append("\"logtype\":\"transaction_commited_log\",");
        logBuilder.append("\"databasename\":").append("\"" + databaseName + "\"").append(",");
        logBuilder.append("\"user\":").append("\"" + userName + "\"").append("");
        logBuilder.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logBuilder.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logTransactionRollback(String databaseName , String userName) {
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("{");
        logBuilder.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logBuilder.append("\"logtype\":\"transaction_rollback_log\",");
        logBuilder.append("\"databasename\":").append("\"" + databaseName + "\"").append(",");
        logBuilder.append("\"user\":").append("\"" + userName + "\"").append("");
        logBuilder.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logBuilder.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void logErrorMessage(String errorMessage,String userName){
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("{");
        logBuilder.append("\"timestamp\":").append("\"" + Instant.now().toString() + "\"").append(",");
        logBuilder.append("\"logtype\":\"error_log\",");
        logBuilder.append("\"error_message\":").append("\"" + errorMessage + "\"").append(",");
        if(userName!=null){
            logBuilder.append("\"user\":").append("\"" + userName + "\"").append("");
        }
        logBuilder.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logBuilder.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
