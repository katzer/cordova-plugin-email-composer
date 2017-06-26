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
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static de.appplant.cordova.emailcomposer.EmailComposer.LOG_TAG;

class EmailComposerImpl {

    // The default mailto: scheme.
    static private final String MAILTO_SCHEME = "mailto:";

    // Path where to put tmp the attachments.
    static private final String ATTACHMENT_FOLDER = "/email_composer";

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

        for (int i = 0; i < attachments.length(); i++) {
            Uri uri = getUriForPath(attachments.getString(i), ctx);
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
     * The URI for an attachment path.
     *
     * @param path  The given path to the attachment.
     * @param ctx   The application context.
     * @return      The URI pointing to the given path.
     */
    private Uri getUriForPath (String path, Context ctx) {
        if (path.startsWith("res:")) {
            return getUriForResourcePath(path, ctx);
        } else if (path.startsWith("app://")) {
            return getUriForAppInternalPath(path, ctx);
        } else if (path.startsWith("file:///")) {
            return getUriForAbsolutePath(path);
        } else if (path.startsWith("file://")) {
            return getUriForAssetPath(path, ctx);
        } else if (path.startsWith("base64:")) {
            return getUriForBase64Content(path, ctx);
        }

        return Uri.parse(path);
    }

    /**
     * The URI for a file.
     *
     * @param path  The given absolute path.
     * @return      The URI pointing to the given path.
     */
    private Uri getUriForAbsolutePath (String path) {
        String absPath = path.replaceFirst("file://", "");
        File file      = new File(absPath);

        if (!file.exists()) {
            Log.e(LOG_TAG, "File not found: " + file.getAbsolutePath());
        }

        return Uri.fromFile(file);
    }

    /**
     * The URI for an asset.
     *
     * @param path  The given asset path.
     * @param ctx   The application context.
     * @return      The URI pointing to the given path.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForAssetPath (String path, Context ctx) {
        String resPath  = path.replaceFirst("file:/", "www");
        String fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        File dir        = ctx.getExternalCacheDir();

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage  = dir.toString() + ATTACHMENT_FOLDER;
        File file       = new File(storage, fileName);

        new File(storage).mkdir();

        FileOutputStream outStream = null;

        try {
            AssetManager assets = ctx.getAssets();

            outStream = new FileOutputStream(file);
            InputStream inputStream    = assets.open(resPath);

            copyFile(inputStream, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            Log.e(LOG_TAG, "File not found: assets/" + resPath);
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                safeClose(outStream);
            }
        }

        return Uri.fromFile(file);
    }

    /**
     * The URI for an internal file.
     *
     * @param path The given asset path.
     * @param ctx  The application context.
     * @return     The URI pointing to the given path.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForAppInternalPath (String path, Context ctx) {
        String resPath  = path.replaceFirst("app:/", "");
        String fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        File dir        = ctx.getExternalCacheDir();

        if (dir == null) {
            Log.e("EmailComposer", "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage  = dir.toString() + ATTACHMENT_FOLDER;
        File file       = new File(storage, fileName);

        new File(storage).mkdir();
        File privateDir    = ctx.getFilesDir();
        String privatePath = privateDir.getAbsolutePath()+"/.."+resPath;

        try {
            FileOutputStream outStream = new FileOutputStream(file);
            InputStream inputStream    = new FileInputStream(privatePath);

            copyFile(inputStream, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            Log.e("EmailComposer", "File not found: " + privatePath);
            e.printStackTrace();
        }

        return Uri.fromFile(file);
    }

    /**
     * The URI for a resource.
     *
     * @param path  The given relative path.
     * @param ctx   The application context.
     * @return      The URI pointing to the given path
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForResourcePath (String path, Context ctx) {
        String resPath   = path.replaceFirst("res://", "");
        String fileName  = resPath.substring(resPath.lastIndexOf('/') + 1);
        String resName   = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = resPath.substring(resPath.lastIndexOf('.'));
        File dir         = ctx.getExternalCacheDir();

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage   = dir.toString() + ATTACHMENT_FOLDER;
        int resId        = getResId(resPath, ctx);
        File file        = new File(storage, resName + extension);

        if (resId == 0) {
            Log.e(LOG_TAG, "File not found: " + resPath);
        }

        new File(storage).mkdir();

        FileOutputStream outStream = null;

        try {
            Resources res = ctx.getResources();
            outStream = new FileOutputStream(file);
            InputStream inputStream    = res.openRawResource(resId);

            copyFile(inputStream, outStream);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                safeClose(outStream);
            }
        }

        return Uri.fromFile(file);
    }

    /**
     * The URI for a base64 encoded content.
     *
     * @param content   The given base64 encoded content.
     * @param ctx       The application context.
     * @return          The URI including the given content.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForBase64Content (String content, Context ctx) {
        String resName = content.substring(content.indexOf(":") + 1, content.indexOf("//"));
        String resData = content.substring(content.indexOf("//") + 2);
        File dir       = ctx.getExternalCacheDir();
        byte[] bytes;

        try {
            bytes = Base64.decode(resData, 0);
        } catch (Exception ignored) {
            Log.e(LOG_TAG, "Invalid Base64 string");
            return Uri.EMPTY;
        }

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage = dir.toString() + ATTACHMENT_FOLDER;
        File file      = new File(storage, resName);

        new File(storage).mkdir();

        FileOutputStream outStream = null;

        try {
            outStream = new FileOutputStream(file);

            outStream.write(bytes);
            outStream.flush();
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                safeClose(outStream);
            }
        }

        return Uri.fromFile(file);
    }

    /**
     * Writes an InputStream to an OutputStream
     *
     * @param in    The input stream.
     * @param out   The output stream.
     */
    private void copyFile (InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Returns the resource ID for the given resource path.
     *
     * @param ctx   The application context.
     * @return      The resource ID for the given resource.
     */
    private int getResId (String resPath, Context ctx) {
        Resources res = ctx.getResources();
        int resId;

        String pkgName  = ctx.getPackageName();
        String dirName  = "drawable";
        String fileName = resPath;

        if (resPath.contains("/")) {
            dirName  = resPath.substring(0, resPath.lastIndexOf('/'));
            fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        }

        String resName = fileName.substring(0, fileName.lastIndexOf('.'));

        resId = res.getIdentifier(resName, dirName, pkgName);

        if (resId == 0) {
            resId = res.getIdentifier(resName, "mipmap", pkgName);
        }

        if (resId == 0) {
            resId = res.getIdentifier(resName, "drawable", pkgName);
        }

        return resId;
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

    /**
     * Attempt to safely close the given stream.
     *
     * @param outStream The stream to close.
     * @return          true if successful, false otherwise
     */
    private static boolean safeClose (final FileOutputStream outStream) {

        if (outStream != null) {
            try {
                outStream.close();
                return true;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error attempting to safely close resource: " + e.getMessage());
            }
        }

        return false;
    }

}