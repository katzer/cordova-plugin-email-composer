
<p align="left"><b><a href="https://github.com/katzer/cordova-plugin-email-composer/tree/example">SAMPLE APP</a> :point_right:</b></p>

# Cordova Email Plugin <br> [![npm version](https://badge.fury.io/js/cordova-plugin-email-composer.svg)](http://badge.fury.io/js/cordova-plugin-email-composer) [![Code Climate](https://codeclimate.com/github/katzer/cordova-plugin-email-composer/badges/gpa.svg)](https://codeclimate.com/github/katzer/cordova-plugin-email-composer) [![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L3HKQCD9UA35A "Donate once-off to this project using Paypal")

<img width="260px" align="right" hspace="10" vspace="5" src="https://github.com/katzer/cordova-plugin-email-composer/blob/example/images/ios_iphone5s_spacegrey_portrait.png">

The plugin provides access to the standard interface that manages the editing and sending an email message. You can use this view controller to display a standard email view inside your application and populate the fields of that view with initial values, such as the subject, email recipients, body text, and attachments. The user can edit the initial contents you specify and choose to send the email or cancel the operation.

Using this interface does not guarantee immediate delivery of the corresponding email message. The user may cancel the creation of the message, and if the user does choose to send the message, the message is only queued in the Mail application outbox. This allows you to generate emails even in situations where the user does not have network access, such as in airplane mode. This interface does not provide a way for you to verify whether emails were actually sent.<br><br>


## Supported Platforms

- __Android__
- __Browser__
- __iOS__
- __OSX__
- __Windows__


## Installation

The plugin can be installed via [Cordova-CLI][CLI] and is publicly available on [NPM][npm].

Execute from the projects root folder:

    $ cordova plugin add cordova-plugin-email-composer

Or install a specific version:

    $ cordova plugin add cordova-plugin-email-composer@VERSION

Or install the latest head version:

    $ cordova plugin add https://github.com/katzer/cordova-plugin-email-composer.git

Or install from local source:

    $ cordova plugin add <path> --nofetch --nosave


## Usage

The plugin creates the object `cordova.plugins.email` and is accessible after the *deviceready* event has been fired.

```js
document.addEventListener('deviceready', function () {
    // cordova.plugins.email is now available
}, false);
```

All properties are optional. After opening the draft the user may have the possibilities to edit the draft from the UI.

```javascript
cordova.plugins.email.open({
    from:       String, // sending email account (iOS only)
    to:          Array, // email addresses for TO field
    cc:          Array, // email addresses for CC field
    bcc:         Array, // email addresses for BCC field
    attachments: Array, // file paths or base64 data streams
    subject:    String, // subject of the email
    body:       String, // email body
    isHtml:    Boolean  // indicats if the body is HTML or plain text (primarily iOS)
}, callback, scope);
```

The following example shows how to create and show an email draft pre-filled with different kind of properties:

```javascript
cordova.plugins.email.open({
    to:      'max@mustermann.de',
    cc:      'erika@mustermann.de',
    bcc:     ['john@doe.com', 'jane@doe.com'],
    subject: 'Greetings',
    body:    'How are you? Nice greetings from Leipzig'
});
```

Of course its also possible to open a blank draft:

```javascript
cordova.plugins.email.open();
```

Its possible to specify the email client. If the phone isn´t able to handle the specified scheme it will fallback to the system default:

```javascript
cordova.plugins.email.open({ app: 'mailto', subject: 'Sent with mailto' });
```

On Android the app can be specified by either an alias or its package name. The alias _gmail_ is available by default.

```javascript
// Add app alias
cordova.plugins.email.addAlias('gmail', 'com.google.android.gm');

// Specify app by name or alias
cordova.plugins.email.open({ app: 'gmail', subject: 'Sent from Gmail' });
```

#### HTML and CSS

Only the built-in email app for iOS does support HTML and CSS. Some Android clients support rich formatted text.

Use `isHtml` with caution! It's disabled by default.

#### Attach Base64 encoded content
The code below shows how to attach an base64 encoded image which will be added as a image with the name *icon.png*.

```javascript
cordova.plugins.email.open({
    subject:     'Cordova Icon',
    attachments: ['base64:icon.png//iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6...']
});
```

#### Attach files from the device storage
The path to the files must be defined absolute from the root of the file system. On Android the user has to allow the app first to read from external storage!

```javascript
cordova.plugins.email.open({
    attachments: 'file:///storage/sdcard/icon.png', //=> storage/sdcard/icon.png (Android)
});
```

#### Attach native app resources
Each app has a resource folder, e.g. the _res_ folder for Android apps or the _Resource_ folder for iOS apps. The following example shows how to attach the app icon from within the app's resource folder.

```javascript
cordova.plugins.email.open({
    attachments: 'res://icon.png' //=> res/mipmap/icon (Android)
});
```

#### Attach assets from the www folder
The path to the files must be defined relative from the root of the mobile web app folder, which is located under the _www_ folder.

```javascript
cordova.plugins.email.open({
    attachments: [
        'file://img/logo.png', //=> assets/www/img/logo.png (Android)
        'file://css/index.css' //=> www/css/index.css (iOS)
    ]
});
```

#### Attach files from the internal app file system
The path must be defined relative from the directory holding application files.

```javascript
cordova.plugins.email.open({
    attachments: [
        'app://databases/db.db3', //=> /data/data/<app.package>/databases/db.db3 (Android)
        'app://databases/db.db3', //=> /Applications/<AppName.app>/databases/db.db3 (iOS, OSX)
        'app://databases/db.db3', //=> ms-appdata:///databases/db.db3 (Windows)
    ]
});
```

### Device Configuration

The email service is only available on devices which have configured an email account. On Android the user has to allow the app first to access account informations.

```javascript
cordova.plugins.email.hasAccount(callbackFn);
```

To check for a specific mail client, just pass its uri scheme on iOS, or the package name on Android as first parameter:

```javascript
cordova.plugins.email.hasClient('gmail', callbackFn);
```

For Android, it's possible to get a list of all installed email clients:

```javascript
cordova.plugins.email.getClients(function (apps) {
    cordova.plugins.email.open({ app: apps[0] });    
});
```

__Note:__ Please keep in mind that these functions might return false results and the support is not fully given for all platforms. Its wise to consider these functions as optional.


## Permissions

Some functions require permissions on __Android__. The plugin itself does not add them to the manifest nor does it ask for by itself at runtime.

| Permission | Description |
| ---------- | ----------- |
| `cordova.plugins.email.permission.READ_EXTERNAL_STORAGE` | Is needed to attach external files `file:///` located outside of the app's own file system. |
| `cordova.plugins.email.permission.GET_ACCOUNTS` | Without the permission the `hasAccount()` function wont be able to look for email accounts. |

To check if a permission has been granted:

```javascript
cordova.plugins.email.hasPermission(permission, callbackFn);
```

To request a permission:

```javascript
cordova.plugins.email.requestPermission(permission, callbackFn);
```

__Note:__ The author of the app has to make sure that the permission is listed in the manifest.


## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request


## License

This software is released under the [Apache 2.0 License][apache2_license].

Made with :yum: from Leipzig

© 2013 [appPlant GmbH][appplant]


[cordova]: https://cordova.apache.org
[CLI]: http://cordova.apache.org/docs/en/edge/guide_cli_index.md.html#The%20Command-line%20Interface
[npm]: https://www.npmjs.com/package/cordova-plugin-email-composer
[email_app]: #specify-email-app
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: http://appplant.de
