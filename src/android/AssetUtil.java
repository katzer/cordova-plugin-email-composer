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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            Log.e(LOG_TAG, "File not found: " + file.getAbsolutePath());
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

        if (dir == null) {
            Log.e(LOG_TAG, "Missing external cache dir");
            return Uri.EMPTY;
        }

        String storage   = dir.toString() + ATTACHMENT_FOLDER;
        int resId        = getResId(resPath);
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

        return getUriForFile(ctx, file);
    }

    /**
     * The URI for a base64 encoded content.
     *
     * @param content   The given base64 encoded content.
     * @return          The URI including the given content.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private Uri getUriForBase64Content (String content) {
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

        return Provider.getUriForFile(ctx, authority, file);
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
     * @return The resource ID for the given resource.
     */
    private int getResId (String resPath) {
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
