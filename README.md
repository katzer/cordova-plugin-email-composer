
<p align="left"><b><a href="https://github.com/katzer/cordova-plugin-email-composer/tree/example">SAMPLE APP</a> :point_right:</b></p>

# Cordova Email Plugin <br> [![npm version](https://badge.fury.io/js/cordova-plugin-email-composer.svg)](http://badge.fury.io/js/cordova-plugin-email-composer) [![Code Climate](https://codeclimate.com/github/katzer/cordova-plugin-email-composer/badges/gpa.svg)](https://codeclimate.com/github/katzer/cordova-plugin-email-composer) [![PayPayl donate button](https://img.shields.io/badge/paypal-donate-yellow.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=L3HKQCD9UA35A "Donate once-off to this project using Paypal")

<img width="260px" align="right" hspace="10" vspace="5" src="https://github.com/katzer/cordova-plugin-email-composer/blob/example/images/ios_iphone5s_spacegrey_portrait.png">

The plugin provides access to the standard interface that manages the editing and sending an email message. You can use this view controller to display a standard email view inside your application and populate the fields of that view with initial values, such as the subject, email recipients, body text, and attachments. The user can edit the initial contents you specify and choose to send the email or cancel the operation.

Using this interface does not guarantee immediate delivery of the corresponding email message. The user may cancel the creation of the message, and if the user does choose to send the message, the message is only queued in the Mail application outbox. This allows you to generate emails even in situations where the user does not have network access, such as in airplane mode. This interface does not provide a way for you to verify whether emails were actually sent.<br><br>


## Supported Platforms

- __Android / Amazon FireOS__
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

    $ cordova plugin add cordova-plugin-email-composer --searchpath <path>


## Usage

The plugin creates the object `cordova.plugins.email` and is accessible after the *deviceready* event has been fired.

```js
document.addEventListener('deviceready', function () {
    // cordova.plugins.email is now available
}, false);
```

### Determine email capability

The Email service is only available on devices which have configured an email account:

```javascript
cordova.plugins.email.isAvailable(function (hasAccount) {});
```

To check for a specific mail client, just pass its uri scheme on iOS, or its name on Android as first parameter:

```javascript
cordova.plugins.email.isAvailable('gmail', function (hasAccount, hasGmail) {});
```

### Open an email draft
All properties are optional. After opening the draft the user may have the possibilities to edit the draft.

```javascript
cordova.plugins.email.open({
    to:          Array, // email addresses for TO field
    cc:          Array, // email addresses for CC field
    bcc:         Array, // email addresses for BCC field
    attachments: Array, // file paths or base64 data streams
    subject:    String, // subject of the email
    body:       String, // email body (for HTML, set isHtml to true)
    isHtml:    Boolean, // indicats if the body is HTML or plain text
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

On _Android_ the app can be specified by either an alias or its package name. The alias _gmail_ is available by default.

```javascript
// Add app alias
cordova.plugins.email.addAlias('gmail', 'com.google.android.gm');

// Specify app by name or alias
cordova.plugins.email.open({ app: 'gmail', subject: 'Sent from Gmail' });
```

### Attachments

Attachments can be either base64 encoded datas, files from the the device storage or assets from within the *www* folder.

#### Attach Base64 encoded content
The code below shows how to attach an base64 encoded image which will be added as a image with the name *icon.png*.

```javascript
cordova.plugins.email.open({
    subject:     'Cordova Icon',
    attachments: ['base64:icon.png//iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6...']
});
```

#### Attach files from the device storage
The path to the files must be defined absolute from the root of the file system.

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


## Permissions

The plugin might ask for granting permissions like reading email account informations. That's done automatically.

Its possible to request them manually:

```javascript
cordova.plugins.email.requestPermission(function (granted) {...});
```

Or check if they have been granted already:

```javascript
cordova.plugins.email.hasPermission(function (granted) {...});
```

In case of missing permissions the result of `isAvailable` might be wrong.


## Quirks

### HTML and CSS on Android
Even Android is capable to render HTML formatted mails, most native Mail clients like the standard app or Gmail only support rich formatted text while writing mails. That means that __CSS cannot be used__ (no _class_ and _style_ support).

__Update:__ Seems it stopped working with gmail at version 6.x and Android Mail, see [#264](https://github.com/katzer/cordova-plugin-email-composer/issues/264).

The following table gives an overview which tags and attributes can be used:

<table>
<td width="60%">
    <ul>
        <li><code>&lt;a href="..."&gt;</code></li>
        <li><code>&lt;b&gt;</code></li>
        <li><code>&lt;big&gt;</code></li>
        <li><code>&lt;blockquote&gt;</code></li>
        <li><code>&lt;br&gt;</code></li>
        <li><code>&lt;cite&gt;</code></li>
        <li><code>&lt;dfn&gt;</code></li>
        <li><code>&lt;div align="..."&gt;</code></li>
        <li><code>&lt;em&gt;</code></li>
        <li><code>&lt;font size="..." color="..." face="..."&gt;</code></li>
        <li><code>&lt;h1&gt;</code></li>
        <li><code>&lt;h2&gt;</code></li>
        <li><code>&lt;h3&gt;</code></li>
    </ul>
</td>
<td width="40%">
    <ul>
        <li><code>&lt;h4&gt;</code></li>
        <li><code>&lt;h5&gt;</code></li>
        <li><code>&lt;h6&gt;</code></li>
        <li><code>&lt;i&gt;</code></li>
        <li><code>&lt;img src="..."&gt;</code></li>
        <li><code>&lt;p&gt;</code></li>
        <li><code>&lt;small&gt;</code></li>
        <li><code>&lt;strike&gt;</code></li>
        <li><code>&lt;strong&gt;</code></li>
        <li><code>&lt;sub&gt;</code></li>
        <li><code>&lt;sup&gt;</code></li>
        <li><code>&lt;tt&gt;</code></li>
        <li><code>&lt;u&gt;</code></li>
    </ul>
</td>
</table>

### HTML and CSS on Windows

HTML+CSS formatted body are not supported through the native API for Windows.

### Other limitations

- _\<img\>_ tags do not work properly on Android.
- Callbacks for windows and osx platform are called immediately.
- _isAvailable_ does always return _true_ for windows platform.


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
