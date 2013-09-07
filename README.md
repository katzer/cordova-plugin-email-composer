Cordova EmailComposer-Plugin
====================

A bunch of email composition plugins for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))

## Supported Platforms
- **iOS**<br>
*See [MFMailComposeViewController Class Reference](http://developer.apple.com/library/ios/documentation/MessageUI/Reference/MFMailComposeViewController_class/Reference/Reference.html) for detailed informations and screenshots.*

- **Android**

## Adding the Plugin to your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```
cordova plugin add https://github.com/katzer/cordova-plugin-email-composer.git
```

## Removing the Plugin from your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```
cordova plugin rm de.appplant.cordova.plugin.email-composer
```

## Release Notes
#### Version 0.4.1 (not yet released)
- [bugfix]: On Android, the `isServiceAvailable()` interface has returned string values instead of boolean values.

#### Version 0.4.0 (20.08.2013)
- Added Android support<br>
  *Based on the EmailComposerWithAttachments Android plugin made by* ***guidosabatini***

#### Version 0.2.1 (15.08.2013)
- [bugfix]: Email was not send in HTML format, if the `isHtml` flag was set.
- [bugfix]: `email.open()` without a parameter throw an error.

#### Version 0.2.0 (13.08.2013)
- Added iOS support<br>
  *Based on the EmailComposer(WithAttachments) iOS plugin made by* ***Randy McMillan*** *and* ***guidosabatini***

## Using the plugin
The plugin creates the object ```window.plugin.email``` with two methods:

### isServiceAvailable()
Email service is only available on devices capable which are able to send emails. You can use this function to hide email functionality from users who will be unable to use it.<br>
Function takes a callback function, passed to which is a boolean property. Optionally the callback scope can be assigned as a second parameter.

**Android:** Service is not available if no email accout is added on the device.

```javascript
/*
 * Find out if the sending of emails is available. Use this for showing/hiding email buttons.
 */
window.plugin.email.isServiceAvailable(
    function (isAvailable) {
        alert(isavailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### open()
You can use this function to show the email view pre-filled with all kind of properties (see example below).<br>
The function takes a hash where each property is optional. If a callback function is given, it will be called with a result code about the user action.

**iOS:** You can attach only PDF and images (the latter will be convertend in PNG format).<br>
**Android:** Result codes for the callback function are not support.

```javascript
/*
 * Opens an email draft pre-filled with the passed properties.
 */
window.plugin.email.open({
    subject: 'subject', // represents the subject of the email
    body: 'body',       // represents the email body (could be HTML code, in this case set isHtml to true)
    isHtml: true,       // indicats if the body is HTML or plain text
    recipients: [],     // contains all the email addresses for TO field
    ccRecipients: [],   // contains all the email addresses for CC field
    bccRecipients: [],  // contains all the email addresses for BCC field
    attachments: [],    // contains all full paths to the files you want to attach

    callback: function (code) {
        switch (code) {
            case 0:     // email composition cancelled (cancel button pressed and draft not saved)
            case 1:     // email saved (cancel button pressed but draft saved)
            case 2:     // email sent
            case 3:     // send failed
            case 4:     // email not sent (something wrong happened e.g. service is not available)
        }
    },
    scope: this         // execution scope of the callback function (default: window)
});
```

```javascript
/*
 * Opens a blank email draft.
 */
window.plugin.email.open();
```

## Quirks

### Testing in the Android Simulator
If you are using an emulator, you’ll need to configure the email client. If the email client is not configured, it will not respond to the Intent we’ll be discussing. If you want to see the chooser in action, you’ll need to configure a device using multiple messaging applications, such as the Gmail application and the Email application.

**Note:** Google APIs will give you access to adding accounts.
