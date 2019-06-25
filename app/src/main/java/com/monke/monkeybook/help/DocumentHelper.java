package com.monke.monkeybook.help;

import android.graphics.Bitmap;
import android.net.Uri;
import androidx.documentfile.provider.DocumentFile;
import android.text.TextUtils;

import com.monke.basemvplib.ContextHolder;
import com.monke.monkeybook.utils.DocumentUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


/**
 * Created by PureDark on 2016/9/24.
 */

public class DocumentHelper {

    public static boolean isFileExist(String fileName, String rootPath, String... subDirs) {
        return DocumentUtil.isFileExist(ContextHolder.getContext(), fileName, rootPath, subDirs);
    }

    public static DocumentFile getDirDocument(String rootPath, String... subDirs) {
        return DocumentUtil.getDirDocument(ContextHolder.getContext(), rootPath, subDirs);
    }

    public static DocumentFile createFileIfNotExist(String fileName, String path, String... subDirs) {
        Logger.d("FileHelper", "fileName:" + fileName);
        Logger.d("FileHelper", "path:" + path);
        Logger.d("FileHelper", Uri.decode(TextUtils.join("/", subDirs)));
        if (!path.startsWith("content://"))
            path = "file://" + Uri.decode(path);
        return DocumentUtil.createFileIfNotExist(ContextHolder.getContext(), fileName, path, subDirs);
    }

    public static DocumentFile createDirIfNotExist(String path, String... subDirs) {
        if (!path.startsWith("content://"))
            path = "file://" + Uri.decode(path);
        return DocumentUtil.createDirIfNotExist(ContextHolder.getContext(), path, subDirs);
    }

    public static boolean deleteFile(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.deleteFile(ContextHolder.getContext(), fileName, rootPath, subDirs);
    }

    public static boolean writeString(String string, DocumentFile file) {
        return DocumentUtil.writeBytes(ContextHolder.getContext(), string.getBytes(), file);
    }

    public static boolean writeString(String string, String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.writeBytes(ContextHolder.getContext(), string.getBytes(), fileName, rootPath, subDirs);
    }

    public static String readString(String fileName, String rootPath, String... subDirs) {
        byte[] data = DocumentUtil.readBytes(ContextHolder.getContext(), fileName, rootPath, subDirs);
        String string = null;
        try {
            string = new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String readString(Uri uri) {
        byte[] data = DocumentUtil.readBytes(ContextHolder.getContext(), uri);
        String string = null;
        try {
            string = new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static String readString(DocumentFile file) {
        byte[] data = DocumentUtil.readBytes(ContextHolder.getContext(), file);
        String string = null;
        try {
            string = new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }

    public static boolean writeBytes(byte[] data, String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.writeBytes(ContextHolder.getContext(), data, fileName, rootPath, subDirs);
    }

    public static boolean writeBytes(byte[] data, DocumentFile file) {
        if (file == null)
            return false;
        return DocumentUtil.writeBytes(ContextHolder.getContext(), data, file);
    }

    public static boolean writeFromFile(File fromFile, DocumentFile file) {
        if (file == null)
            return false;
        try {
            return DocumentUtil.writeFromInputStream(ContextHolder.getContext(), new FileInputStream(fromFile), file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFromInputStream(InputStream inStream, DocumentFile file) {
        if (file == null)
            return false;
        return DocumentUtil.writeFromInputStream(ContextHolder.getContext(), inStream, file);
    }

    public static void saveBitmapToFile(Bitmap bitmap, DocumentFile file) throws IOException {
        saveBitmapToFile(bitmap, file.getUri());
    }

    public static void saveBitmapToFile(Bitmap bitmap, Uri fileUri) throws IOException {
        OutputStream out = ContextHolder.getContext().getContentResolver().openOutputStream(fileUri);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        out.flush();
        out.close();
    }

    public static OutputStream getFileOutputSteam(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.getFileOutputSteam(ContextHolder.getContext(), fileName, rootPath, subDirs);
    }

    public static InputStream getFileInputSteam(String fileName, String rootPath, String... subDirs) {
        if (!rootPath.startsWith("content://"))
            rootPath = "file://" + Uri.decode(rootPath);
        return DocumentUtil.getFileInputSteam(ContextHolder.getContext(), fileName, rootPath, subDirs);
    }

    public static String filenameFilter(String str) {
        return DocumentUtil.filenameFilter(str);
    }

    public static byte[] getBytes(File file) {
        byte[] buffer = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
