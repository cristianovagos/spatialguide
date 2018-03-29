package com.paydayme.spatialguide.core.storage;

import android.content.Context;
import android.util.Log;

import com.paydayme.spatialguide.core.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cvagos on 27-03-2018.
 */

public final class InternalStorage {
    private InternalStorage() {}

    private static List<String> getFiles(Context context) {
        List<String> list = new ArrayList<>();
        File dir = context.getFilesDir();
        File[] files = dir.listFiles();
        for (File f : files)
            list.add(f.getName());
        return list;
    }

    public static List<Integer> getRouteIDs(Context context) {
        List<Integer> list = new ArrayList<>();
        List<String> files = getFiles(context);
        for(String file : files) {
            if(file.contains(Constant.ROUTE_STORAGE_SEPARATOR))
                list.add(Integer.parseInt(file.replace(Constant.ROUTE_STORAGE_SEPARATOR, "")));
        }
        return list;
    }

    public static boolean deleteFile(Context context, String filename) {
        File dir = context.getFilesDir();
        File file = new File(dir, filename);
        return file.delete();
    }

    public static void writeObject(Context context, String key, Object object) throws IOException {
        FileOutputStream fos = context.openFileOutput(key, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String key) throws IOException,
            ClassNotFoundException {
        FileInputStream fis = context.openFileInput(key);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Object object = ois.readObject();
        return object;
    }
}
