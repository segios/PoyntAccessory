package com.ehopper.pos;

import android.content.Context;

import com.ehopper.antroshell.util.Util;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.MalformedURLException;

/**
 * Created by SStarikov on 1/19/2016.
 */
public class AppSettings {
    private static AppSettings instance = null;

    public static final String URL_FILE = "ehopper.txt";

    public static final String PREPROD = "PREPROD";
    public static final String PROD = "PROD";
    public static final String QA89 = "QA8089";
    public static final String QA91 = "QA8091";
    public static final String LOCAL = "LOCAL";

    public static final String WSPROD = "WSPROD";
    public static final String WSDEMO = "WSDEMO";
    public static final String WSQA = "WSQA";


    private static final String PREPROD_BOOT_URL = "https://qapos.ehopper.com";
    private static final String PROD_BOOT_URL = "https://pos.ehopper.com";
    private static final String QA_BOOT_URL89 = "http://nsqa.b2bsoft.com:8089/";
    private static final String QA_BOOT_URL91 = "http://nsqa.b2bsoft.com:8091/";
    private static final String LOCAL_BOOT_URL = "http://192.168.0.XX:8091/";


    private static final String WSPROD_BOOT_URL = "https://pos.b2bsoft.com/16/";
    private static final String WSDEMO_BOOT_URL = "https://demo-ehopper-pos.b2bsoft.com";
    private static final String WSQA_BOOT_URL = "https://qa-wswehopper-pos.b2bsoft.com";

    private static Context mContext;

    private final String BOOT_URL = PROD_BOOT_URL;

    //    private final String DYNATRACE_URL = "http://qaserver2008/";
    private static String filePath = null;

    private Settings settings = new Settings();

    private String appDomain;


    public static AppSettings getInstance() {
        return instance;
    }

    public static AppSettings initInstance(Context context) {
        mContext = context;
        filePath = mContext.getFilesDir() + "/" + URL_FILE;
        instance = new AppSettings();
        return instance;
    }

    private AppSettings() {
        initSettings();
    }

    public void saveSettings() {
        if (settings == null) {
            settings = new Settings(BOOT_URL);
        }

        if (settings.getUrl() == null) {
            settings.setUrl(BOOT_URL);
        }

//        if(settings.getDynaTraceUrl() == null) {
//            settings.setDynaTraceUrl(DYNATRACE_URL);
//        }

        String fileContent = Json.toJson(settings);

        FileWriter file;
        try {
            file = new FileWriter(  filePath);
            file.write(fileContent);
            file.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public static String resolveUrl(String env)
    {
        switch (env)
        {
            case PROD:
                return PROD_BOOT_URL;
            case PREPROD:
                return PREPROD_BOOT_URL;
            case QA89:
                return QA_BOOT_URL89;
            case QA91:
                return QA_BOOT_URL91;
            case LOCAL:
                return LOCAL_BOOT_URL;

            case WSPROD:
                return WSPROD_BOOT_URL;
            case WSDEMO:
                return WSDEMO_BOOT_URL;
            case WSQA:
                return WSQA_BOOT_URL;
            default:
                return PROD_BOOT_URL;
        }
    }
    private void initSettings() {
//        settings = new Settings();
//        settings.setUrl(PREPROD_BOOT_URL);
//
//        return;

//        if(ENV == PREPROD || ENV == PROD ) {
//            settings = new Settings();
//            settings.setUrl(resolveUrl());
//        }else {

        String fileContent;
        if (new File(filePath).exists()) {
            try {
                FileInputStream fIn = new FileInputStream(filePath);
                fileContent = Util.inputStream2String(fIn);

                // try deserialize
                try {
                    settings = Json.fromJson(fileContent, Settings.class);
                } catch (JsonSyntaxException e) {
                    // old format
                    if (fileContent != null) {
                        settings.setUrl(fileContent.trim());
                    }

                    saveSettings();
                }
            } catch (Exception e) {
                Logger.error(e);
            }
        } else {
            saveSettings();
        }
//        }

    }


    public void setAppUrl(String url) {
        settings.setUrl(url);
        saveSettings();
    }

    public String getAppUrl() {
        if(settings.getUrl() == null){
            initSettings();
        }

        return settings.getUrl();
    }


//    public String getDynaTraceUrl() {
//        if(settings.getDynaTraceUrl() == null){
//            initSettings();
//
//            if(settings.getDynaTraceUrl() == null) {
//                settings.setDynaTraceUrl(DYNATRACE_URL);
//                saveSettings();
//            }
//        }
//
//        return settings.getDynaTraceUrl();
////        return "";
//    }

    public String getAppDomain() {

        if(appDomain == null){
            java.net.URL url;
            try {
                url = new java.net.URL(getAppUrl());
                String host = url.getHost();
                if(host.startsWith("www")){
                    host = host.substring("www".length()+1);
                }
                appDomain = host;
                return appDomain;

            } catch (MalformedURLException e1) {
                Logger.error(e1);
            }
            return "";
        }

        return appDomain;

    }
}
