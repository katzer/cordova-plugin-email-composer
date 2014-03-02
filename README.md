Cordova EmailComposer-Plugin
====================

A bunch of email composition plugins for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))


## Supported Platforms
- **iOS**<br>
*See [MFMailComposeViewController Class Reference](http://developer.apple.com/library/ios/documentation/MessageUI/Reference/MFMailComposeViewController_class/Reference/Reference.html) for detailed informations and screenshots.*
- **Android**
- **WP8**<br>
*See [How to use the email compose task for Windows Phone](http://msdn.microsoft.com/en-us/library/windowsphone/develop/hh394003.aspx) for detailed informations.*


## Adding the Plugin to your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```bash
# from master:
cordova plugin add https://github.com/katzer/cordova-plugin-email-composer.git
cordova build

# last version:
cordova plugin add de.appplant.cordova.plugin.email-composer
cordova build
```


## Removing the Plugin from your project
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```
cordova plugin rm de.appplant.cordova.plugin.email-composer
```


## PhoneGap Build
Add the following xml to your config.xml to always use the latest version of this plugin:
```
<gap:plugin name="de.appplant.cordova.plugin.email-composer" />
```
or to use this exact version:
```
<gap:plugin name="de.appplant.cordova.plugin.email-composer" version="0.7.1" />
```
More informations can be found [here](https://build.phonegap.com/plugins/369).


## Release Notes
#### Version 0.8.0 (02.03.2014)
- [enhancement:] New `absolute://` and `relative://` attachment prefixes.
- [feature:] New `base64://` prefix to attach base64 encoded data streams.

#### Version 0.7.2 (01.03.2014)
- [enhancement:] Attachments are added with their real name.

#### Version 0.7.1 (17.12.2013)
- [bugfix:] Only the last attachment was added to the email composer on android.

#### Version 0.7.0 (05.12.2013)
- Release under the Apache 2.0 license.
- [***change:***] Removed the `callback` property from the `open` interface.
- [***change:***] Renamed the properties `recipients`, `ccRecipients`, `bccRecipients`.
- [bugfix:] Plugin under WP8 throws an error, if recipients were given as arrays.
- [enhancement:] `open` does not block the ui thread on iOS & Android anymore.

#### Version 0.6.0 (17.11.2013)
- Added WP8 support
- [***deprecated:***] The `callback` property will be removed with v0.7.0.

#### Version 0.4.2 (17.11.2013)
- [feature:] Added alias `openDraft` to the `open` interface.

#### Version 0.4.1 (03.11.2013)
- [bugfix]: On Android, the `isServiceAvailable()` interface has returned string values instead of boolean values.
- [bugfix]: Sometimes the device said that no email app is available because of the missing mime type.

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
        alert(isAvailable ? 'Service is available' : 'Service NOT available');
    }
);
```

### open() / openDraft()
You can use this function to show the email view pre-filled with all kind of properties (see example below).<br>
The function takes a hash where each property is optional. If a callback function is given, it will be called with a result code about the user action.

**iOS:** You can attach only PDF and images (the latter will be converted in PNG format).

```javascript
/*
 * Opens an email draft pre-filled with the passed properties.
 */
window.plugin.email.open({
    to:          Array, // contains all the email addresses for TO field
    cc:          Array, // contains all the email addresses for CC field
    bcc:         Array, // contains all the email addresses for BCC field
    attachments: Array, // contains all paths to the files you want to attach or base64 encoded data streams
    subject:    String, // represents the subject of the email
    body:       String, // represents the email body (could be HTML code, in this case set isHtml to true)
    isHtml:    Boolean, // indicats if the body is HTML or plain text
});
```

```javascript
/*
 * Opens a blank email draft.
 */
window.plugin.email.open();
```


## Example
```javascript
window.plugin.email.open({
    to:          ['max.mustermann@appplant.de'],
    cc:          ['erika.mustermann@appplant.de'],
    bcc:         ['john.doe@appplant.com', 'jane.doe@appplant.com'],
    attachments: ['base64:icon.png//iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6/...'],
    subject:     'Hello World!',
    body:        '<h3>TEST</h3><h2>TEST</h2><h1>TEST</h1>',
    isHtml:      true
});
```

## Platform specifics
### File attachment on Android
File attachment paths can either be defined absolute or relative. A relative path must be point to a file from the res folder of the application.
```javascript
window.plugin.email.open({
    attachments: [
        'absolute://storage/sdcard/icon.jpg',
        'relative://drawable/icon'
    ]
});
```

### File attachment on iOS
File attachment paths can either be defined absolute or relative. A relative path must be point to a file from the root folder of the application.
```javascript
window.plugin.email.open({
    attachments: [
        'absolute://Users/sebastian/Library/Application Support/iPhone Simulator/7.0.3/Applications/E7981856-801F-4355-8687-EAACDF8B2A54/HelloCordova.app/../Documents/icon.jpg"'
        'relative://resources/icons/icon.jpg'
    ]
});
```


## Quirks

### Email composer under Android and Windows Phone
An configured email account is required to send emails.<br>
**WP8:** If an email account is not set up on the phone, the application prompts the user to set up an account.

### Limited support for Windows Phone 8
Adding attachments and HTML formatted body are not supported.

### TypeError: Cannot read property 'currentVersion' of null
Along with Cordova 3.2 and Windows Phone 8 the `version.bat` script has to be renamed to `version`.

On Mac or Linux
```
mv platforms/wp8/cordova/version.bat platforms/wp8/cordova/version
```
On Windows
```
ren platforms\wp8\cordova\version.bat platforms\wp8\cordova\version
```


### Compile error on iOS
The error indicates, that the `MessageUI.framework` is not linked to your project. The framework is linked automatically when the plugin was installed, but may removed later.
```
Undefined symbols for architecture i386:
  "_OBJC_CLASS_$_MFMailComposeViewController", referenced from:
      objc-class-ref in APPEmailComposer.o
ld: symbol(s) not found for architecture i386
clang: error: linker command failed with exit code 1 (use -v to see invocation)
```


## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## License

This software is released under the [Apache 2.0 License](http://opensource.org/licenses/Apache-2.0).
