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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

@SuppressWarnings({"Convert2Diamond", "Convert2Lambda"})
public class EmailComposer extends CordovaPlugin {

    // The log tag for this plugin
    static final String LOG_TAG = "EmailComposer";

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
        AssetUtil.cleanupAttachmentFolder(getContext());
    }

    /**
     * Executes the request.
     * <p>
     * This method is called from the WebView thread.
     * To do a non-trivial amount of work, use:
     * cordova.getThreadPool().execute(runnable);
     * <p>
     * To run on the UI thread, use:
     * cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments in JSON form.
     * @param callback The callback context used when calling
     *                 back into JavaScript.
     * @return Whether the action was valid.
     */
    @Override
    public boolean execute(String action, JSONArray args,
                           CallbackContext callback) throws JSONException {

        this.command = callback;

        if        ("open".equalsIgnoreCase(action)) {
            open(args.getJSONObject(0));
        } else if ("client".equalsIgnoreCase(action)) {
            client(args.getString(0));
        } else if ("check".equalsIgnoreCase(action)) {
            check(args.optInt(0, 0));
        } else if ("request".equalsIgnoreCase(action)) {
            request(args.optInt(0, 0));
        } else if ("clients".equalsIgnoreCase(action)) {
            clients();
        } else if ("account".equalsIgnoreCase(action)) {
            account();
        } else {
            return false;
        }

        return true;
    }

    /**
     * Returns the application context.
     */
    private Context getContext() {
        return cordova.getActivity();
    }

    /**
     * Finds out if the given mail client is installed.
     *
     * @param id The app id.
     */
    private void client(String id) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Impl impl   = new Impl(getContext());
                boolean res = impl.isAppInstalled(id);

                sendResult(new PluginResult(Status.OK, res));
            }
        });
    }

    /**
     * List of the package IDs from all available email clients.
     */
    private void clients() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Impl impl              = new Impl(getContext());
                List<String> ids       = impl.getEmailClientIds();
                List<PluginResult> res = new ArrayList<PluginResult>();

                for (String id:ids) {
                    res.add(new PluginResult(Status.OK, id));
                }

                sendResult(new PluginResult(Status.OK, res));
            }
        });
    }

    /**
     * Tries to figure out if an email account is setup.
     */
    private void account() {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Impl impl   = new Impl(getContext());
                boolean res = impl.isEmailAccountConfigured();

                sendResult(new PluginResult(Status.OK, res));
            }
        });
    }

    /**
     * Sends an intent to the email app.
     *
     * @param props The email properties like subject or body
     */
    private void open(JSONObject props) {
        final EmailComposer me = this;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                try {
                    Impl impl    = new Impl(getContext());
                    Intent draft = impl.getDraft(props);

                    cordova.startActivityForResult(me, draft, 0);
                } catch (ActivityNotFoundException e) {
                    onActivityResult(0, 0, null);
                }
            }
        });
    }

    /**
     * Check if the given permissions has been granted.
     *
     * @param code The code number of the permission to check for.
     */
    private void check(int code) {
        check(getPermission(code));
    }

    /**
     * Check if the given permission has been granted.
     *
     * @param permission The permission to check for.
     */
    private void check(String permission) {
        Boolean granted = cordova.hasPermission(permission);
        sendResult(new PluginResult(Status.OK, granted));
    }

    /**
     * Request given permission.
     *
     * @param code The code number of the permission to request for.
     */
    private void request(int code) {
        cordova.requestPermission(this, code, getPermission(code));
    }

    /**
     * Returns the corresponding permission for the internal code.
     *
     * @param code The internal code number.
     *
     * @return The Android permission string or "".
     */
    private String getPermission(int code) {
        switch (code) {
            case 1:  return READ_EXTERNAL_STORAGE;
            case 2:  return GET_ACCOUNTS;
            default: return "";
        }
    }

    /**
     * Send plugin result and reset plugin state.
     *
     * @param result The result to send to the webview.
     */
    private void sendResult(PluginResult result) {
        if (command != null) {
            command.sendPluginResult(result);
        }

        command = null;
    }

    /**
     * Called when an activity you launched exits, giving you the reqCode you
     * started it with, the resCode it returned, and any additional data from it.
     *
     * @param reqCode The request code originally supplied to startActivityForResult(),
     *                allowing you to identify who this result came from.
     * @param resCode The integer result code returned by the child activity
     *                through its setResult().
     * @param intent  An Intent, which can return result data to the caller
     *                (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        sendResult(new PluginResult(Status.OK));
    }

    /**
     * Called by the system when the user grants permissions.
     *
     * @param code         The requested code.
     * @param permissions  The requested permissions.
     * @param grantResults The grant result for the requested permissions.
     */
    @Override
    public void onRequestPermissionResult(int code, String[] permissions,
                                          int[] grantResults) {

        List<PluginResult> messages = new ArrayList<PluginResult>();
        Boolean granted             = false;

        if (grantResults.length > 0) {
            granted = grantResults[0] == PERMISSION_GRANTED;
        }

        messages.add(new PluginResult(Status.OK, granted));
        messages.add(new PluginResult(Status.OK, code));

        sendResult(new PluginResult(Status.OK, messages));
    }

}
