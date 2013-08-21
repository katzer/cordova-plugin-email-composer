/**
 *  EmailComposer.java
 *  Cordova Email Composition Plugin
 *
 *  Created by Sebastian Katzer (github.com/katzer) on 16/08/2013.
 *  Copyright 2013 Sebastian Katzer. All rights reserved.
 *  GPL v2 licensed
 */

package de.appplant.cordova.plugin;

import java.io.File;
import java.util.ArrayList;

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

	private CallbackContext ctx;

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

		this.ctx = ctx;
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
		if (params.has("recipients"))
			this.setRecipients(params.getJSONArray("recipients"), mail);
		if (params.has("ccRecipients"))
			this.setCcRecipients(params.getJSONArray("ccRecipients"), mail);
		if (params.has("bccRecipients"))
			this.setBccRecipients(params.getJSONArray("bccRecipients"), mail);
		if (params.has("attachments"))
			this.setAttachments(params.getJSONArray("attachments"), mail);

		return mail;
	}

	/**
	 * Zeigt den ViewController zum Versenden/Bearbeiten der Mail an.
	 */
	private void openDraft (Intent draft) {
		this.cordova.startActivityForResult(this, Intent.createChooser(draft, "Select Email app"), 0);
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
	 * Convert from paths to Android friendly Parcelable Uri's
	 */
	private void setAttachments (JSONArray attachments, Intent draft) throws JSONException {
		ArrayList<Uri> uris = new ArrayList<Uri>();

		for (int i = 0; i < attachments.length(); i++) {
			File file = new File(attachments.getString(i));

			if (file.exists()) {
				Uri uri = Uri.fromFile(file);

				uris.add(uri);
			}
		}

		draft.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
	}

	/**
	 * Gibt an, ob es eine Anwendung gibt, welche E-Mails versenden kann.
	 */
	private Boolean isEmailAccountConfigured () {
		Intent  intent    = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto","max@mustermann.com", null));
		Boolean available = cordova.getActivity().getPackageManager().queryIntentActivities(intent, 0).size() > 1;

		return available;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	 	super.onActivityResult(requestCode, resultCode, intent);

	 	if (this.isEmailAccountConfigured()) {
	 		this.ctx.success();
	 	} else {
	 		this.ctx.success(4);
	 	}
	}
}
