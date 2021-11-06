package ru.hse.edu.aaarifkhanov192.supportiveclasses;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

/**
 * Методы и поля для работы с файлом.
 */
public class FileManager {
    private String saveHash;    //Хэш сохраненного файла в saveFile.
    private String filePath;
    private TextCanvas activeCanvas;

    public void setActiveCanvas(TextCanvas activeCanvas) {
        this.activeCanvas = activeCanvas;
    }

    public String getSaveHash() {
        return saveHash;
    }

    public void setSaveHash(String saveHash) {
        this.saveHash = saveHash;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Считывает текст по заданному пути файла <code>path</code>.
     */
    public String readText() {
        StringBuilder txt = new StringBuilder();
        try {
            File file = new File(filePath);
            if(!file.canExecute()){
                return txt.toString();
            }
            Scanner scan = new Scanner(file);
            while (scan.hasNextLine()) {
                String data = scan.nextLine();
                txt.append(data).append("\n");
            }
            scan.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return txt.toString();
    }

    /**
     * Сохранение файла.
     * @param x Ключ для Debounce.
     */
    public void saveFile(Integer x) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(activeCanvas.text.toString());
            saveHash = getHash(activeCanvas.text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Вычисляет и возвращает хэш-значение строки <code>text</code>.
     *
     * @param text Строка.
     * @return Вычисляет и возвращает хэш-значение строки <code>text</code>.
     */
    public String getHash(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] dataBytes = text.getBytes();
        byte[] mdBytes = new byte[0];
        if (md != null) {
            mdBytes = md.digest(dataBytes);
        }

        //Конверт байтов в hex.
        StringBuilder sb = new StringBuilder();
        for (byte b : mdBytes) {
            sb.append(Integer.toString((b & 0xff), 16).substring(1));
        }

        return sb.toString();
    }
}
