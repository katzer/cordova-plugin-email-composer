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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static de.appplant.cordova.emailcomposer.AssetUtil.ATTACHMENT_FOLDER;
import static de.appplant.cordova.emailcomposer.EmailComposer.LOG_TAG;

class Impl {

    // The default mailto: scheme.
    static private final String MAILTO_SCHEME = "mailto:";

    /**
     * Cleans the attachment folder.
     *
     * @param ctx   The application context.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void cleanupAttachmentFolder (Context ctx) {
        try {
            File dir = new File(ctx.getExternalCacheDir() + ATTACHMENT_FOLDER);

            if (!dir.isDirectory())
                return;

            File[] files = dir.listFiles();

            for (File file : files) { file.delete(); }
        } catch (Exception npe){
            Log.w(LOG_TAG, "Missing external cache dir");
        }
    }

    /**
     * Tells if the device has the capability to send emails.
     *
     * @param id    The app id.
     * @param ctx   The application context.
     */
    boolean[] canSendMail (String id, Context ctx) {
        // is possible with specified app
        boolean withScheme = isAppInstalled(id, ctx);
        // is possible in general
        boolean isPossible = isEmailAccountConfigured(ctx);

        return new boolean[] { isPossible, withScheme };
    }

    /**
     * The intent with the containing email properties.
     *
     * @param params    The email properties like subject or body
     * @param ctx       The context of the application.
     * @return          The resulting intent.
     */
    Intent getDraftWithProperties (JSONObject params, Context ctx)
            throws JSONException {

        Intent mail = getEmailIntent();
        String app  = params.optString("app", MAILTO_SCHEME);

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
            setAttachments(params.getJSONArray("attachments"), mail, ctx);

        if (!app.equals(MAILTO_SCHEME) && isAppInstalled(app, ctx)) {
            mail.setPackage(app);
        }

        return mail;
    }

    /**
     * Setter for the subject.
     *
     * @param subject   The subject of the email.
     * @param draft     The intent to send.
     */
    private void setSubject (String subject, Intent draft) {
        draft.putExtra(Intent.EXTRA_SUBJECT, subject);
    }

    /**
     * Setter for the body.
     *
     * @param body      The body of the email.
     * @param isHTML    Indicates the encoding (HTML or plain text).
     * @param draft     The intent to send.
     */
    private void setBody (String body, Boolean isHTML, Intent draft) {
        CharSequence text = isHTML ? Html.fromHtml(body) : body;

        draft.putExtra(Intent.EXTRA_TEXT, text);
    }

    /**
     * Setter for the recipients.
     *
     * @param recipients    List of email addresses.
     * @param draft         The intent to send.
     */
    private void setRecipients (JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];

        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }

        draft.putExtra(Intent.EXTRA_EMAIL, receivers);
    }

    /**
     * Setter for the cc recipients.
     *
     * @param recipients    List of email addresses.
     * @param draft         The intent to send.
     */
    private void setCcRecipients (JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];

        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }

        draft.putExtra(Intent.EXTRA_CC, receivers);
    }

    /**
     * Setter for the bcc recipients.
     *
     * @param recipients    List of email addresses.
     * @param draft         The intent to send.
     */
    private void setBccRecipients (JSONArray recipients, Intent draft) throws JSONException {
        String[] receivers = new String[recipients.length()];

        for (int i = 0; i < recipients.length(); i++) {
            receivers[i] = recipients.getString(i);
        }

        draft.putExtra(Intent.EXTRA_BCC, receivers);
    }

    /**
     * Setter for the attachments.
     *
     * @param attachments   List of URIs to attach.
     * @param draft         The intent to send.
     * @param ctx           The application context.
     */
    private void setAttachments (JSONArray attachments, Intent draft,
                                 Context ctx) throws JSONException {

        ArrayList<Uri> uris = new ArrayList<Uri>();
        AssetUtil assets    = new AssetUtil(ctx);

        for (int i = 0; i < attachments.length(); i++) {
            Uri uri = assets.parse(attachments.getString(i));
            if (uri != null) uris.add(uri);
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
     * If email apps are available.
     *
     * @param ctx   The application context.
     * @return      true if available, otherwise false
     */
    private boolean isEmailAccountConfigured (Context ctx) {
        AccountManager am  = AccountManager.get(ctx);

        try {
            Pattern emailPattern = Patterns.EMAIL_ADDRESS;
            for (Account account : am.getAccounts()) {
                if (emailPattern.matcher(account.name).matches()) {
                    return true;
                }
            }
        } catch (Exception e) {
            Log.i(LOG_TAG, "Missing GET_ACCOUNTS permission.");
        }

        return false;
    }

    /**
     * Ask the package manager if the app is installed on the device.
     *
     * @param id    The app id.
     * @param ctx   The application context.
     * @return      true if yes otherwise false.
     */
    private boolean isAppInstalled (String id, Context ctx) {

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
