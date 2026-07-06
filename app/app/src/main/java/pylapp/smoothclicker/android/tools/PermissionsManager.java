package pylapp.smoothclicker.android.tools;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import pylapp.smoothclicker.android.R;

public class PermissionsManager {

    private PermissionGrantedCallback mPermissionGrantedCallback;
    private PermissionDeniedCallback mPermissionDeniedCallback;
    private PermissionRationShouldBeShownCallback mPermissionRationShouldBeShownCallback;

    private Context mContext;
    private int mPermissionType;

    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 1;
    public static final int PERMISSION_READ_EXTERNAL_STORAGE = 2;

    public static final PermissionsManager instance = new PermissionsManager();

    private static final String LOG_TAG = PermissionsManager.class.getSimpleName();

    private PermissionsManager() {
        super();
    }

    public PermissionsManager refreshContext(Context c) {
        if (c == null) throw new IllegalArgumentException("The context parameter cannot be null!");
        mContext = c;
        return this;
    }

    public void createPermissionListenerForWriteExternalStorage(final PermissionGrantedCallback callbackGranted,
                                                                final PermissionDeniedCallback callbackDenied,
                                                                final PermissionRationShouldBeShownCallback callbackRationale) {
        mPermissionType = PERMISSION_WRITE_EXTERNAL_STORAGE;
        mPermissionGrantedCallback = callbackGranted;
        mPermissionDeniedCallback = callbackDenied;
        mPermissionRationShouldBeShownCallback = callbackRationale;
    }

    public void getAndGoWithPermissionWriteExternalStorage() {
        if (!isApi23OrHigher()) {
            if (mPermissionGrantedCallback != null) {
                mPermissionGrantedCallback.onPermissionGranted();
            }
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (mPermissionRationShouldBeShownCallback != null) {
                    mPermissionRationShouldBeShownCallback.onPermissionRationaleShouldBeShown();
                }
            }
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else {
            if (mPermissionGrantedCallback != null) {
                mPermissionGrantedCallback.onPermissionGranted();
            }
        }
    }

    public void createPermissionListenerForReadExternalStorage(final PermissionGrantedCallback callbackGranted,
                                                               final PermissionDeniedCallback callbackDenied,
                                                               final PermissionRationShouldBeShownCallback callbackRationale) {
        mPermissionType = PERMISSION_READ_EXTERNAL_STORAGE;
        mPermissionGrantedCallback = callbackGranted;
        mPermissionDeniedCallback = callbackDenied;
        mPermissionRationShouldBeShownCallback = callbackRationale;
    }

    public void getAndGoWithPermissionReadExternalStorage() {
        if (!isApi23OrHigher()) {
            if (mPermissionGrantedCallback != null) {
                mPermissionGrantedCallback.onPermissionGranted();
            }
            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (mPermissionRationShouldBeShownCallback != null) {
                    mPermissionRationShouldBeShownCallback.onPermissionRationaleShouldBeShown();
                }
            }
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_EXTERNAL_STORAGE);
        } else {
            if (mPermissionGrantedCallback != null) {
                mPermissionGrantedCallback.onPermissionGranted();
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Logger.i(LOG_TAG, "Permission granted");
            if (mPermissionGrantedCallback != null) {
                mPermissionGrantedCallback.onPermissionGranted();
            }
        } else {
            Logger.i(LOG_TAG, "Permission denied");
            if (mPermissionDeniedCallback != null) {
                mPermissionDeniedCallback.onPermissionDenied();
            }
        }
    }

    public void clean() {
        mPermissionGrantedCallback = null;
        mPermissionDeniedCallback = null;
        mPermissionRationShouldBeShownCallback = null;
        mContext = null;
    }

    public void initialize(Context c) {
        if (c == null) throw new IllegalArgumentException("The context must not be null!");
        refreshContext(c);
    }

    public static boolean isApi23OrHigher() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
    }

    public interface PermissionGrantedCallback {
        void onPermissionGranted();
    }

    public interface PermissionDeniedCallback {
        void onPermissionDenied();
    }

    public interface PermissionRationShouldBeShownCallback {
        void onPermissionRationaleShouldBeShown();
    }
}