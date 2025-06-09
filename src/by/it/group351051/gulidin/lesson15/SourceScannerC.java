package by.it.group351051.gulidin.lesson15;

import java.io.*;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class SourceScannerC {

    public static void main(String[] args) {
        String src = System.getProperty("user.dir") + File.separator + "src" + File.separator;

        try {
            // 1. Читаем все Java файлы
            List<String> javaFiles = findJavaFiles(src);

            // 2. Обрабатываем файлы и фильтруем тесты
            Map<String, String> processedFiles = processFiles(javaFiles);

            // 3. Ищем копии
            Map<String, List<String>> copies = findCopies(processedFiles);

            // 4. Выводим результаты
            printResults(copies);

        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }

    // Рекурсивный поиск всех .java файлов
    private static List<String> findJavaFiles(String srcPath) throws IOException {
        List<String> javaFiles = new ArrayList<>();

        try {
            Files.walk(Paths.get(srcPath))
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toString()));
        } catch (IOException e) {
            // Если каталог src не существует, возвращаем пустой список
            return javaFiles;
        }

        return javaFiles;
    }

    // Обработка файлов с фильтрацией тестов
    private static Map<String, String> processFiles(List<String> javaFiles) {
        Map<String, String> processedFiles = new HashMap<>();

        for (String filePath : javaFiles) {
            try {
                String content = readFileWithEncoding(filePath);

                // Фильтруем тесты
                if (isTestFile(content)) {
                    continue;
                }

                // Обрабатываем содержимое
                String processed = processFileContent(content);

                if (!processed.isEmpty()) {
                    processedFiles.put(filePath, processed);
                }

            } catch (IOException e) {
                System.err.println("Ошибка чтения файла " + filePath + ": " + e.getMessage());
            }
        }

        return processedFiles;
    }

    // Чтение файла с обработкой различных кодировок
    private static String readFileWithEncoding(String filePath) throws IOException {
        // Сначала пробуем UTF-8
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            // Если UTF-8 не подходит, пробуем CP1251
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                return new String(bytes, "CP1251");
            } catch (UnsupportedEncodingException ex) {
                // Если и CP1251 не подходит, используем системную кодировку
                return Files.readString(Paths.get(filePath));
            }
        }
    }

    // Проверка, является ли файл тестом
    private static boolean isTestFile(String content) {
        return content.contains("@Test") || content.contains("org.junit.Test");
    }

    // Обработка содержимого файла
    private static String processFileContent(String content) {
        // 1. Удаляем package и imports
        String withoutPackageImports = removePackageAndImports(content);

        // 2. Удаляем комментарии за O(n)
        String withoutComments = removeComments(withoutPackageImports);

        // 3. Заменяем символы с кодом <33 на пробел
        String normalized = normalizeWhitespace(withoutComments);

        // 4. Выполняем trim
        return normalized.trim();
    }

    // Удаление package и import строк
    private static String removePackageAndImports(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n");

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (!trimmedLine.startsWith("package ") && !trimmedLine.startsWith("import ")) {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }

    // Удаление комментариев за O(n)
    private static String removeComments(String content) {
        StringBuilder result = new StringBuilder();
        int length = content.length();
        int i = 0;

        while (i < length) {
            char current = content.charAt(i);

            // Проверяем на начало комментария
            if (current == '/' && i + 1 < length) {
                char next = content.charAt(i + 1);

                if (next == '/') {
                    // Однострочный комментарий - пропускаем до конца строки
                    i += 2;
                    while (i < length && content.charAt(i) != '\n') {
                        i++;
                    }
                    // Сохраняем символ новой строки
                    if (i < length) {
                        result.append('\n');
                        i++;
                    }
                } else if (next == '*') {
                    // Многострочный комментарий - пропускаем до */
                    i += 2;
                    while (i + 1 < length) {
                        if (content.charAt(i) == '*' && content.charAt(i + 1) == '/') {
                            i += 2;
                            break;
                        }
                        i++;
                    }
                } else {
                    result.append(current);
                    i++;
                }
            } else if (current == '"') {
                // Строковый литерал - не обрабатываем как комментарий
                result.append(current);
                i++;
                while (i < length) {
                    char ch = content.charAt(i);
                    result.append(ch);
                    if (ch == '"') {
                        i++;
                        break;
                    } else if (ch == '\\' && i + 1 < length) {
                        // Экранированный символ
                        i++;
                        if (i < length) {
                            result.append(content.charAt(i));
                        }
                    }
                    i++;
                }
            } else if (current == '\'') {
                // Символьный литерал
                result.append(current);
                i++;
                while (i < length) {
                    char ch = content.charAt(i);
                    result.append(ch);
                    if (ch == '\'') {
                        i++;
                        break;
                    } else if (ch == '\\' && i + 1 < length) {
                        i++;
                        if (i < length) {
                            result.append(content.charAt(i));
                        }
                    }
                    i++;
                }
            } else {
                result.append(current);
                i++;
            }
        }

        return result.toString();
    }

    // Нормализация пробельных символов
    private static String normalizeWhitespace(String content) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch < 33) {
                result.append(' ');
            } else {
                result.append(ch);
            }
        }

        return result.toString();
    }

    // Поиск копий по расстоянию Левенштейна
    private static Map<String, List<String>> findCopies(Map<String, String> processedFiles) {
        Map<String, List<String>> copies = new HashMap<>();
        List<String> filePaths = new ArrayList<>(processedFiles.keySet());

        // Сортируем пути для детерминированного порядка
        Collections.sort(filePaths);

        for (int i = 0; i < filePaths.size(); i++) {
            String path1 = filePaths.get(i);
            String content1 = processedFiles.get(path1);

            for (int j = i + 1; j < filePaths.size(); j++) {
                String path2 = filePaths.get(j);
                String content2 = processedFiles.get(path2);

                int distance = levenshteinDistance(content1, content2);

                if (distance < 10) {
                    // Найдена копия
                    copies.computeIfAbsent(path1, k -> new ArrayList<>()).add(path2);
                }
            }
        }

        return copies;
    }

    // Оптимизированное вычисление расстояния Левенштейна
    private static int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        // Оптимизация: если одна строка намного длиннее другой
        if (Math.abs(len1 - len2) >= 10) {
            return Math.abs(len1 - len2);
        }

        // Используем только две строки вместо полной матрицы для экономии памяти
        int[] previousRow = new int[len2 + 1];
        int[] currentRow = new int[len2 + 1];

        // Инициализация первой строки
        for (int j = 0; j <= len2; j++) {
            previousRow[j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            currentRow[0] = i;

            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;

                currentRow[j] = Math.min(
                        Math.min(currentRow[j - 1] + 1,        // insertion
                                previousRow[j] + 1),          // deletion
                        previousRow[j - 1] + cost              // substitution
                );
            }

            // Оптимизация: если текущий минимум уже >= 10, можно прервать
            int minInRow = currentRow[0];
            for (int j = 1; j <= len2; j++) {
                minInRow = Math.min(minInRow, currentRow[j]);
            }
            if (minInRow >= 10) {
                return 10; // Возвращаем 10 или больше
            }

            // Меняем строки местами
            int[] temp = previousRow;
            previousRow = currentRow;
            currentRow = temp;
        }

        return previousRow[len2];
    }

    // Вывод результатов
    private static void printResults(Map<String, List<String>> copies) {
        if (copies.isEmpty()) {
            return;
        }

        // Сортируем файлы лексикографически
        List<String> sortedFiles = new ArrayList<>(copies.keySet());
        Collections.sort(sortedFiles);

        for (String filePath : sortedFiles) {
            System.out.println(filePath);

            List<String> copyPaths = copies.get(filePath);
            Collections.sort(copyPaths);

            for (String copyPath : copyPaths) {
                System.out.println(copyPath);
            }
        }
    }
}
