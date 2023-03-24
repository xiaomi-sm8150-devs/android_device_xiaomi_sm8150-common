package org.lineageos.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import androidx.preference.PreferenceManager;

import org.lineageos.settings.haptic.HapticUtils;
import org.lineageos.settings.popupcamera.PopupCameraUtils;

import vendor.lineage.livedisplay.ISunlightEnhancement;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final boolean DEBUG = false;
    private static final String TAG = "XiaomiParts";
    private static final String HBM_ENABLE_KEY = "hbm_mode";

    private static ISunlightEnhancement getSunlightEnhancementService() {
        return ISunlightEnhancement.Stub.asInterface(
                ServiceManager.waitForService(
                        ISunlightEnhancement.DESCRIPTOR + "/default"));
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (DEBUG) Log.d(TAG, "Received boot completed intent");

        HapticUtils.restoreLevel(context);
        PopupCameraUtils.checkPopupCameraService(context);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hbmEnabled = sharedPrefs.getBoolean(HBM_ENABLE_KEY, false);

        try {
            ISunlightEnhancement service = getSunlightEnhancementService();
            if (service == null) {
                Log.e(TAG, "Sunlight enhancement service not available");
                return;
            }

            service.setEnabled(hbmEnabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to restore HBM state", e);
        }
    }
}
