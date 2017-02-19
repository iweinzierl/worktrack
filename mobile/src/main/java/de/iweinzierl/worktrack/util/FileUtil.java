package de.iweinzierl.worktrack.util;

import android.os.Environment;

import com.github.iweinzierl.android.logging.AndroidLoggerFactory;

import org.slf4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtil {

    private static final Logger LOGGER = AndroidLoggerFactory.getInstance().getLogger("FileUtil");

    public static File toFile(String filename, String content) throws IOException {
        File dir = Environment.getExternalStorageDirectory();
        File file = new File(dir, filename);

        LOGGER.debug("Write to file: {}", file.getAbsolutePath());
        LOGGER.debug("Content: {}", content);

        if (file.exists()) {
            file.delete();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
        writer.write(content);
        writer.flush();

        return file;
    }
}
