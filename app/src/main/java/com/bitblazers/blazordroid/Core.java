package com.bitblazers.blazordroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Core {
    public static boolean TestMode = false;

    //Bitblazers
    public static String ApiKey = "ca-app-pub-blazordroid-id-fake";
    public static String BannerID ="ca-app-pub-blazordroid-id-fake/blazordroid-id-fake";
    public static String InterstitialID = "ca-app-pub-blazordroid-id-fake/blazordroid-id-fake";
    public static String RewardID = "ca-app-pub-blazordroid-id-fake/blazordroid-id-fake";

    public static String AdResponse;
    public static String FileResponse;
    public static long LastAdTime;

    public static long AdDelay = 120;

    public static String AdDisableFile = "Addis.json";
    public static String ToastEnableFile = "Toasten.json";

    public static boolean AdDisabled = false;
    public static boolean ToastEnabled = false;

    public static AdRequest GetNewAdRequest() {
        return new AdRequest.Builder().build();
    }

    public static boolean writeStringAsFile(Context context, String fileName, String fileContents) {
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            Writer out = new OutputStreamWriter(fos);
            out.write(fileContents);
            out.close();
            return true;
        } catch (Exception e) {
            Log.e("Write File", e.toString());
        }
        return false;
    }

    public static String readFileAsString(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader r = new BufferedReader(new InputStreamReader(fis));
            String line = "";
            String s;
            while ((s = r.readLine()) != null) {
                line += s;
            }
            r.close();
            return line;
        } catch (Exception e) {
            Log.e("Write File", e.toString());
            return "";
        }
    }

    public static void Share(Activity x, String s) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share With Your Friends...");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, s);
        x.startActivity(Intent.createChooser(intent, ""));
    }
}

