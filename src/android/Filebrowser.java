package com.ourcodeworld.plugins.filebrowser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import com.nononsenseapps.filepicker.FilePickerActivity;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Filebrowser extends CordovaPlugin {
    private static int FILE_PICKER_CODE = 0x123987;
    private CallbackContext callback = null;

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        JSONObject arg_object = data.getJSONObject(0);
        String startDirectory = arg_object.getString("start_directory");
        callback = callbackContext;

        try {
            Context context = cordova.getActivity().getApplicationContext();
            Intent intent = new Intent(context, FilePickerActivity.class);

            if (startDirectory.equals("default")) {
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            } else {
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, startDirectory);
            }

            // Start single filepicker
            if (action.equals("showPicker")) {
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                // Start multi filepicker
            } else if (action.equals("showMultiFilepicker")) {
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                // Start folder (dir) picker
            } else if (action.equals("showFolderpicker")) {
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                // Start multi folder (dir) picker
            } else if (action.equals("showMultiFolderpicker")) {
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            } else if (action.equals("showMixedPicker")) {
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE_AND_DIR);
            } else if (action.equals("showCreatefile")) {
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            }

            cordova.startActivityForResult((CordovaPlugin) this, intent, FILE_PICKER_CODE);
        } catch (ActivityNotFoundException e) {
            PluginResult errorResults = new PluginResult(PluginResult.Status.ERROR, "Could not start FilePickerActivity");
            errorResults.setKeepCallback(true);
            callback.sendPluginResult(errorResults);
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        return true;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Retrieve file, folders paths
        if (requestCode == FILE_PICKER_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                JSONArray jsonArray = new JSONArray();

                if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                    // For JellyBean and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        ClipData clip = data.getClipData();

                        if (clip != null) {
                            for (int i = 0; i < clip.getItemCount(); i++) {
                                jsonArray.put(clip.getItemAt(i).getUri().toString());
                            }
                        }
                        // For Ice Cream Sandwich
                    } else {
                        ArrayList<String> paths = data.getStringArrayListExtra(FilePickerActivity.EXTRA_PATHS);

                        if (paths != null) {
                            for (String path : paths) {
                                jsonArray.put(Uri.parse(path).toString());
                            }
                        }
                    }
                } else {
                    jsonArray.put(data.getData().toString());
                }

                PluginResult result = new PluginResult(PluginResult.Status.OK, jsonArray);
                callback.sendPluginResult(result);
            } else {
                PluginResult result = new PluginResult(PluginResult.Status.OK, new JSONArray());
                callback.sendPluginResult(result);
            }
        }
    }
}
