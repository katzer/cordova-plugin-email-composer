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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static de.appplant.cordova.emailcomposer.EmailComposer.LOG_TAG;

class Impl {

    // The default mailto: scheme.
    private static final String MAILTO_SCHEME = "mailto:";

    // The application context.
    private final Context ctx;

    /**
     * Initializes the class.
     *
     * @param ctx The application context.
     */
    Impl (Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Tells if the device has the capability to send emails.
     *
     * @param id The app id.
     */
    boolean[] canSendMail (String id) {
        // is possible with specified app
        boolean withScheme = isAppInstalled(id);
        // is possible in general
        boolean isPossible = isEmailAccountConfigured();

        return new boolean[] { isPossible, withScheme };
    }

    /**
     * The intent with the containing email properties.
     *
     * @param params    The email properties like subject or body.
     * @return          The resulting intent.
     */
    Intent getDraft (JSONObject params) {
        Intent mail = getEmailIntent();
        String app  = params.optString("app", MAILTO_SCHEME);

        if (params.has("subject"))
            setSubject(params, mail);

        if (params.has("body"))
            setBody(params, mail);

        if (params.has("to"))
            setRecipients(params, mail);

        if (params.has("cc"))
            setCcRecipients(params, mail);

        if (params.has("bcc"))
            setBccRecipients(params, mail);

        if (params.has("attachments"))
            setAttachments(params, mail);

        if (params.has("type"))
            setType(params, mail);

        if (!app.equals(MAILTO_SCHEME) && isAppInstalled(app)) {
            mail.setPackage(app);
        }

        return mail;
    }

    /**
     * Setter for the subject.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setSubject (JSONObject params, Intent draft) {
        String subject = params.optString("subject");
        draft.putExtra(Intent.EXTRA_SUBJECT, subject);
    }

    /**
     * Setter for the body.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setBody (JSONObject params, Intent draft) {
        String body       = params.optString("body");
        boolean isHTML    = params.optBoolean("isHtml");
        CharSequence text = isHTML ? Html.fromHtml(body) : body;

        draft.putExtra(Intent.EXTRA_TEXT, text);
    }

    /**
     * Setter for the recipients.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setRecipients (JSONObject params, Intent draft) {
        insertRecipients(draft, params, "to", Intent.EXTRA_EMAIL);
    }

    /**
     * Setter for the cc recipients.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setCcRecipients (JSONObject params, Intent draft) {
        insertRecipients(draft, params, "cc", Intent.EXTRA_CC);
    }

    /**
     * Setter for the bcc recipients.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setBccRecipients (JSONObject params, Intent draft) {
        insertRecipients(draft, params, "bcc", Intent.EXTRA_BCC);
    }

    /**
     * Insert the recipients into the email draft intent.
     *
     * @param draft     The intent to send.
     * @param params    The email properties like subject or body.
     * @param key       The key where to find the recipients.
     * @param extra     The key where to insert the recipients.
     */
    private void insertRecipients (Intent draft, JSONObject params,
                                   String key, String extra) {

        JSONArray recipients = params.optJSONArray(key);
        String[] receivers   = new String[recipients.length()];

        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.optString(i);
        }

        draft.putExtra(extra, receivers);
    }

    /**
     * Setter for the attachments.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setAttachments (JSONObject params, Intent draft) {

        JSONArray attachments = params.optJSONArray("attachments");
        ArrayList<Uri> uris   = new ArrayList<Uri>();
        AssetUtil assets      = new AssetUtil(ctx);

        for (int i = 0; i < attachments.length(); i++) {
            Uri uri = assets.parse(attachments.optString(i));
            if (uri != null && uri != Uri.EMPTY) uris.add(uri);
        }

        if (uris.isEmpty())
            return;

        draft.setAction(Intent.ACTION_SEND_MULTIPLE)
                .setType("message/rfc822")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_STREAM, uris);

        if (uris.size() > 1)
            return;

        draft.setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_STREAM, uris.get(0));
    }

    /**
     * Setter for the email type.
     *
     * @param params    The email properties like subject or body.
     * @param draft     The intent to send.
     */
    private void setType (JSONObject params, Intent draft) {
        String type = params.optString("type", "message/rfc822");
        draft.setType(type);
    }

    /**
     * If email apps are available.
     *
     * @return true if available, otherwise false
     */
    private boolean isEmailAccountConfigured() {
        AccountManager am  = AccountManager.get(ctx);

        try {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;

            for (Account account : am.getAccounts()) {
                if (emailPattern.matcher(account.name).matches()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.w(LOG_TAG, "Missing GET_ACCOUNTS permission.");
        }

        return false;
    }

    /**
     * Ask the package manager if the app is installed on the device.
     *
     * @param id    The app id.
     * @return      true if yes otherwise false.
     */
    private boolean isAppInstalled (String id) {

        if (id.equalsIgnoreCase(MAILTO_SCHEME)) {
            Intent intent     = getEmailIntent();
            PackageManager pm = ctx.getPackageManager();
            int apps          = pm.queryIntentActivities(intent, 0).size();

            return (apps > 0);
        }

        try {
            ctx.getPackageManager().getPackageInfo(id, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Setup an intent to send to email apps only.
     *
     * @return intent
     */
    private static Intent getEmailIntent() {
        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse(MAILTO_SCHEME));

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

}
