package org.example;

import org.example.DTO.Entity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DiagramHandeler
{


    public static Entity parseSchema(String filePath) throws IOException
    {
        Entity entity = new Entity();
        List<String> lines = readFile(filePath);
        String[] parts = lines.get(0).split("\\|");
        entity.setName(filePath.split("\\\\")[2].split("_")[0]);
        for (int i = 1; i < parts.length; i++) {
            entity.attributes.put(parts[i].split(" ")[0], parts[i].split(" ")[1]);
        }
        try {
            entity.primaryKey = lines.get(1).split("=")[1];
        } catch (Exception e) {
            entity.primaryKey = null;
        }
        try {
            entity.foreignKey = lines.get(2).split("=")[1].split("\\|")[0];
        } catch (Exception e) {
            entity.foreignKey = null;
        }
        try {
            entity.foreignTable = lines.get(2).split("=")[1].split("\\|")[1];
        } catch (Exception e) {
            entity.foreignTable = null;
        }
        return entity;
    }

    private static List<String> readFile(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }

    public static void displayERDiagram(List<Entity> entities, Map<String, LinkedHashMap<String, List<String>>> copyOfTables, String outputPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            int maxEntityNameLength = entities.stream()
                    .mapToInt(entity -> entity.name.length())
                    .max()
                    .orElse(0);

            int maxAttributeLength = entities.stream()
                    .flatMap(entity -> entity.attributes.entrySet().stream())
                    .mapToInt(entry -> (entry.getKey() + " (" + entry.getValue() + ")").length())
                    .max()
                    .orElse(0);

            int maxLineLength = Math.max(maxEntityNameLength + 2, maxAttributeLength + 2);
            maxLineLength = Math.max(maxLineLength, 40);

            for (Entity entity : entities) {
                String borderLine = "+" + "-".repeat(maxLineLength + 2) + "+";

                writer.write(borderLine);
                writer.newLine();
                createLine(writer, entity.name, maxLineLength);

                writer.write(borderLine);
                writer.newLine();

                for (Map.Entry<String, String> entry : entity.attributes.entrySet()) {
                    String attributeLine = entry.getKey() + " (" + entry.getValue() + ")";
                    createLine(writer, attributeLine, maxLineLength);
                }

                String primaryKeyLine = "primaryKey = " + entity.primaryKey;
                createLine(writer, primaryKeyLine, maxLineLength);

                if (entity.foreignKey != null && !entity.foreignKey.isEmpty()) {
                    String foreignKeyLine = "foreignKey = " + entity.foreignKey;
                    String referencesLine = "references " + entity.foreignTable + "." + entity.foreignKey;
                    createLine(writer, foreignKeyLine, maxLineLength);
                    createLine(writer, referencesLine, maxLineLength);
                }

                writer.write(borderLine);
                writer.newLine();
                writer.newLine();
            }

            writer.write("\nLegend:");
            writer.newLine();
            writer.write("<>------: Foreign Key Relationship (1:N)");
            writer.newLine();
            writer.write("--------: Foreign Key Relationship (1:1)");
            writer.newLine();

            for (Entity entity : entities) {
                if (entity.foreignKey != null && !entity.foreignKey.isEmpty()) {
                    Scanner sc = new Scanner(System.in);

                    System.out.println("Please give the relation between " + entity.name + " and " + entity.foreignTable);

                    String var = sc.nextLine();
                    List<String> list1 = copyOfTables.get(entity.foreignTable).get(entity.foreignKey);
                    List<String> list2 = copyOfTables.get(entity.name).get(entity.foreignKey);

                    String relation = checkElements(list1, list2);
                    String foreignKeyLine = entity.foreignTable + relation + entity.name;
                    int paddingLength = entity.foreignTable.length() - 1 + (foreignKeyLine.length() - entity.name.length() - entity.foreignTable.length() - 1) / 2;
                    String padding = " ".repeat(Math.max(0, paddingLength));
                    writer.write(padding + var);
                    writer.newLine();
                    writer.write(foreignKeyLine);
                    writer.newLine();
                }
            }
        }
    }

    public static void createLine(BufferedWriter writer, String content, int maxLineLength) throws IOException {
        int padding = maxLineLength - content.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;
        writer.write("| " + " ".repeat(leftPadding) + content + " ".repeat(rightPadding) + " |");
        writer.newLine();
    }

    public static String checkElements(List<String> list1, List<String> list2) {
        Map<String, Integer> counter2 = countOccurrences(list2);

        for (String element : list1) {
            if (!counter2.containsKey(element) || counter2.get(element) != 1) {
                return "------<>";
            }
        }

        return "--------";
    }

    private static Map<String, Integer> countOccurrences(List<String> list) {
        Map<String, Integer> counter = new HashMap<>();
        for (String element : list) {
            counter.put(element, counter.getOrDefault(element, 0) + 1);
        }
        return counter;
    }







}
