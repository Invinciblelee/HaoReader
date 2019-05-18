package com.monke.monkeybook.help;

import com.monke.monkeybook.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ErrorReport {

    public static void saveError(String error) {
        if (StringUtils.isNotBlank(error)) {
            File file = FileHelp.getFile(Constant.AUDIO_BOOK_ERROR, "error_" + System.currentTimeMillis() + ".txt");
            //获取流并存储
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(error);
                writer.flush();
            } catch (IOException ignore) {
            }
        }
    }

}
