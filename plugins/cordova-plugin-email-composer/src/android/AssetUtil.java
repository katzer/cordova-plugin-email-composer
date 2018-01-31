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
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import static de.appplant.cordova.emailcomposer.EmailComposer.LOG_TAG;

final class AssetUtil {

    // Path where to put tmp the attachments.
    private static final String ATTACHMENT_FOLDER = "/email_composer";

    // Application context
    private final Context ctx;

    /**
     * Initializes the asset utils.
     *
     * @param ctx The application context.
     */
    AssetUtil (Context ctx) {
        this.ctx = ctx;
    }

    /**
     * Cleans the attachment folder.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void cleanupAttachmentFolder() {
        try {
            String path = ctx.getExternalCacheDir() + ATTACHMENT_FOLDER;
            File dir    = new File(path);

            if (!dir.isDirectory())
                return;

            File[] files = dir.listFiles();

            for (File file : files) { file.delete(); }
        } catch (Exception npe){
            Log.w(LOG_TAG, "Missing external cache dir");
        }
    }

    /**
     * The URI for an attachment path.
     *
     * @param path  The given path to the attachment.
     * @return      The URI pointing to the given path.
     */
    Uri parse (String path) {
        Uri uri;

        if (path.startsWith("res:")) {
            uri = getUriForResourcePath(path);
        } else if (path.startsWith("app://")) {
            uri = getUriForAppInternalPath(path);
        } else if (path.startsWith("file:///")) {
            uri = getUriForAbsolutePath(path);
        } else if (path.startsWith("file://")) {
            uri = getUriForAssetPath(path);
        } else if (path.startsWith("base64:")) {
            uri = getUriForBase64Content(path);
        } else {
            uri = Uri.parse(path);
        }

        return uri;
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
            Log.e(LOG_TAG, "File not found: " + absPath);
        }

        return getUriForFile(ctx, file);
    }

    /**
     * The URI for an asset.
     *
     * @param path  The given asset path.
     * @return      The URI pointing to the given path.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForAssetPath (String path) {
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

        try {
            AssetManager assets  = ctx.getAssets();
            InputStream in       = assets.open(resPath);
            FileOutputStream out = new FileOutputStream(file);
            copyFile(in, out);
        } catch (Exception e) {
            Log.e(LOG_TAG, "File not found: " + resPath);
            e.printStackTrace();
        }

        return getUriForFile(ctx, file);
    }

    /**
     * The URI for an internal file.
     *
     * @param path The given asset path.
     * @return     The URI pointing to the given path.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForAppInternalPath (String path) {
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

        File filesDir  = ctx.getFilesDir();
        String absPath = filesDir.getAbsolutePath() + "/.." + resPath;

        try {
            InputStream in       = new FileInputStream(absPath);
            FileOutputStream out = new FileOutputStream(file);
            copyFile(in, out);
        } catch (Exception e) {
            Log.e(LOG_TAG, "File not found: " + absPath);
            e.printStackTrace();
        }

        return getUriForFile(ctx, file);
    }

    /**
     * The URI for a resource.
     *
     * @param path  The given relative path.
     * @return      The URI pointing to the given path
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForResourcePath (String path) {
        String resPath   = path.replaceFirst("res://", "");
        String fileName  = resPath.substring(resPath.lastIndexOf('/') + 1);
        String resName   = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = resPath.substring(resPath.lastIndexOf('.'));
        File dir         = ctx.getExternalCacheDir();
        int resId        = getResId(resPath);

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        if (resId == 0) {
            Log.e(LOG_TAG, "File not found: " + resPath);
        }

        String storage   = dir.toString() + ATTACHMENT_FOLDER;
        File file        = new File(storage, resName + extension);
        new File(storage).mkdir();

        try {
            Resources res        = ctx.getResources();
            InputStream in       = res.openRawResource(resId);
            FileOutputStream out = new FileOutputStream(file);
            copyFile(in, out);
        } catch (Exception e) {
            Log.e(LOG_TAG, "File not found: " + resPath);
            e.printStackTrace();
        }

        return getUriForFile(ctx, file);
    }

    /**
     * The URI for a base64 encoded content.
     *
     * @param str   The given base64 encoded content.
     * @return      The URI including the given content.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForBase64Content (String str) {
        String resName = str.substring(str.indexOf(":") + 1, str.indexOf("//"));
        String resData = str.substring(str.indexOf("//") + 2);
        File dir       = ctx.getExternalCacheDir();

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage = dir.toString() + ATTACHMENT_FOLDER;
        File file      = new File(storage, resName);
        new File(storage).mkdir();

        try {
            byte[] bytes         = Base64.decode(resData, 0);
            InputStream in       = new ByteArrayInputStream(bytes);
            FileOutputStream out = new FileOutputStream(file);
            copyFile(in, out);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Invalid Base64 string");
            e.printStackTrace();
        }

        return getUriForFile(ctx, file);
    }

    /**
     * Get content URI for the specified file.
     *
     * @param ctx The application context.
     * @param file The file to get the URI.
     *
     * @return content://...
     */
    private Uri getUriForFile(Context ctx, File file) {
        String authority = ctx.getPackageName() + ".provider";

        try {
            return Provider.getUriForFile(ctx, authority, file);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to get uri for file");
            e.printStackTrace();
            return Uri.EMPTY;
        }
    }

    /**
     * Writes an InputStream to an OutputStream
     *
     * @param in    The input stream.
     * @param out   The output stream.
     */
    private void copyFile (InputStream in, FileOutputStream out) {
        byte[] buffer = new byte[1024];
        int read;

        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the resource ID for the given resource path.
     *
     * @return The resource ID for the given resource.
     */
    private int getResId (String resPath) {
        Resources res   = ctx.getResources();
        String pkgName  = ctx.getPackageName();
        String dirName  = "drawable";
        String fileName = resPath;

        if (resPath.contains("/")) {
            dirName  = resPath.substring(0, resPath.lastIndexOf('/'));
            fileName = resPath.substring(resPath.lastIndexOf('/') + 1);
        }

        String resName = fileName.substring(0, fileName.lastIndexOf('.'));
        int resId      = res.getIdentifier(resName, dirName, pkgName);

        if (resId == 0) {
            resId = res.getIdentifier(resName, "mipmap", pkgName);
        }

        if (resId == 0) {
            resId = res.getIdentifier(resName, "drawable", pkgName);
        }

        return resId;
    }

}
