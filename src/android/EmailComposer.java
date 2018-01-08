/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package de.appplant.cordova.emailcomposer;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.GET_ACCOUNTS;

@SuppressWarnings("Convert2Diamond")
public class EmailComposer extends CordovaPlugin {

    // The log tag for this plugin
    static final String LOG_TAG = "EmailComposer";

    // Required permissions to work properly
    private static final String PERMISSION = GET_ACCOUNTS;

    private JSONArray args;

    // Request codes used to determine what to do after they have been
    // granted or denied by the user.
    private static final int EXEC_AVAIL_AFTER = 0;
    private static final int EXEC_CHECK_AFTER = 1;

    // The callback context used when calling back into JavaScript
    private CallbackContext command;

    /**
     * Delete externalCacheDirectory on app start
     *
     * @param cordova Cordova-instance
     * @param webView CordovaWebView-instance
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        new AssetUtil(getContext()).cleanupAttachmentFolder();
    }

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling
     *                 back into JavaScript.
     * @return         Whether the action was valid.
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback) throws JSONException {

        this.args    = args;
        this.command = callback;

        if ("open".equalsIgnoreCase(action)) {
            open(args.getJSONObject(0));
        }
        else if ("isAvailable".equalsIgnoreCase(action)) {
            if (cordova.hasPermission(PERMISSION)) {
                isAvailable(args.getString(0));
            } else {
                requestPermissions(EXEC_AVAIL_AFTER);
            }
        }
        else if ("hasPermission".equalsIgnoreCase(action)) {
            hasPermission();
        }
        else if ("requestPermission".equalsIgnoreCase(action)) {
            requestPermissions(EXEC_CHECK_AFTER);
        }
        else {
            return false;
        }

        return true;
    }

    /**
     * Returns the application context.
     */
    private Context getContext() { return cordova.getActivity(); }

    /**
     * Tells if the device has the capability to send emails.
     *
     * @param id The app id.
     */
    private void isAvailable (final String id) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Impl impl     = new Impl(getContext());
                boolean[] res = impl.canSendMail(id);
                List<PluginResult> messages = new ArrayList<PluginResult>();

                messages.add(new PluginResult(PluginResult.Status.OK, res[0]));
                messages.add(new PluginResult(PluginResult.Status.OK, res[1]));

                PluginResult result = new PluginResult(
                        PluginResult.Status.OK, messages);

                command.sendPluginResult(result);
            }
        });
    }

    /**
     * Sends an intent to the email app.
     *
     * @param props The email properties like subject or body
     */
    private void open (JSONObject props) throws JSONException {
        Impl impl     = new Impl(getContext());
        Intent draft  = impl.getDraft(props);
        String header = props.optString("chooserHeader", "Open with");

        final Intent chooser       = Intent.createChooser(draft, header);
        final EmailComposer plugin = this;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                cordova.startActivityForResult(plugin, chooser, 0);
            }
        });
    }

    /**
     * Check if the required permissions are granted.
     */
    private void hasPermission() {
        Boolean hasPermission = cordova.hasPermission(PERMISSION);

        PluginResult result = new PluginResult(
                PluginResult.Status.OK, hasPermission);

        command.sendPluginResult(result);
    }

    /**
     * Request permission to read account details.
     *
     * @param requestCode The code to attach to the request.
     */
    @Override
    public void requestPermissions (int requestCode) {
        cordova.requestPermission(this, requestCode, PERMISSION);
    }

    /**
     * Called when an activity you launched exits, giving you the reqCode you
     * started it with, the resCode it returned, and any additional data from it.
     *
     * @param reqCode     The request code originally supplied to startActivityForResult(),
     *                    allowing you to identify who this result came from.
     * @param resCode     The integer result code returned by the child activity
     *                    through its setResult().
     * @param intent      An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult (int reqCode, int resCode, Intent intent) {
        if (command != null) {
            command.success();
        }
    }

    /**
     * Called by the system when the user grants permissions.
     *
     * @param code The requested code.
     * @param permissions The requested permissions.
     * @param grantResults The grant result for the requested permissions.
     */
    @Override
    public void onRequestPermissionResult (int code, String[] permissions,
                                           int[] grantResults) {
        try {
            switch (code) {
                case EXEC_CHECK_AFTER:
                    hasPermission();
                    break;

                case EXEC_AVAIL_AFTER:
                    isAvailable(this.args.getString(0));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
