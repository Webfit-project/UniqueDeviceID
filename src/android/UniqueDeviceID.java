package hu.dpal.phonegap.plugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class UniqueDeviceID extends CordovaPlugin {

    public static final String TAG = "UniqueDeviceID";
    public CallbackContext callbackContext;
    public static final int REQUEST_READ_PHONE_STATE = 0;

    protected final static String permission = Manifest.permission.READ_PHONE_STATE;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;

        getDeviceId();
        return true;

    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        if(requestCode == REQUEST_READ_PHONE_STATE){
            getDeviceId();
        }
    }

    protected void getDeviceId(){

        Context context = cordova.getActivity().getApplicationContext();
        String androidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        serialNumber = "{serial:'"+serialNumber+"',uid:'"+androidID+"'}";

        this.callbackContext.success(serialNumber);
    }

    private boolean hasPermission(String permission) throws Exception{
        boolean hasPermission = true;
        Method method = null;
        try {
            method = cordova.getClass().getMethod("hasPermission", permission.getClass());
            Boolean bool = (Boolean) method.invoke(cordova, permission);
            hasPermission = bool.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v" + CordovaWebView.CORDOVA_VERSION + " does not support API 23 runtime permissions so defaulting to GRANTED for " + permission);
        }
        return hasPermission;
    }

    private void requestPermission(CordovaPlugin plugin, int requestCode, String permission) throws Exception{
        try {
            java.lang.reflect.Method method = cordova.getClass().getMethod("requestPermission", org.apache.cordova.CordovaPlugin.class ,int.class, java.lang.String.class);
            method.invoke(cordova, plugin, requestCode, permission);
        } catch (NoSuchMethodException e) {
            throw new Exception("requestPermission() method not found in CordovaInterface implementation of Cordova v" + CordovaWebView.CORDOVA_VERSION);
        }
    }
}