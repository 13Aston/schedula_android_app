package com.aston.tanion.schedule.utility;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Aston Tanion on 22/03/2016.
 */
public class CommonMethod {
    private static final String TAG = "CommonMethod";

    public static String timeComponent(int time) {
        if (time < 10) return "0" + time;
        else return "" + time;
    }

    public static String timeStringFormat(int time) {
        int hours = time / 60;
        int minutes = time % 60;

        String hour;
        String minute;

        if (hours < 10) {
            hour = "0" + hours;
        } else {
            hour = "" + hours;
        }

        if (minutes < 10) {
            minute = "0" + minutes;
        } else {
            minute = "" + minutes;
        }
        return (hour + ":" + minute);
    }

    public static long[] getVibrator (int position) {
        long[] pattern;

        switch (position) {
            case 0:
                pattern = new long[]{0, 100, 10, 200, 30, 500, 80, 1300}; // Fibonacci
                break;
            case 1:
                pattern = new long[]{0, 300, 10, 400, 10, 500, 90, 200}; // Pi
                break;
            case 2:
                pattern = new long[]{0, 200, 70, 100, 80, 200, 80, 100}; // E
                break;
            case 3:
                pattern = new long[]{0, 100, 60, 100, 80, 0, 30, 300}; // Phi
                break;
            default:
                return null;
        }

        return pattern;
    }

    public static void createFile(final Context context,
                                  final String name, final String fileContent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileOutputStream outputStream = context
                            .openFileOutput(name, Context.MODE_PRIVATE);

                    outputStream.write(fileContent.getBytes());
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String readFile(Context context, String fileName) {
        StringBuffer buffer = new StringBuffer("");

        FileInputStream inputStream = null;
        BufferedReader bufferedReader;

        try {
            inputStream = context.openFileInput(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            String readString = bufferedReader.readLine();
            while (readString != null) {
                buffer.append(readString + "\n");
                readString = bufferedReader.readLine();
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return buffer.toString();
    }
}