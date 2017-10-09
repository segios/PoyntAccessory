package com.ehopper.antroshell.poynt;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.ehopper.pos.Logger;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import co.poynt.api.model.AdjustTransactionRequest;
import co.poynt.api.model.Business;
import co.poynt.api.model.Customer;
import co.poynt.api.model.FundingSourceType;
import co.poynt.api.model.OrderItem;
import co.poynt.api.model.TokenResponse;
import co.poynt.api.model.Transaction;
import co.poynt.api.model.TransactionAmounts;
import co.poynt.os.model.AccessoryProvider;
import co.poynt.os.model.AccessoryProviderFilter;
import co.poynt.os.model.AccessoryType;
import co.poynt.os.model.Intents;
import co.poynt.os.model.Payment;
import co.poynt.os.model.PaymentStatus;
import co.poynt.os.model.PoyntError;
import co.poynt.os.model.PrintedReceipt;
import co.poynt.os.model.PrintedReceiptLine;
import co.poynt.os.model.PrinterStatus;
import co.poynt.os.sdk.BuildConfig;
import co.poynt.os.services.v1.IPoyntAccessoryManager;
import co.poynt.os.services.v1.IPoyntAccessoryManagerListener;
import co.poynt.os.services.v1.IPoyntBusinessReadListener;
import co.poynt.os.services.v1.IPoyntBusinessService;
import co.poynt.os.services.v1.IPoyntCustomerReadListener;
import co.poynt.os.services.v1.IPoyntCustomerService;
import co.poynt.os.services.v1.IPoyntReceiptPrintingService;
import co.poynt.os.services.v1.IPoyntReceiptPrintingServiceListener;
import co.poynt.os.services.v1.IPoyntSecondScreenService;
import co.poynt.os.services.v1.IPoyntSessionService;
import co.poynt.os.services.v1.IPoyntSessionServiceListener;

import co.poynt.os.services.v1.IPoyntSecondScreenCodeScanListener;

import co.poynt.os.services.v1.IPoyntTokenService;
import co.poynt.os.services.v1.IPoyntTokenServiceListener;
import co.poynt.os.services.v1.IPoyntTransactionService;
import co.poynt.os.services.v1.IPoyntTransactionServiceListener;
import co.poynt.os.util.AccessoryProviderServiceHelper;

public class PoyntManager {

    public PoyntManager(Activity activity){
        this.activityRef = new WeakReference<Activity>(activity);

         accessoryProviderServiceHelper = new AccessoryProviderServiceHelper(activity);

//        accessoryProviderServiceHelper = new AccessoryProviderServiceHelper(activity, new AccessoryProviderServiceHelper.AccessoryManagerConnectionCallback() {
//            @Override
//            public void onConnected(AccessoryProviderServiceHelper accessoryProviderServiceHelper) {
//
//            }
//
//            @Override
//            public void onDisconnected(AccessoryProviderServiceHelper accessoryProviderServiceHelper) {
//
//            }
//        });

    }

    private final String TAG = getClass().getSimpleName();

    private WeakReference<Activity> activityRef;

    public static final String INTENT_EXTRAS_PAYMENT = "PAYMENT";

    private AccountManager accountManager;
    private IPoyntSessionService mSessionService;
    private IPoyntBusinessService mBusinessService;
    private IPoyntSecondScreenService mSecondScreenService;
    private IPoyntReceiptPrintingService mPrintingService;
    private IPoyntTransactionService mTransactionService;
    private IPoyntCustomerService mCustomerService;
    private IPoyntTokenService tokenService;
    private AccessoryProviderServiceHelper accessoryProviderServiceHelper;
    private IPoyntAccessoryManager accessoryManager;


    Account currentAccount = null;
    String userName;

    Boolean usePoyntsScanner = false;

    Boolean isPoyntAvailable = android.os.Build.MODEL.toUpperCase().contains("POYNT");

    public boolean getIsPoyntAvailable(){
        return isPoyntAvailable;
    }

    private boolean isPoyntTerminal() {
        return "POYNT".equals(Build.MANUFACTURER);
    }

    public static String getPoyntBuildConfig(){
        String FLAVOR = BuildConfig.FLAVOR;
        String APPLICATION_ID = BuildConfig.APPLICATION_ID;
        String BUILD_TYPE = BuildConfig.BUILD_TYPE;
        String VERSION_NAME = BuildConfig.VERSION_NAME;
        int VERSION_CODE = BuildConfig.VERSION_CODE;
        boolean DEBUG = BuildConfig.DEBUG;
        return APPLICATION_ID;
    }

    public void bindServices() {
        Log.d(TAG, "binding to services...");

//        Intent serviceIntent = new Intent(context,MyService.class);
//        context.startService(serviceIntent);
        Activity  activity = activityRef.get();
        if(activity!=null) {
            try {

                activity.bindService(new Intent(IPoyntTransactionService.class.getName()),
                        mTransactionServiceConenction, Context.BIND_AUTO_CREATE);

                activity.bindService(new Intent(IPoyntCustomerService.class.getName()),
                        mCustomerServiceConnection, Context.BIND_AUTO_CREATE);

                activity.bindService(new Intent(IPoyntReceiptPrintingService.class.getName()),
                        mPrintingServiceConnection, Context.BIND_AUTO_CREATE);
                activity.bindService(new Intent(IPoyntBusinessService.class.getName()),
                        mBusinessServiceConnection, Context.BIND_AUTO_CREATE);
                activity.bindService(new Intent(IPoyntSessionService.class.getName()),
                        mSessionConnection, Context.BIND_AUTO_CREATE);
                activity.bindService(new Intent(IPoyntSecondScreenService.class.getName()),
                        mSecondScreenConnection, Context.BIND_AUTO_CREATE);

//                activity.bindService(new Intent(IPoyntAccessoryManager.class.getName()),
//                        accessoryManagerConnection, Context.BIND_AUTO_CREATE);

                //accessoryProviderServiceHelper.bindAccessoryManager();
//                accessoryProviderServiceHelper = new AccessoryProviderServiceHelper(activity);

            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public void unbindServices() {
        Log.d(TAG, "unbinding from services...");
        try {
            Activity  activity = activityRef.get();
            if(activity!=null) {

                if (getIsPoyntAvailable()) {
                    activity.unbindService(mTransactionServiceConenction);
                    activity.unbindService(mCustomerServiceConnection);

                    activity.unbindService(mPrintingServiceConnection);
                    activity.unbindService(mBusinessServiceConnection);
                    activity.unbindService(mSessionConnection);
                    activity.unbindService(mSecondScreenConnection);

//                    activity.unbindService(accessoryManagerConnection);
                }
            }

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private ServiceConnection accessoryManagerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            accessoryManager = IPoyntAccessoryManager.Stub.asInterface(iBinder);
            Log.d(TAG, "onServiceConnected ");

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "onServiceDisconnected ");
            accessoryManager = null;
        }
    };

    private ServiceConnection mTransactionServiceConenction = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mTransactionService = IPoyntTransactionService.Stub.asInterface(iBinder);
            Log.d(TAG, "Printing service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTransactionService = null;
        }
    };

    private ServiceConnection mPrintingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mPrintingService = IPoyntReceiptPrintingService.Stub.asInterface(iBinder);
            Log.d(TAG, "Printing service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mPrintingService = null;
        }
    };

    private ServiceConnection mSessionConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e("TransactionTestActivity", "PoyntSessionService is now connected");
            // Following the example above for an AIDL interface,
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            mSessionService = IPoyntSessionService.Stub.asInterface(service);

            isPoyntAvailable = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.e("TransactionTestActivity", "PoyntSessionService has unexpectedly disconnected");
            mSessionService = null;
        }
    };

    private ServiceConnection mSecondScreenConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "IPoyntSecondScreenService is now connected");
            // Following the example above for an AIDL interface,
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            mSecondScreenService = IPoyntSecondScreenService.Stub.asInterface(service);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "IPoyntSecondScreenService has unexpectedly disconnected");
            mSecondScreenService = null;
        }
    };

    private ServiceConnection mBusinessServiceConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "PoyntBusinessService is now connected");
            // this gets an instance of the IRemoteInterface, which we can use to call on the service
            mBusinessService = IPoyntBusinessService.Stub.asInterface(service);

            // first load business and business users to make sure the device resolves to a business
            // invoke the api to get business details
            try {
                mBusinessService.getBusiness(businessReadServiceListener);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to connect to business service to resolve the business this terminal belongs to!");
                Logger.error(e);
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "PoyntBusinessService has unexpectedly disconnected");
            mBusinessService = null;
        }
    };

    private ServiceConnection mCustomerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mCustomerService = IPoyntCustomerService.Stub.asInterface(iBinder);
            Log.d(TAG, "PoyntCustomerService is now connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "PoyntCustomerService disconnected");
        }
    };


    private IPoyntCustomerReadListener customerServiceListener = new IPoyntCustomerReadListener.Stub(){
        @Override
        public void onResponse(Customer customer, PoyntError poyntError) throws RemoteException {
            if (customer != null){
                for (Map.Entry pair : customer.getEmails().entrySet()) {
                    Log.d(TAG, "CustomerReadListener: " + pair.getKey() + ": " + pair.getValue());
                }
            }
        }
    };

    private IPoyntReceiptPrintingServiceListener printListener = new IPoyntReceiptPrintingServiceListener.Stub() {
        @Override
        public void printQueued() throws RemoteException {
            Log.d(TAG, "printQueued");
        }

        @Override
        public void printFailed(PrinterStatus printerStatus) throws RemoteException {
            Log.d(TAG, "PrintFailed - " + printerStatus.getMessage()+"; Code: " +  printerStatus.getCode());
        }

    };

    private IPoyntSessionServiceListener sessionServiceListener = new IPoyntSessionServiceListener.Stub() {

        @Override
        public void onResponse(final Account account, PoyntError poyntError) throws RemoteException {
            Activity  activity = activityRef.get();
            if(activity!=null) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (account != null) {
                            userName = account.name;
                            currentAccount = account;
                        } else {
                            currentAccount = null;
                            userName = null;
                        }
                    }
                });
            }
        }
    };

    private IPoyntBusinessReadListener businessReadServiceListener = new IPoyntBusinessReadListener.Stub() {
        @Override
        public void onResponse(final Business business, PoyntError poyntError) throws RemoteException {
            Log.d(TAG, "Received business obj:" + business.getDoingBusinessAs() + " -- " + business.getDescription());
            Activity  activity = activityRef.get();
            if(activity!=null) {

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                    bizInfo.setText(business.getDoingBusinessAs());
//                    chargeBtn.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    };


    public void findCashDrawer(final List<AccessoryProvider> resultList, final CountDownLatch countDownLatch) {
        Activity  activity = activityRef.get();
        if(activity!=null) {

            IPoyntAccessoryManager accessoryManager = accessoryProviderServiceHelper.getAccessoryServiceManager();

            try {
                accessoryManager.getAccessoryProviders(new AccessoryProviderFilter(AccessoryType.CASH_DRAWER), new IPoyntAccessoryManagerListener() {
                    @Override
                    public void onError(PoyntError poyntError) throws RemoteException {

                        countDownLatch.countDown();
                    }

                    @Override
                    public void onSuccess(List<AccessoryProvider> list) throws RemoteException {
                        if (list != null) {
                            for (AccessoryProvider provider : list) {
                                resultList.add(provider);
                            }
                        }
                        countDownLatch.countDown();
                    }

                    @Override
                    public IBinder asBinder() {
                        return null;
                    }
                });
            } catch (Throwable e) {
                Logger.error(e);
            }
        }
    }

    public void resetSecondScreen() {
        try {
            if (mSecondScreenService != null) {
                mSecondScreenService.displayWelcome(null, null, null);
            }
        } catch (RemoteException e) {
            Logger.error(e);
        }
    }

}
