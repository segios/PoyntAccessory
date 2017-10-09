package com.ehopper.pos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ehopper.antroshell.entities.PrinterDescriptor;
import com.ehopper.antroshell.entities.PrinterManaufacture;
import com.ehopper.antroshell.entities.PrinterType;
import com.ehopper.antroshell.poynt.PoyntManager;
import com.ehopper.antroshell.poynt.PoyntServiceClient;
import com.ehopper.antroshell.util.Util;

import android.app.ActivityManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ClientCertRequest;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import co.poynt.os.model.AccessoryProvider;

public class ShellActivity extends FragmentActivity {
    public static final int INSTALL_KEYSTORE_CODE = 333;

    private static final String TAG = "ShellActivity";

    private static ShellActivity instance = null;

    public static ShellActivity getInstance() {
        return instance;
    }

    private PoyntManager poyntManager;
    public PoyntManager getPoyntManager() {
        return poyntManager;
    }

    ListView lvDevices;
    ArrayAdapter<String> adtDevices;
    List<String> lstDevices = new ArrayList<String>();

    private void showErrorMessage(final String msg)
    {
        this.runOnUiThread(
                new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shell);
        instance = this;

        poyntManager = new PoyntManager(this);

        Logger.info("ShellActivity created.");


        boolean viewportSupported = true;

        if (poyntManager.getIsPoyntAvailable()) {
            setScreenOrientation("PORTRAIT");
        } else {
            setScreenOrientation("LANDSCAPE");
        }

        lvDevices = (ListView) this.findViewById(R.id.lvCashDrawers);
        adtDevices = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lstDevices);
        lvDevices.setAdapter(adtDevices);


        Button btnFindCashDrawer = (Button) findViewById(R.id.btnFindCashDrawer);

        btnFindCashDrawer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                lstDevices.clear();

                new FindCashDrawerTask(instance, new FindCashDrawerTaskClient() {
                    @Override
                    public void onFinished(final ArrayList<PrinterDescriptor> data) {

                        instance.runOnUiThread(
                                new Runnable() {
                                    public void run() {
                                        instance.SetCashDrawersresult(data);
                                    }
                                }
                        );
                    }
                }).execute();
            }
        });

    }

    private void SetCashDrawersresult(ArrayList<PrinterDescriptor> data)
    {
        for(int i = 0; i < data.size(); i++){
            String str = data.get(i).getFullAddress();
            if (lstDevices.indexOf(str) == -1) {
                lstDevices.add(str); // Get devices name and IP address
            }
        }

        adtDevices.notifyDataSetChanged(); //Refresh the list

    }

    public interface FindCashDrawerTaskClient
    {
        void onFinished(ArrayList<PrinterDescriptor> data);
    }

    public class FindCashDrawerTask extends AsyncTask<String, Integer, ArrayList<PrinterDescriptor>> {
        Activity context;
        FindCashDrawerTaskClient client;

        public FindCashDrawerTask(Activity context, FindCashDrawerTaskClient client) {
            this.context = context;
            this.client = client;
        }

        protected ArrayList<PrinterDescriptor> doInBackground(String... param) {
            ArrayList<PrinterDescriptor> results = searchPrinters(true);

            return results;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onPostExecute(ArrayList<PrinterDescriptor> result) {
            if (result != null) {
                if (this.client != null) {
                    this.client.onFinished(result);
                }
            }
        }
    }

    public ArrayList<PrinterDescriptor> searchPrinters(boolean isCashDrawer)  {
        ArrayList<PrinterDescriptor> printerDecriptorList = new ArrayList<PrinterDescriptor>();

        List<AccessoryProvider> list = new ArrayList<AccessoryProvider>();
        CountDownLatch countDownLatch = new CountDownLatch(1);

        if (poyntManager.getIsPoyntAvailable()) {
            if (isCashDrawer) {
                poyntManager.findCashDrawer(list, countDownLatch);
                Boolean result;

                try {
                    result = countDownLatch.await(30, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for(AccessoryProvider accessoryProvider : list)
                {
                    if(accessoryProvider.isConnected()){
                        createPrinterDescriptor(PrinterType.POYNT, isCashDrawer, accessoryProvider);
                    }
                }

            } else {
                printerDecriptorList.add(createPrinterDescriptor(PrinterType.POYNT, isCashDrawer, null));
            }
        }


        return printerDecriptorList;
    }

    public static PrinterDescriptor createPrinterDescriptor(PrinterType printerType, boolean isDrawer, AccessoryProvider accessoryProvider)
    {

        PrinterDescriptor printerDescriptor = new PrinterDescriptor();
        printerDescriptor.setFullAddress("POYNT PRINTER");
        printerDescriptor.setAddress("POYNT");
        printerDescriptor.setModelName("POYNT");
        printerDescriptor.setPrinterType(PrinterType.POYNT);
        printerDescriptor.setPaperSizeName("58-nopadding");
        printerDescriptor.setPaperSizeNotSwitchable(true);
        printerDescriptor.setScale(PrinterType.POYNT.scale());

        printerDescriptor.setPrinterManaufacture(PrinterManaufacture.POYNT);
        printerDescriptor.setPrintableAreaWidth(576);
        printerDescriptor.setDpi(203);
        printerDescriptor.setDpiX(203);
        printerDescriptor.setDpiY(203);


        if(isDrawer && accessoryProvider != null) {
            int id = accessoryProvider.getId();
            printerDescriptor.setFullAddress(accessoryProvider.getAssignedName());
            printerDescriptor.setAddress(id + ":" + accessoryProvider.getAppId() + ":" + accessoryProvider.getAssignedName());
        }

        return printerDescriptor;
    }


    @Override
    protected void onDestroy() {
        Logger.info("ShellActivity destroyed");
        super.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (savedInstanceState != null) {
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (poyntManager.getIsPoyntAvailable()) {
            poyntManager.unbindServices();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (poyntManager.getIsPoyntAvailable()) {
            poyntManager.bindServices();
        }

    }

    private String screenOrientation;

    public void setScreenOrientation(String type) {
        if (type == "PORTRAIT") {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (type == "LANDSCAPE") {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }

        screenOrientation = type;

    }

    public String getScreenOrientation() {
        return screenOrientation;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Received onActivityResult (" + requestCode + ")");
    }


    public String getPlatform() {
        String platform = "android";
        if (poyntManager.getIsPoyntAvailable()) {
            platform += ".poynt";
        }
        return platform;
    }

}