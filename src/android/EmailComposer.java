/*
    Copyright 2013-2016 appPlant UG

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
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2Diamond")
public class EmailComposer extends CordovaPlugin {

    /**
     * The log tag for this plugin
     */
    static protected final String LOG_TAG = "EmailComposer";

    // Implementation of the plugin.
    private final EmailComposerImpl impl = new EmailComposerImpl();

    // The callback context used when calling back into JavaScript
    private CallbackContext command;

    /**
     * Delete externalCacheDirectory on appstart
     *
     * @param cordova Cordova-instance
     * @param webView CordovaWebView-instance
     */
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        impl.cleanupAttachmentFolder(getContext());
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

        this.command = callback;

        if ("open".equalsIgnoreCase(action)) {
            open(args);
            return true;
        }

        if ("isAvailable".equalsIgnoreCase(action)) {
            isAvailable(args.getString(0));
            return true;
        }

        return false;
    }

    /**
     * Returns the application context.
     */
    private Context getContext() { return cordova.getActivity(); }

    /**
     * Tells if the device has the capability to send emails.
     *
     * @param id
     * The app id.
     */
    private void isAvailable (final String id) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                boolean[] available = impl.canSendMail(id, getContext());
                List<PluginResult> messages = new ArrayList<PluginResult>();

                messages.add(new PluginResult(PluginResult.Status.OK, available[0]));
                messages.add(new PluginResult(PluginResult.Status.OK, available[1]));

                PluginResult result = new PluginResult(
                        PluginResult.Status.OK, messages);

                command.sendPluginResult(result);
            }
        });
    }

    /**
     * Sends an intent to the email app.
     *
     * @param args
     * The email properties like subject or body
     * @throws JSONException
     */
    private void open (JSONArray args) throws JSONException {
        JSONObject props = args.getJSONObject(0);
        String appId     = props.getString("app");

        if (!(impl.canSendMail(appId, getContext()))[0]) {
            LOG.i(LOG_TAG, "No client or account found for.");
            return;
        }

        Intent draft  = impl.getDraftWithProperties(props, getContext());
        String header = props.optString("chooserHeader", "Open with");

        final Intent chooser = Intent.createChooser(draft, header);
        final EmailComposer plugin = this;

        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                cordova.startActivityForResult(plugin, chooser, 0);
            }
        });
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
    public void onActivityResult(int reqCode, int resCode, Intent intent) {
        if (command != null) {
            command.success();
        }
    }

}
