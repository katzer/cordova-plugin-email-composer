/*
    Copyright 2013-2014 appPlant UG

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

package de.appplant.cordova.plugin.emailcomposer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.text.Html;
import android.util.Base64;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class EmailComposer extends CordovaPlugin {

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // Eine E-Mail soll versendet werden
        if ("open".equals(action)) {
            open(args, callbackContext);

            return true;
        }

        // Es soll überprüft werden, ob ein Dienst zum Versenden der E-Mail zur Verfügung steht
        if ("isServiceAvailable".equals(action)) {
            isServiceAvailable(callbackContext);

            return true;
        }

        // Returning false results in a "MethodNotFound" error.
        return false;
    }

    /**
     * Überprüft, ob Emails versendet werden können.
     */
    private void isServiceAvailable (CallbackContext ctx) {
        Boolean available   = isEmailAccountConfigured();
        PluginResult result = new PluginResult(PluginResult.Status.OK, available);

        ctx.sendPluginResult(result);
    }

    /**
     * Öffnet den Email-Kontroller mit vorausgefüllten Daten.
     */
    private void open (JSONArray args, CallbackContext ctx) throws JSONException {
        JSONObject properties = args.getJSONObject(0);
        Intent     draft      = getDraftWithProperties(properties);

        openDraft(draft);
    }

    /**
     * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
     *
     * @param {JSONObject} params (Subject, Body, Recipients, ...)
     */
    private Intent getDraftWithProperties (JSONObject params) throws JSONException {
        Intent mail = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);

        if (params.has("subject"))
            setSubject(params.getString("subject"), mail);
        if (params.has("body"))
            setBody(params.getString("body"), params.optBoolean("isHtml"), mail);
        if (params.has("to"))
            setRecipients(params.getJSONArray("to"), mail);
        if (params.has("cc"))
            setCcRecipients(params.getJSONArray("cc"), mail);
        if (params.has("bcc"))
            setBccRecipients(params.getJSONArray("bcc"), mail);
        if (params.has("attachments"))
            setAttachments(params.getJSONArray("attachments"), mail);

        mail.setType("application/octet-stream");

        return mail;
    }

    /**
     * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
     */
    private void openDraft (final Intent draft) {
        final EmailComposer plugin = this;

        cordova.getThreadPool().execute( new Runnable() {
            public void run() {
                cordova.startActivityForResult(plugin, Intent.createChooser(draft, "Select Email App"), 0);
            }
        });
    }

    /**
     * Setzt den Subject der Mail.
     */
    private void setSubject (String subject, Intent draft) {
        draft.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    }

    /**
     * Setzt den Body der Mail.
     */
    private void setBody (String body, Boolean isHTML, Intent draft) {
        if (isHTML) {
            draft.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(body));
            draft.setType("text/html");
        } else {
            draft.putExtra(android.content.Intent.EXTRA_TEXT, body);
            draft.setType("text/plain");
        }
    }

    /**
     * Setzt die Empfänger der Mail.
     */
    private void setRecipients (JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];

        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }

        draft.putExtra(android.content.Intent.EXTRA_EMAIL, receivers);
    }

    /**
     * Setzt die CC-Empfänger der Mail.
     */
    private void setCcRecipients (JSONArray ccRecipients, Intent draft) throws JSONException {
        String[] receivers = new String[ccRecipients.length()];

        for (int i = 0; i < ccRecipients.length(); i++) {
            receivers[i] = ccRecipients.getString(i);
        }

        draft.putExtra(android.content.Intent.EXTRA_CC, receivers);
    }

    /**
     * Setzt die BCC-Empfänger der Mail.
     */
    private void setBccRecipients (JSONArray bccRecipients, Intent draft) throws JSONException {
        String[] receivers = new String[bccRecipients.length()];

        for (int i = 0; i < bccRecipients.length(); i++) {
            receivers[i] = bccRecipients.getString(i);
        }

        draft.putExtra(android.content.Intent.EXTRA_BCC, receivers);
    }

    /**
     * Fügt die Anhände zur Mail hinzu.
     */
    private void setAttachments (JSONArray attachments, Intent draft) throws JSONException {
        ArrayList<Uri> attachmentUris = new ArrayList<Uri>();

        for (int i = 0; i < attachments.length(); i++) {
            Uri attachmentUri = getUriForPath(attachments.getString(i));

            attachmentUris.add(attachmentUri);
        }

        draft.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris);
    }

    /**
     * Gibt an, ob es eine Anwendung gibt, welche E-Mails versenden kann.
     */
    private Boolean isEmailAccountConfigured () {
        Intent  intent    = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","max@mustermann.com", null));
        Boolean available = cordova.getActivity().getPackageManager().queryIntentActivities(intent, 0).size() > 1;

        return available;
    }

    /**
     * Retrieves the URI for a given path.
     */
    private Uri getUriForPath (String path) {
        if (path.startsWith("relative://")) {
            String resPath = path.replaceFirst("relative://", "");
            String resName = resPath.substring(resPath.lastIndexOf('.') + 1);

            Resources res  = cordova.getActivity().getResources();
            int resId      = getResId(resPath);
            Bitmap bmp     = BitmapFactory.decodeResource(res, resId);
            String storage = Environment.getExternalStorageDirectory().toString() + "/email_composer";
            File file      = new File(storage, resName + ".png");

            new File(storage).mkdir();

            try {
                FileOutputStream outStream = new FileOutputStream(file);

                bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();

                return Uri.fromFile(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (path.startsWith("absolute://")) {
            String absPath = path.replaceFirst("absolute://", "/");
            File file      = new File(absPath);

            return Uri.fromFile(file);
        } else if (path.startsWith("base64:")) {
            String resName = path.substring(path.indexOf(":") + 1, path.indexOf("//"));
            String resData = path.substring(path.indexOf("//") +2);

            byte[] bytes   = Base64.decode(resData, 0);
            String storage = this.cordova.getActivity().getCacheDir() + "/email_composer";
            File file      = new File(storage, resName);

            new File(storage).mkdir();

            try {
                FileOutputStream os = new FileOutputStream(file, true);

                os.write(bytes);
                os.flush();
                os.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Uri.parse("content://" + AttachmentProvider.AUTHORITY + "/" + resName);
        }

        return Uri.parse(path);
    }

    /**
     * @return The resource ID for the given resource.
     */
    private int getResId (String resPath) {
        String pkgName = cordova.getActivity().getPackageName();
        String clsName = resPath.substring(0, resPath.lastIndexOf("."));
        String resName = resPath.substring(resPath.lastIndexOf('.') + 1);
        int resId      = 0;

        try {
            Class<?> klass  = Class.forName(pkgName + ".R$" + clsName);

            resId = (Integer) klass.getDeclaredField(resName).get(Integer.class);
        } catch (Exception e) {}

        return resId;
    }
}
