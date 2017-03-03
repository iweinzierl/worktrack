package de.iweinzierl.worktrack.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;

import de.iweinzierl.worktrack.BuildConfig;
import de.iweinzierl.worktrack.model.Week;
import de.iweinzierl.worktrack.model.Year;

public class MailHelper {

    public static void sendMail(Context context, Week week) throws IOException {
        if (week == null) {
            return;
        }

        SettingsHelper settingsHelper = new SettingsHelper(context);

        String csv = new CsvTransformer().transform(week);
        sendMailWithCsv(
                context,
                new String[]{settingsHelper.getDefaultEmailAddress()},
                "Worktrack Export " + week.getWeekNum() + "/" + week.getYear(),
                csv
        );
    }

    public static void sendMail(Context context, Year year) throws IOException {
        if (year == null) {
            return;
        }

        SettingsHelper settingsHelper = new SettingsHelper(context);

        String csv = new CsvTransformer().transform(year);
        sendMailWithCsv(
                context,
                new String[]{settingsHelper.getDefaultEmailAddress()},
                "Worktrack Export " + year.getYear(),
                csv
        );
    }

    private static void sendMailWithCsv(Context context, String[] receiver, String subject, String content) throws IOException {
        File csvFile = FileUtil.toFile(subject.replaceAll(" ", "") + ".csv", content);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, receiver);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider", csvFile));

        context.startActivity(intent);
    }
}
