package org.example.Logs;

import java.io.FileWriter;
import java.io.IOException;

public class QueryLogsService {
    private static final String LOG_FILE = "TinyDB/QueryLogs.txt";
    public static void logQuerySubmission(String query, String queryTimeStamp) {
        StringBuilder logEntry = new StringBuilder();
        logEntry.append("{");
        logEntry.append("\"timestamp\":").append("\""+ queryTimeStamp +"\"").append(",");
        logEntry.append("\"logtype\":\"query_log\",");
        logEntry.append("\"query\":").append("\"" + query + "\"");
        logEntry.append("}");
        try (FileWriter file = new FileWriter(LOG_FILE, true)) {
            file.write(logEntry.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
