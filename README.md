
Cordova Email Plugin - Example
==============================

[Cordova][cordova] plugin to access the specific email composition API on various mobile platforms including iOS, Android and Windows Phone.

## Instructions
[Download][zip] or clone the _example_ branch and run the following command:

```bash
cordova run [ios|android|wp8]
```

These will lunch the simulator or any plugged in device and start the example application as seen below in the screenshots. Its also possible to open the project with [Xcode][xcode], [Android Studio][studio] or [Eclipse][eclipse].

<p align="center">
    <img src="images/overview.png"></img>
</p>

A click on the _"Draft with attachments"_ button will present the email composition controller to send the draft and for further editing.

```javascript
cordova.plugins.email.open({
    subject: 'Cordova Icons',
    body: [
        '<ol>',
            '<li>Asset from the www folder</li>',
            '<li>Asset from the res folder</li>',
            '<li>Asset from Base64 string</li>',
        '</ol>'
    ],
    attachments: [
        'file://img/logo.png',
        'res://icon.png',
        'base64:icon.png//iVBORw0KGgoAAAANSUhEUg...',
        'file://README.pdf'
    ]
})
```

Please read the plugin's [README][readme] for further requirements and informations.


## Screenshots

<p align="center">
    <img height="335px" src="images/ios.png"></img>
    &nbsp;
    <img height="335px" src="images/android.png"></img>
    &nbsp;
    <img height="335px" src="images/wp8.png"></img>
</p>


## License

This software is released under the [Apache 2.0 License][apache2_license].

© 2013-2014 appPlant UG, Inc. All rights reserved


[cordova]: https://cordova.apache.org
[readme]: https://github.com/katzer/cordova-plugin-email-composer/blob/master/README.md
[zip]: https://github.com/katzer/cordova-plugin-email-composer/archive/example.zip
[xcode]: https://developer.apple.com/xcode/
[studio]: https://developer.android.com/sdk/installing/studio.html
[eclipse]: https://developer.android.com/sdk/index.html
[apache2_license]: http://opensource.org/licenses/Apache-2.0

