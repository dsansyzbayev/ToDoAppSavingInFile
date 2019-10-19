package kz.mobile.todoapp;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtils {

    public static void saveToFile(File file, Context context, String text,String fileName) {
        File dataFile = new File(file, fileName);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(dataFile);
            //fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fileOutputStream.write(text.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
