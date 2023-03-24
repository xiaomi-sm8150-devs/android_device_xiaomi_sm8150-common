package org.lineageos.settings.display;

import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.preference.PreferenceManager;

import vendor.lineage.livedisplay.ISunlightEnhancement;

public class HBMTileService extends TileService {

    private static final String TAG = "HBMTileService";
    private static final String HBM_ENABLE_KEY = "hbm_mode";

    private static ISunlightEnhancement getSunlightEnhancementService() {
        return ISunlightEnhancement.Stub.asInterface(
                ServiceManager.waitForService(
                        ISunlightEnhancement.DESCRIPTOR + "/default"));
    }

    private void updateUI(boolean enabled) {
        final Tile tile = getQsTile();
        if (tile == null) {
            return;
        }

        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        updateUI(sharedPrefs.getBoolean(HBM_ENABLE_KEY, false));
    }

    @Override
    public void onClick() {
        super.onClick();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean enabled = !sharedPrefs.getBoolean(HBM_ENABLE_KEY, false);

        try {
            ISunlightEnhancement service = getSunlightEnhancementService();
            if (service == null) {
                Log.e(TAG, "Sunlight enhancement service not available");
                return;
            }

            service.setEnabled(enabled);
            sharedPrefs.edit().putBoolean(HBM_ENABLE_KEY, enabled).apply();
            updateUI(enabled);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to toggle HBM", e);
        }
    }
}
