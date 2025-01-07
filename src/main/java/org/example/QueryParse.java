package org.example;

import org.example.DTO.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QueryParse
{
    String query;

    int QueryType = 0;

    private static final String CreateTableSQL_REGEX = "CREATE TABLE [a-zA-Z_][a-zA-Z0-9_]*\\s?\\((\\s?(?:[a-zA-Z][a-zA-Z0-9_]*\\s(?:int|float|double|boolean|varchar\\(\\d+\\)))(?:\\s?,)?)+\\s?\\)";

    private static final String InsertTableSQL_REGEX = "INSERT INTO [a-zA-Z_][a-zA-Z0-9_]* values\\s?\\(\\s?((?:[a-zA-Z0-9_]*)\\s?(?:,\\s?)?)+\\)";

    private static final String UpdateTableSQL_REGEX = "UPDATE [a-zA-Z_][a-zA-Z0-9_]* +SET ((?:[a-zA-Z_][a-zA-Z0-9_]*\\s?=\\s?[a-zA-Z0-9_]+(?:,\\\\s*)?)*) WHERE +(.+).";

    private static final String DeleteTableSQL_REGEX = "DELETE +(FROM|FROM +)?([a-zA-Z_][a-zA-Z0-9_]*) *(WHERE|AND|OR) *(.*)";

    private static final String StartTransactionSQL_REGEX = "start +transaction";


    private static final String selectPattern = "SELECT\\s+.+\\s+FROM\\s+\\w+\\s*(WHERE\\s+.+)?";

    private static final Pattern InsertTablePattern = Pattern.compile(InsertTableSQL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final Pattern CreateTablePattern = Pattern.compile(CreateTableSQL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final Pattern SelectTablePattern = Pattern.compile(selectPattern, Pattern.CASE_INSENSITIVE);

    private static final Pattern UpdateTablePattern = Pattern.compile(UpdateTableSQL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final Pattern DeleteTablePattern = Pattern.compile(DeleteTableSQL_REGEX, Pattern.CASE_INSENSITIVE);

    private static final Pattern StartTransactionPattern = Pattern.compile(StartTransactionSQL_REGEX, Pattern.CASE_INSENSITIVE);


    public QueryParse(String query) {
        this.query = query.toLowerCase();
        this.QueryType = determineQueryType(query);
    }

    public String getType() {
        return query;
    }


    private int determineQueryType(String query) {
        String queryLower = query.trim().toLowerCase();

        if (queryLower.startsWith("create database")) {
            return 1;
        } else if (queryLower.startsWith("use")) {
            return 2;
        } else if (queryLower.startsWith("create table")) {
            return 3;
        } else if (queryLower.startsWith("insert")) {
            return 4;
        } else if (queryLower.startsWith("select")) {
            return 5;
        } else if (queryLower.startsWith("update")) {
            return 6;
        }
        else if (queryLower.startsWith("delete")) {
            return 7;
        }  else if (queryLower.startsWith("drop table")) {
            return 8;
        } else if (queryLower.startsWith("start transaction")) {
            return 9;
        }else if (queryLower.startsWith("commit")) {
            return 10;
        }
        else if (queryLower.startsWith("rollback")) {
            return 11;
        }else {
            return 0;
        }
    }

    public QueryDTO parseQuery()
    {
        switch (QueryType)
        {
            case 1:
                return parseCreateDatabaseQuery();
            case 2:
                return parseUseDatabaseQuery();
            case 3:
                return parseCreateTableQuery();
            case 4:
                return parseInsertIntoTableQuery();
            case 5:
                return parseSelectFromTableQuery();
            case 6:
                return parseUpdateTableQuery();
            case 7:
                return parseDeleteTableQuery();
            case 8:
                return parseDropTableQuery();
            case 9:
                return new StartTransactionDTO(query);
            case 10:
                return parseCommitQuery();
            case 11:
                return parseRollbackQuery();
            default:

                return null;
        }
    }


    private QueryDTO parseSelectFromTableQuery() {
        if (!checkQuotes(query))
        {
            return new SelectFromTableDTO(query, null, null, null);
        }

        query = query.replaceAll("\"", "");

        if (!validateSQL(query, SelectTablePattern)) {
            return new SelectFromTableDTO(query, null, null, null);
        }

        query = query.replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\s+", " ");

        String[] parts = query.trim().split("\\s+", 4);

        String columnsPart = parts[1];
        String tableName = parts[3].split("where")[0].trim();
        String condition = parts.length > 3 && parts[3].contains("where") ? parts[3].split("where")[1].trim() : null;

        return new SelectFromTableDTO(query, Arrays.asList(columnsPart.split(",")), tableName, condition);
    }

    private QueryDTO parseUpdateTableQuery() {
        if (!checkQuotes(query)) {
            return new UpdateTableDTO(query, null, null, null);
        }

        query = query.replaceAll("\"", "");

        if (!validateSQL(query, UpdateTablePattern)) {
            return new UpdateTableDTO(query, null, null, null);
        }

        query = query.replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\s+", " ");

        String[] parts = query.trim().split("\\s+", 4);
        String tableName = parts[1];

        String setPart = parts[3].split("where")[0].trim();
        String condition = parts[3].split("where")[1].trim();

        String[] setValuesArray = setPart.split(",");
        Map<String, String> setValues = Arrays.stream(setValuesArray)
                .map(String::trim)
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));

        return new UpdateTableDTO(query, tableName, setValues, condition);
    }

    private QueryDTO parseInsertIntoTableQuery() {

        if(!checkQuotes(query))
        {
            return new InsertIntoTableDTO(query, null,null);
        }

        query = query.replaceAll("\"", "");

        if(!validateSQL(query, InsertTablePattern))
        {
            return new InsertIntoTableDTO(query, null,null);
        }
        query = query.replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\s+", " ");

        String[] parts = query.trim().split("\\s+", 5);

        String val = parts[4];

        String[] value = Arrays.stream(val.trim().split(","))
                .map(String::trim)
                .toArray(String[]::new);

        List<String> list = List.of(value);

        return new InsertIntoTableDTO(query, list,parts[2]);

    }


    private CreateDatabaseDTO parseCreateDatabaseQuery()
    {

        String[] parts = query.trim().split("\\s+");
        if (parts.length == 3)
        {
            String databaseName = parts[2];
            return new CreateDatabaseDTO(query,databaseName);
        }
        return new CreateDatabaseDTO(query, null);
    }

    private UseDatabaseDTO parseUseDatabaseQuery()
    {
        String[] parts = query.trim().split("\\s+");
        if (parts.length == 2)
        {
            String databaseName = parts[1];
            return new UseDatabaseDTO(query, databaseName);
        }
        return new UseDatabaseDTO(query, null);
    }


    private CreateTableDTO parseCreateTableQuery()
    {
        String foregin_key="";
        String primary_key="";

        if(!checkQuotes(query))
        {
            return new CreateTableDTO(query, null, null,primary_key,foregin_key);
        }
        if(query.contains("primary"))
        {
            int lastCommaIndex = query.lastIndexOf(',');

            if(query.contains("foreign"))
            {
                int secondLastCommaIndex = query.lastIndexOf(',', lastCommaIndex - 1);
                lastCommaIndex = secondLastCommaIndex;
            }

            int openingParenthesisIndex = query.lastIndexOf(')');
            String extractedString = query.substring(lastCommaIndex, openingParenthesisIndex).trim();

            String[] constraint =  query.split(" key ");

//            // Output each part
//            for (int i = 0; i < constraint.length; i++) {
//                System.out.println("Part " + (i + 1) + ": " + constraint[i]);
//            }
            if(constraint.length > 0)
            {
                primary_key = constraint[1].replaceAll("\\(","").replaceAll("foreign","").replaceAll(",","");
            }
            if(constraint.length > 2)
            {

                String ColumnName = getColumnName(constraint[2]);
                String tableName = constraint[2].replaceAll("\\("," ").replaceAll("\\)", " ").replaceAll("\\s+", " ").split(" ")[3];
                foregin_key = ColumnName + "|" + tableName;
            }
            query = query.replace(extractedString,"");
        }

        query = query.replaceAll("\"", "");

        if(!validateSQL(query, CreateTablePattern))
        {
            return new CreateTableDTO(query, null, null,primary_key,foregin_key);
        }
        List<Map<String, String>> tableStructure = new ArrayList<>();
        String convertedQuery = query.trim().toLowerCase();

        convertedQuery = convertedQuery.replaceFirst("\\("," \\(");

        convertedQuery = convertedQuery.replaceAll("\\s+", " ");

        String[] parts = convertedQuery.trim().split("\\s+", 4);

        if (parts.length == 4)
        {
            tableStructure = validateCreateQuery(parts);
            if (!tableStructure.isEmpty())
            {
                return new CreateTableDTO(query, parts[2], tableStructure,primary_key,foregin_key);
            }
        }

        return new CreateTableDTO(query, null, null,primary_key,foregin_key);
    }

    private QueryDTO parseDeleteTableQuery(){
        if (!checkQuotes(query)) {
            return new DeleteFromTableDTO(query, null, null);
        }

        query = query.replaceAll("\"", "");

        if (!validateSQL(query, DeleteTablePattern)) {
            return new DeleteFromTableDTO(query, null, null);
        }

        query = query.replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll("\\s+", " ");

        String[] parts = query.trim().split("\\s+", 4);
        String tableName = parts[2];

        String whereCondition = parts[3].trim().split("where")[1].trim();

        return new DeleteFromTableDTO(query, tableName, whereCondition);
    }

    private DropTableDTO parseDropTableQuery()
    {
        String[] parts = query.trim().split("\\s+");
        if (parts.length == 3)
        {
            String tableName = parts[2];
            return new DropTableDTO(query, tableName);
        }
        return new DropTableDTO(query, null);
    }

    private boolean checkQuotes(String query)
    {

        int count = 0;

        for(Character c : query.toCharArray())
        {
            if(c.equals('"'))
            {
                count++;
            }
        }
        if(count == 0)
        {
            return true;
        }

        return count % 2 == 0;
    }

    private TransactionCommitDTO parseCommitQuery(){
        return new TransactionCommitDTO(query);
    }

    private TransactionRollbackDTO parseRollbackQuery(){
        return new TransactionRollbackDTO(query);
    }




    private List<Map<String, String>> validateCreateQuery(String[] parts) {
        List<Map<String, String>> tableStructure = new ArrayList<>();

        String structurePart = parts[3];

        if (!structurePart.startsWith("(") || !structurePart.endsWith(")")) {
            return tableStructure;
        }

        structurePart = structurePart.substring(1, structurePart.length() - 1).trim();
        String[] columns = structurePart.split(",");

        for (String column : columns) {
            String[] columnParts = column.trim().split("\\s+");
            if (columnParts.length != 2) {
                return new ArrayList<>();
            }
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put("name", columnParts[0]);
            columnMap.put("type", columnParts[1]);
            tableStructure.add(columnMap);
        }

        return tableStructure;
    }


    public static String getColumnName(String text)
    {
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();
        String content = "";

        while (matcher.find())
        {
            content = matcher.group(1);
        }

        return content.replaceAll("\\)", "");
    }


    public static String getWordsOutsideParentheses(String text) {
        // This regex matches whole words outside of parentheses
        Pattern pattern = Pattern.compile("\\b(?!PersonID\\b)\\w+");
        Matcher matcher = pattern.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            if (!matcher.group().matches("\\(.*\\)")) { // Ensure we are not capturing words inside parentheses
                if (result.length() > 0) {
                    result.append(", ");
                }
                result.append(matcher.group());
            }
        }

        return result.toString();
    }




    public static boolean validateSQL(String sql, Pattern pattern)
    {
        Matcher matcher = pattern.matcher(sql);
        return matcher.matches();
    }
}

