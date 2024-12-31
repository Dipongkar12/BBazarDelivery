package com.example.bbazardelivery;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class StoragePermissionManager {
    private Activity mActivity;

    public StoragePermissionManager(Activity activity) {
        this.mActivity = activity;
    }

    // Method to check if storage permissions are granted
    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = {
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(mActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } else {
            return true; // Automatically granted for SDK < 23
        }
    }


    // Method to request permissions
    public void requestPermissions() {
        String[] permissions = getPermissions();
        boolean shouldShowRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                shouldShowRationale = true;
                break;
            }
        }

        if (shouldShowRationale) {
            showRationaleDialog(permissions, 1);
        } else {
            ActivityCompat.requestPermissions(mActivity, permissions, 1);
        }

    }

    // Method to get the permissions based on API level
    public String[] getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            return new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }

    // Method to check if rationale should be shown or redirect to settings if denied permanently
    public void showDialog(final int requestCode) {
        String[] permissions = getPermissions();
        boolean shouldShowRationale = false;
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                shouldShowRationale = true;
                break;
            }
        }

        if (shouldShowRationale) {
            showRationaleDialog(permissions, requestCode);
        } else {
            goToSettings();
        }
    }

    // Method to show rationale dialog when permission is denied
    private void showRationaleDialog(final String[] permissions, final int requestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Permission Required")
                .setCancelable(false)
                .setMessage("This app needs storage permissions to function properly. Please grant all of them.")
                .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(mActivity, permissions, requestCode);
                    }
                })
                .setNegativeButton("NO THANKS", null)
                .show();
    }

    // Method to navigate to app settings if permissions are denied permanently
    private void goToSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Permission Required")
                .setCancelable(false)
                .setMessage("Permission was denied and cannot be asked again. Please allow permission from app settings.")
                .setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", mActivity.getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mActivity.startActivity(intent);
                    }
                })
                .setNegativeButton("NO THANKS", null)
                .show();
    }

}
