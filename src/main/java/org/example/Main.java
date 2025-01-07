package org.example;

import org.example.DTO.*;
import org.example.Logs.EventLogsService;
import org.example.Logs.GeneralLogsService;
import org.example.Logs.QueryLogsService;
import org.example.auth.AuthPage;
import org.w3c.dom.events.EventException;

import javax.management.StandardEmitterMBean;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class Main {

    static Path path = Paths.get("TinyDB");

    static Path internalPath = Path.of("TinyDB/");

    static Path schemaPath = Path.of("TinyDB/");

    static Map<String, LinkedHashMap<String, List<String>>> tables = new LinkedHashMap<>();

    static Map<String, TableSchema> schemas = new HashMap<>();

    static Map<String, LinkedHashMap<String, List<String>>> copyOfTables = new HashMap<>();

    static Map<String, TableSchema> copyOfSchemas = new HashMap<>();

    static String currentUser = null;

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        AuthPage authPage = new AuthPage();
        String currUser = authPage.displayAuthPage();
        if(currUser == null){
            System.out.println("Invalid User");
            System.exit(0);
        }
        currentUser = currUser;
        System.out.println("Current user is "+ currUser);
        optionsPage(sc);
    }


    public static void optionsPage(Scanner sc) throws Exception {
        System.out.println("Welcome ! Choose an option");
        System.out.println("1. Write queries");
        System.out.println("2. Export Data and Structure");
        System.out.println("3. ERD");
        System.out.println("4. Exit");

        int selectedOption = sc.nextInt();
        switch (selectedOption) {
            case 1:
                queriesMenu();
                break;
            case 2:
                exportData();
                break;
            case 3:
                erdGenerator();
                break;
            case 4:
                System.out.println("Program Terminated Successfully");
                System.exit(0);
        }

    }

    private static void erdGenerator() throws IOException {
        DiagramHandeler diagramHandeler = new DiagramHandeler();
        System.out.println("Name of the Database ");
        Scanner sc = new Scanner(System.in);
        String name = sc.nextLine();
        Path schemaFolderPath = schemaPath.resolve(name);
        List<Entity> entities = new ArrayList<>();

        File folder = new File(String.valueOf(schemaFolderPath));
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith("schema")) {
                    Entity entity = DiagramHandeler.parseSchema(file.getPath());
                    entities.add(entity);
                }
            }
        }
        getDataInLocalMemory(schemaFolderPath);
        Path databaseSqlSchema = schemaPath.resolve(name + "_ERD");
        diagramHandeler.displayERDiagram(entities,tables, String.valueOf(databaseSqlSchema));
    }

    private static void exportData() {

        System.out.println("Enter Database Name :");
        Scanner sc = new Scanner(System.in);
        String databaseName = sc.nextLine();
        Path databaseNameSchema = schemaPath.resolve(databaseName);
        Path databaseSqlSchema = schemaPath.resolve(databaseName + "_schema" + ".sql");
        try {
            SchemaDataProcessor schemaDataProcessor = new SchemaDataProcessor();
            List<Table> tables = schemaDataProcessor.processFolder(databaseNameSchema.toString());
            schemaDataProcessor.writeSQLToFile(tables, databaseSqlSchema.toString());
            System.out.println("Database Schema Created Successfully");
        } catch (IOException e) {
            e.printStackTrace();
            EventLogsService.logErrorMessage("Error while exporting data",currentUser);
        }
    }


    public static void queriesMenu() throws DatabaseNotSet, IOException {
        boolean isTransactionOpen = false;
        Scanner sc = new Scanner(System.in);
        String currentDatabaseName = null;

        while (true) {
            System.out.print("SQL> ");
            String command = sc.nextLine();

            command = command.toLowerCase();

            if (command.equals("quit")) {
                System.out.println("Exit");
                break;
            }


            if (!command.contains(";")) {
                System.out.println("Syntax Error" + command);
                continue;
            }

            String currentQuery = command;
            command = command.replaceAll(";", "");

            long startTime = System.currentTimeMillis();
            String queryTimeStamp = Instant.now().toString();
            QueryParse queryParse = new QueryParse(command);
            QueryDTO dto1 = queryParse.parseQuery();

            if (dto1 instanceof CreateDatabaseDTO)
            {

                CreateDatabaseDTO createDatabaseDTO = (CreateDatabaseDTO) dto1;
                if (createDatabaseDTO.getDatabaseName() == null || createDatabaseDTO.getDatabaseName().isEmpty()) {
                    System.out.println("Syntax Error");
                    continue;
                }
                try {
                    Path databasePath = path.resolve(createDatabaseDTO.getDatabaseName());
                    Files.createDirectory(databasePath);
                    System.out.println("Database Created Successfully");
                    EventLogsService.logCreateDatabase(createDatabaseDTO.getDatabaseName() , currentUser);
                } catch (Exception e) {
                    System.out.println("Database Name Already Present");
                    EventLogsService.logErrorMessage("Database Name Already Present",currentUser);
                    continue;
                }
            }

            if (dto1 instanceof UseDatabaseDTO) {
                UseDatabaseDTO useDatabaseDTO = (UseDatabaseDTO) dto1;
                try {
                    Path folderPath = path.resolve(useDatabaseDTO.getDatabaseName());

                    if (Files.exists(folderPath)) {
                        internalPath = folderPath;
                        getDataInLocalMemory(internalPath);
                        System.out.println("Entered into database");
                        currentDatabaseName = useDatabaseDTO.getDatabaseName();
                    } else {
                        System.out.println("The database does not exist");
                        EventLogsService.logErrorMessage("The database does not exist",currentUser);
                    }
                } catch (Exception e) {
                    System.out.println("The database does not exist");
                    EventLogsService.logErrorMessage("The database does not exist",currentUser);
                }
            }

            if (dto1 instanceof CreateTableDTO)
            {

                CreateTableDTO createTableDTO = (CreateTableDTO) dto1;

                if (createTableDTO.getTableName() == null) {
                    System.out.println("Syntax Error");
                    continue;
                }
                try
                {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }

                    // Creating Foreign Key constraint
                    if (createTableDTO.getForeign_key().length() > 1) {
                        String foreignKey = createTableDTO.getForeign_key().split("\\|")[0];
                        String foreignKeyTable = createTableDTO.getForeign_key().split("\\|")[1];

                        if (!tables.containsKey(foreignKeyTable)) {
                            throw new ConstarintException("Key");
                        } else {
                            if (!tables.get(foreignKeyTable).containsKey(foreignKey)) {
                                throw new ConstarintException("Key");

                            }
                        }

                    }

                    Path tablePath = internalPath.resolve(createTableDTO.getTableName());
                    Path schemaPath = internalPath.resolve(createTableDTO.getTableName() + "_schema");

                    Files.createFile(tablePath);
                    Files.createFile(schemaPath);

                    StringBuilder data = new StringBuilder();
                    List<Map<String, String>> columnStructure = createTableDTO.getTableStructure();

                    for (Map<String, String> column : columnStructure) {
                        String columnName = column.get("name");
                        data.append(columnName).append("|");
                    }
                    StringBuilder schema = new StringBuilder();
                    for (Map<String, String> column : columnStructure) {
                        String columnName = column.get("name");
                        String columnType = column.get("type");
                        schema.append(columnName).append(" ").append(columnType).append("|");
                    }
                    StringBuilder keys = new StringBuilder();

                    keys.append("primaryKey=").append(createTableDTO.getPrimary_key().replaceAll("\\)", "")).append("\n");
                    keys.append("foreignKey=").append(createTableDTO.getForeign_key());


                    schema = schema.append("\n").append(keys);

                    Files.write(tablePath, data.toString().getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    Files.write(schemaPath, schema.toString().getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    getDataInLocalMemory(internalPath);


                    System.out.println("Table Created Successfully");
                    EventLogsService.logCreateTable(createTableDTO.getTableName() , currentUser);
                }catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);
                }
                catch (ConstarintException constarintException){
                    System.out.println("Table constraints given are not valid.");
                    EventLogsService.logErrorMessage("Table constraints given are not valid",currentUser);

                }
                catch (Exception e) {
                    System.out.println("Table already exists ");
                    EventLogsService.logErrorMessage("Table already exists",currentUser);
                }
            }

            if (dto1 instanceof InsertIntoTableDTO) {
                try {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }
                    InsertIntoTableDTO insertIntoTableDTO = (InsertIntoTableDTO) dto1;

                    if (insertIntoTableDTO.getTableName() == null) {
                        System.out.println("Table name is wrong");
                    }

                    TableSchema tableSchema = schemas.get(insertIntoTableDTO.getTableName());
                    if (tableSchema == null) {
                        System.out.println("No table");
                        break;
                    }
                    int i = 0;
                    boolean isValid = true;
                    Map<String, LinkedHashMap<String, List<String>>> tempTable = new HashMap<>();
                    for (Map.Entry<String, LinkedHashMap<String, List<String>>> entry : tables.entrySet()) {
                        String key = entry.getKey();
                        LinkedHashMap<String, List<String>> value = new LinkedHashMap<>();
                        for (Map.Entry<String, List<String>> innerEntry : entry.getValue().entrySet()) {
                            value.put(innerEntry.getKey(), new ArrayList<>(innerEntry.getValue()));
                        }
                        tempTable.put(key, value);
                    }


                    try {
                        if (tableSchema.primary_key != null) {
                            String primaryKey = tableSchema.primary_key;
                            int index = findIndexByColumnName(tableSchema.columns, primaryKey);
                            String tableName = insertIntoTableDTO.getTableName();
                            String check = insertIntoTableDTO.getValues().get(index);
                            List<String> l = tempTable.get(tableName).get(primaryKey);
                            if (l.contains(check)) {
                                isValid = false;
                                System.out.println("Primary key constraint Not followed ");
                                continue;
                            }
                        }

                        if (tableSchema.foreign_key != null) {
                            String foreignKey = tableSchema.foreign_key;
                            String foreignTableName = tableSchema.foreign_key_table;
                            int index = findIndexByColumnName(tableSchema.getColumns(), foreignKey);
                            String check = insertIntoTableDTO.getValues().get(index);
                            List<String> l = tempTable.get(foreignTableName).get(foreignKey);

                            if (!l.contains(check)) {
                                isValid = false;
                                System.out.println("Foreign key constraint not followed for column: " + foreignKey);
                                continue;
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Constraint Failed");
                        EventLogsService.logErrorMessage("PK and FK constraints not followed",currentUser);
                    }


                    for (Map<String, String> list : tableSchema.getColumns()) {
                        String C_Name = list.keySet().iterator().next();
                        String C_Type = list.values().iterator().next();
                        // check if the value is of the correct type
                        if (C_Type.equals("int")) {
                            try {
                                Integer.parseInt(insertIntoTableDTO.getValues().get(i));
                            } catch (NumberFormatException e) {
                                isValid = false;
                                System.out.println("Value is not of correct type");
                                break;
                            }
                        } else if (C_Type.equals("float")) {
                            try {
                                Float.parseFloat(insertIntoTableDTO.getValues().get(i));
                            } catch (NumberFormatException e) {
                                isValid = false;
                                System.out.println("Value is not of correct type");
                                break;
                            }
                        } else if (C_Type.equals("double")) {
                            try {

                                Double.parseDouble(insertIntoTableDTO.getValues().get(i));
                            } catch (NumberFormatException e) {
                                isValid = false;
                                System.out.println("Value is not of correct type");
                            }
                        } else if (C_Type.startsWith("varchar")) {
                            // extract the number from varchar(5)

                            int originalLength = Integer.parseInt(C_Type.substring(8, C_Type.length() - 1));
                            // check if varchar(5) so data to be inserted should be inside 5
                            String val = insertIntoTableDTO.getValues().get(i);
                            if (val.length() > originalLength) {
                                isValid = false;
                                System.out.println("Value is not of correct type");
                                break;
                            }
                            // No need to parse, as varchar can accept any string value

                        }

                        tempTable.get(insertIntoTableDTO.getTableName()).get(C_Name).add(insertIntoTableDTO.getValues().get(i));
                        i++;
                    }
                    if (isValid) {
                        tables.putAll(tempTable);
                        System.out.println("Data Inserted Successfully");
                    }

                } catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);

                }
            }

            if (dto1 instanceof SelectFromTableDTO) {
                try {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }

                    SelectFromTableDTO selectFromTableDTO = (SelectFromTableDTO) dto1;

                    if (selectFromTableDTO.getTableName() == null) {
                        System.out.println("Table name is wrong");
                        continue;
                    }
                    TableSchema tableSchema = schemas.get(selectFromTableDTO.getTableName());
                    if (tableSchema == null) {
                        System.out.println("Table is not present: " + selectFromTableDTO.getTableName());
                        continue;
                    }

                    List<String> columns = selectFromTableDTO.getColumns();
                    String condition = selectFromTableDTO.getCondition();

                    if (columns.contains("*")) {
                        columns = new ArrayList<>(tableSchema.getColumns().stream()
                                .map(Map::keySet)
                                .flatMap(Set::stream)
                                .collect(Collectors.toList()));
                    }

                    for (String column : columns) {
                        System.out.print(column + "\t");
                    }
                    System.out.println();

                    Map<String, List<String>> tableData = tables.get(selectFromTableDTO.getTableName());

                    // Iterate through each row
                    for (int i = 0; i < tableData.values().iterator().next().size(); i++) {
                        if (condition != null && !evaluateCondition(condition, tableData, i)) {
                            continue;
                        }
                        for (String column : columns) {
                            System.out.print(tableData.get(column).get(i) + "\t");
                        }
                        System.out.println();
                    }

                }catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);

                } catch (Exception ex) {
                    System.out.println("An Error occurred while selecting from the table: " + ex.getMessage());
                    EventLogsService.logErrorMessage("An Error occurred while selecting from the table: " + ex.getMessage(),currentUser);
                }
            }

            if (dto1 instanceof UpdateTableDTO) {
                try {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }

                    UpdateTableDTO updateTableDTO = (UpdateTableDTO) dto1;

                    if (updateTableDTO.getTableName() == null) {
                        System.out.println("Table name is wrong");
                        continue;  // Skip the rest of this iteration and prompt for a new query
                    }

                    Map<String, List<String>> tableData = tables.get(updateTableDTO.getTableName());
                    Map<String, String> setValues = updateTableDTO.getSetValues();
                    String condition = updateTableDTO.getCondition();

                    for (int i = 0; i < tableData.values().iterator().next().size(); i++) {
                        if (evaluateCondition(condition, tableData, i)) {
                            for (Map.Entry<String, String> entry : setValues.entrySet()) {
                                String columnName = entry.getKey();
                                String newValue = entry.getValue();
                                tableData.get(columnName).set(i, newValue);
                            }
                        }
                    }

                }catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);

                } catch (Exception ex) {
                    System.out.println("An Error occurred while updating the table: " + ex.getMessage());
                }
            }

            if (dto1 instanceof DeleteFromTableDTO) {
                try {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }
                    DeleteFromTableDTO deleteFromTableDTO = (DeleteFromTableDTO) dto1;

                    if (deleteFromTableDTO.getTableName() == null) {
                        System.out.println("Table name is wrong");
                    }

                    Map<String, List<String>> tableData = tables.get(deleteFromTableDTO.getTableName());
                    if (tableData == null) {
                        System.out.println("Table is empty");
                    }
                    String condition = deleteFromTableDTO.getWhereCondition();


                    for (int i = 0; i < tableData.values().iterator().next().size(); i++) {
                        if (evaluateCondition(condition, tableData, i)) {
                            for (Map.Entry<String, List<String>> entry : tableData.entrySet()) {
                                entry.getValue().remove(i);
                            }
                        }
                    }

                } catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);

                }catch (Exception ex) {
                    System.out.println("An error occurred while deleting from the table: " + ex.getMessage());
                    EventLogsService.logErrorMessage("An error occurred while deleting from the table: " + ex.getMessage(),currentUser);
                }
            }

            if (dto1 instanceof DropTableDTO) {
                try {
                    if(internalPath.toString().equals("TinyDB"))
                    {
                        throw new DatabaseNotSet("Database Not Set");
                    }
                    DropTableDTO dropTableDTO = (DropTableDTO) dto1;

                    if (dropTableDTO.getTableName() == null) {
                        System.out.println("Table name is wrong");
                    }

                    Path tablePath = internalPath.resolve(dropTableDTO.getTableName());
                    Path schemaPath = internalPath.resolve(dropTableDTO.getTableName() + "_schema");

                    Files.deleteIfExists(tablePath);
                    Files.deleteIfExists(schemaPath);

                    tables.remove(dropTableDTO.getTableName());
                    schemas.remove(dropTableDTO.getTableName());

                    System.out.println("Table Dropped Successfully");
                    EventLogsService.logDropTable(dropTableDTO.getTableName() , currentUser);
                }catch (DatabaseNotSet databaseNotSet)
                {
                    System.out.println("Please Select The Database");
                    EventLogsService.logErrorMessage("Database has not been selected",currentUser);
                } catch (Exception ex) {
                    System.out.println("An error occurred while dropping the table: " + ex.getMessage());
                }
            }

            if (dto1 instanceof StartTransactionDTO) {
                isTransactionOpen = true;
                for (Map.Entry<String, TableSchema> entry : schemas.entrySet()) {
                    copyOfSchemas.put(entry.getKey(), entry.getValue().clone());
                }

                for (Map.Entry<String, LinkedHashMap<String, List<String>>> entry : tables.entrySet()) {
                    String tableName = entry.getKey();
                    LinkedHashMap<String, List<String>> tableContent = entry.getValue();

                    LinkedHashMap<String, List<String>> tableContentCopy = new LinkedHashMap<>();
                    for (Map.Entry<String, List<String>> subEntry : tableContent.entrySet()) {
                        String key = subEntry.getKey();
                        List<String> list = subEntry.getValue();

                        List<String> listCopy = new ArrayList<>(list);
                        tableContentCopy.put(key, listCopy);
                    }
                    copyOfTables.put(tableName, tableContentCopy);
                }

                System.out.println("Transaction Started");
                EventLogsService.logTransactionStarted(currentDatabaseName , currentUser);

            }

            if (dto1 instanceof TransactionCommitDTO) {
                if (isTransactionOpen) {
                    isTransactionOpen = false;
                    System.out.println("Transaction Committed");
                    EventLogsService.logTransactionCommited(currentDatabaseName , currentUser);
                } else {
                    System.out.println("No Transaction to Commit");
                }
            }

            if (dto1 instanceof TransactionRollbackDTO) {
                if (isTransactionOpen) {
                    schemas.clear();
                    schemas.putAll(copyOfSchemas);

                    tables.clear();
                    tables.putAll(copyOfTables);

                    isTransactionOpen = false;
                    System.out.println("Transaction Rolled Back");
                    EventLogsService.logTransactionRollback(currentDatabaseName , currentUser);

                } else {
                    System.out.println("No Transaction to Rollback");
                }
            }

            if (dto1 == null) {
                System.out.println("Query Type: Syntax Error;");
                continue;
            }
            if (!isTransactionOpen) {
                write_into_files();
            }
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            Map<String, Integer> dbState = new HashMap<>();
            if(currentDatabaseName != null){
                Path dbPath = Paths.get("TinyDB/").resolve(currentDatabaseName);
                DirectoryStream<Path> stream = Files.newDirectoryStream(dbPath);
                for(Path entry:stream){
                    if(!Files.isDirectory(entry)) {
                        if(entry.getFileName().toString().endsWith("_schema")) {
                            continue;
                        }
                        int recordCount = Files.readAllLines(entry).size() - 1;
                        dbState.put(entry.getFileName().toString(), recordCount);
                    }
                }
            }
            GeneralLogsService.logQueryExecutionTime(command, executionTime);
            GeneralLogsService.logDatabaseState(dbState);
            QueryLogsService.logQuerySubmission(currentQuery, queryTimeStamp);

        }

    }
    // Changing Structure
    private static void getDataInLocalMemory(Path path) {

        schemas.clear();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(String.valueOf(path)))) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith("_schema")) {
                        String tableName = fileName.replace("_schema", "");
                        TableSchema schema = processSchemaFile(file);
                        schemas.put(tableName, schema);
                        tables.put(tableName, new LinkedHashMap<>());
                        for (Map<String, String> column : schema.columns) {
                            for (String columnName : column.keySet()) {
                                tables.get(tableName).put(columnName, new ArrayList<>());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while getting the data in the local memory: " + e.getMessage());
            EventLogsService.logErrorMessage("Something went wrong while getting the data in the local memory: " + e.getMessage(),currentUser);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(String.valueOf(path)))) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    String fileName = file.getFileName().toString();
                    if (!fileName.endsWith("_schema")) {
                        String tableName = fileName;
                        if (tables.containsKey(tableName)) {
                            processDataFile(file, tables.get(tableName));
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while getting the data in the local memory: " + e.getMessage());
            EventLogsService.logErrorMessage("Something went wrong while getting the data in the local memory: " + e.getMessage(),currentUser);
        }

    }

    private static TableSchema processSchemaFile(Path file) throws IOException {
        List<String> lines = readFile(file.toString());
        TableSchema schema = new TableSchema();
        String[] schemaParts = lines.get(0).split("\\|");
        for (String part : schemaParts) {
            String[] column = part.split(" ");
            schema.addColumn(column[0], column[1]);
        }
        try {
            schema.primary_key = lines.get(1).split("=")[1];
        } catch (Exception E) {
            schema.primary_key = null;
        }
        try {
            schema.foreign_key = lines.get(2).split("=")[1].split("\\|")[0];
        } catch (Exception E) {
            schema.foreign_key = null;
        }
        try {
            schema.foreign_key_table = lines.get(2).split("=")[1].split("\\|")[1];
        } catch (Exception E) {
            schema.foreign_key_table = null;
        }
        return schema;
    }

    private static void processDataFile(Path file, Map<String, List<String>> table) throws IOException {
        List<String> lines = readFile(file.toString());
        String[] headers = lines.get(0).split("\\|");

        for (int i = 1; i < lines.size(); i++) {
            String[] dataParts = lines.get(i).split("\\|");
            for (int j = 0; j < headers.length; j++) {
                if (j < dataParts.length) {

                    if (table != null) {
                        if (table.get(headers[j]) != null) {
                            table.get(headers[j]).add(dataParts[j]);
                        }
                    }
                }
            }
        }
    }

    private static List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    private static void write_into_files() {

        for (Map.Entry<String, LinkedHashMap<String, List<String>>> entry : tables.entrySet()) {
            String tableName = entry.getKey();
            writeDataToFile(String.valueOf(internalPath), tableName, entry.getValue());
        }

    }

    private static void writeDataToFile(String folderPath, String tableName, Map<String, List<String>> table) {
        Path filePath = Paths.get(folderPath, tableName);
        int numberOfColumns = table.keySet().size();
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            if (!table.isEmpty()) {
                if (filePath.endsWith("schema")) {
                    return;
                }
                String headers = String.join("|", table.keySet());
                writer.write(headers + "\n");

                int rowCount = table.values().iterator().next().size();
                for (int i = 0; i < rowCount; i++) {
                    StringBuilder line = new StringBuilder();
                    for (String key : table.keySet()) {
                        line.append(table.get(key).get(i)).append("|");
                    }
                    writer.write(line.substring(0, line.length() - 1) + "\n"); // Remove last pipe
                }
            }
        } catch (IOException e) {
            System.out.println("Something went wrong while getting the data in the local memory: " + e.getMessage());
            EventLogsService.logErrorMessage("Something went wrong while getting the data in the local memory: " + e.getMessage(),currentUser);
        }

    }

    private static boolean evaluateCondition(String condition, Map<String, List<String>> tableData, int rowIndex) {
        String[] operators = {"<=", ">=", "!=", ">", "<", "="};
        String operator = null;

        for (String op : operators) {
            if (condition.contains(op)) {
                operator = op;
                break;
            }
        }

        if (operator == null) {
            return false;
        }

        String[] parts = condition.split(operator);
        if (parts.length != 2) {
            return false;
        }

        String columnName = parts[0].trim();
        String value = parts[1].trim();

        String actualValue = tableData.get(columnName).get(rowIndex);

        switch (operator) {
            case "=":
                return actualValue.equals(value);
            case ">=":
                return Double.parseDouble(actualValue) >= Double.parseDouble(value);
            case "<=":
                return Double.parseDouble(actualValue) <= Double.parseDouble(value);
            case ">":
                return Double.parseDouble(actualValue) > Double.parseDouble(value);
            case "<":
                return Double.parseDouble(actualValue) < Double.parseDouble(value);
            case "!=":
                return Double.parseDouble(actualValue) != Double.parseDouble(value);
            default:
                return false;
        }
    }

    public static int findIndexByColumnName(List<Map<String, String>> columns, String columnName)
    {
        for (int i = 0; i < columns.size(); i++) {
            Map<String, String> column = columns.get(i);
            if (column.containsKey(columnName)) {
                return i;
            }
        }
        return -1; // Return -1 if the column name is not found
    }


}












