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
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.content.Intent.ACTION_SENDTO;
import static android.content.Intent.EXTRA_INITIAL_INTENTS;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP;
import static de.appplant.cordova.emailcomposer.EmailComposer.LOG_TAG;

@SuppressWarnings("Convert2Diamond")
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
     * The intent with the containing email properties.
     *
     * @param params    The email properties like subject or body.
     * @return          The resulting intent.
     */
    Intent getDraft (JSONObject params) {
        Intent draft  = getFilledEmailIntent(params);
        String app    = params.optString("app", MAILTO_SCHEME);
        String header = params.optString("chooserHeader", "Open with");

        if (!app.equals(MAILTO_SCHEME) && isAppInstalled(app)) {
            return draft.setPackage(app);
        }

        List<Intent> targets = new ArrayList<>();

        for (String clientId : getEmailClientIds()) {
            Intent target = (Intent) draft.clone();
            targets.add(target.setPackage(clientId));
        }

        return Intent.createChooser(targets.remove(0), header)
                .putExtra(EXTRA_INITIAL_INTENTS, targets.toArray(new Parcelable[0]));
    }

    /**
     * The intent with the containing email properties.
     *
     * @param params    The email properties like subject or body.
     * @return          The resulting intent.
     */
    private Intent getFilledEmailIntent (JSONObject params) {
        Intent draft = getEmailIntent();

        if (params.has("subject"))
            setSubject(params, draft);

        if (params.has("body"))
            setBody(params, draft);

        if (params.has("to"))
            setRecipients(params, draft);

        if (params.has("cc"))
            setCcRecipients(params, draft);

        if (params.has("bcc"))
            setBccRecipients(params, draft);

        if (params.has("attachments"))
            setAttachments(params, draft);

        return draft;
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
        String body       = fixLineBreaks(params.optString("body"));
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
                .setType("*/*")
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
     * @return true if available, otherwise false
     */
    @SuppressLint("MissingPermission")
    boolean isEmailAccountConfigured() {
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
     * Get the info for all available email client activities.
     */
    private List<ActivityInfo> getEmailClients() {
        Intent           intent = getEmailIntent();
        PackageManager       pm = ctx.getPackageManager();
        List<ResolveInfo>  apps = pm.queryIntentActivities(intent, 0);
        List<ActivityInfo> list = new ArrayList<>();

        for (ResolveInfo app : apps) {
            if (app.activityInfo.isEnabled()) {
                list.add(app.activityInfo);
            }
        }

        return list;
    }

    /**
     * Get package IDs for all available email clients.
     */
    List<String> getEmailClientIds() {
        List<String> ids = new ArrayList<>();

        for (ActivityInfo app : getEmailClients()) {
            ids.add(app.packageName);
        }

        return ids;
    }

    /**
     * Ask the package manager if the app is installed on the device.
     *
     * @param id    The app id.
     *
     * @return true if yes otherwise false.
     */
    boolean isAppInstalled (String id) {

        if (id.equalsIgnoreCase(MAILTO_SCHEME)) {
            Intent intent     = getEmailIntent();
            PackageManager pm = ctx.getPackageManager();
            int apps          = pm.queryIntentActivities(intent, 0).size();

            return (apps > 0);
        }

        try {
            return ctx.getPackageManager()
                    .getPackageInfo(id, 0)
                    .applicationInfo.enabled;
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
        Intent intent = new Intent(ACTION_SENDTO, Uri.parse(MAILTO_SCHEME));

        intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(FLAG_ACTIVITY_PREVIOUS_IS_TOP);

        return intent;
    }

    /**
     * Fix line breaks within the provided text.
     *
     * @param text The text where to fix the line breaks.
     *
     * @return The fixed text.
     */
    private static String fixLineBreaks (String text) {
        return text.replaceAll("\r\n", "\n");
    }

}
