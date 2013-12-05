/*
    Copyright 2013 appPlant UG

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.text.Html;

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
        Boolean available   = this.isEmailAccountConfigured();
        PluginResult result = new PluginResult(PluginResult.Status.OK, available);

        ctx.sendPluginResult(result);
    }

    /**
     * Öffnet den Email-Kontroller mit vorausgefüllten Daten.
     */
    private void open (JSONArray args, CallbackContext ctx) throws JSONException {
        JSONObject properties = args.getJSONObject(0);
        Intent     draft      = this.getDraftWithProperties(properties);

        this.openDraft(draft);
    }

    /**
     * Erstellt den ViewController für Mails und fügt die übergebenen Eigenschaften ein.
     *
     * @param {JSONObject} params (Subject, Body, Recipients, ...)
     */
    private Intent getDraftWithProperties (JSONObject params) throws JSONException {
        Intent mail = new Intent(android.content.Intent.ACTION_SEND);

        if (params.has("subject"))
            this.setSubject(params.getString("subject"), mail);
        if (params.has("body"))
            this.setBody(params.getString("body"), params.optBoolean("isHtml"), mail);
        if (params.has("to"))
            this.setRecipients(params.getJSONArray("to"), mail);
        if (params.has("cc"))
            this.setCcRecipients(params.getJSONArray("cc"), mail);
        if (params.has("bcc"))
            this.setBccRecipients(params.getJSONArray("bcc"), mail);
        if (params.has("attachments"))
            this.setAttachments(params.getJSONArray("attachments"), mail);

        mail.setType("application/octet-stream");

        return mail;
    }

    /**
     * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
     */
    private void openDraft (Intent draft) {
        this.cordova.startActivityForResult(this, Intent.createChooser(draft, "Select Email App"), 0);
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
        for (int i = 0; i < attachments.length(); i++) {
            Uri attachmentUri = Uri.parse(attachments.getString(i));
            File file         = new File(attachmentUri.getPath());

            if (file.exists()) {
                draft.putExtra(Intent.EXTRA_STREAM, attachmentUri);
            }
        }
    }

    /**
     * Gibt an, ob es eine Anwendung gibt, welche E-Mails versenden kann.
     */
    private Boolean isEmailAccountConfigured () {
        Intent  intent    = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","max@mustermann.com", null));
        Boolean available = cordova.getActivity().getPackageManager().queryIntentActivities(intent, 0).size() > 1;

        return available;
    }
}
