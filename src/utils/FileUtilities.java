package utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtilities {
    public static void WriteToFile(String filePath, String content) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(Paths.get(filePath)))) {
            writer.write(content);
        } catch (IOException e) {
            throw new IOException(String.format("Failed to write content to file located at: %s", filePath) ,e);
        }
    }

    public static void createFoldersInPath(String path) {
        if(path == null) {
            return;
        }

        File folder = new File(path);
        String parentPath = folder.getParent();

        createFoldersInPath(parentPath);

        if(!folder.getName().contains(".") && !folder.exists()) {
            folder.mkdir();
        }
    }

    public static void ZipFile(String fileToZipName, String content, String zipPath) throws IOException {
        File zipFile = new File(zipPath);

        try(ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile))) {
            ZipEntry zipEntry = new ZipEntry(fileToZipName);
            out.putNextEntry(zipEntry);

            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            out.write(data, 0, data.length);
            out.closeEntry();
        }
    }

    public static long getFolderSize(File folder) throws IOException {
        return Files.walk(folder.toPath())
                .filter(p -> p.toFile().isFile())
                .mapToLong(p -> p.toFile().length())
                .sum();
    }

    public static String UnzipFile(String zipPath) throws IOException {
        String content = null;

        try(ZipFile zip = new ZipFile(zipPath)) {
            Enumeration e = zip.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();

                if (!entry.isDirectory()) {
                    content = getText(zip.getInputStream(entry));
                }
            }
        }

        return content;
    }

    public static String getZippedFileName(String zipPath) throws IOException {
        String name = null;

        try(ZipFile zip = new ZipFile(zipPath)) {
            Enumeration e = zip.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                name = entry.getName();
            }
        }

        return name;
    }

    private static String getText(InputStream in)  {
        StringBuilder sb = new StringBuilder();
        String content = null;
        String line;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }

            content = sb.toString();
            int lastIndexOfLineSeparator = content.lastIndexOf(System.lineSeparator());
            content = lastIndexOfLineSeparator == -1 ? "" : content.substring(0, lastIndexOfLineSeparator);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public static String readTextFromFile(String textFilePath) throws IOException {
        String content = null;

        try(FileInputStream inputStream = new FileInputStream(textFilePath)) {
            content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }

        return content;
    }

    public static boolean removeFile(File file) {
        return FileUtils.deleteQuietly(file);
    }

    public static void serializeObject(String pathToSave, Object object) {
        try {
            FileOutputStream file = new FileOutputStream(pathToSave);
            ObjectOutputStream out = new ObjectOutputStream(file);

            out.writeObject(object);
            out.close();
            file.close();

        } catch (IOException ignored) {}
    }

    public static Object deserializeObject(String objectPath) {
        Object object = null;

        try {
            FileInputStream file = new FileInputStream(objectPath);
            ObjectInputStream in = new ObjectInputStream(file);

            object = in.readObject();
            in.close();
            file.close();
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    public static boolean isFileExistInFolder(String folderPath, String fileName) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        boolean isExists = false;

        if (files != null) {
            for(File file: files) {
                if(file.getName().equals(fileName)) {
                    isExists = true;
                    break;
                }
            }
        }

        return isExists;
    }
}
