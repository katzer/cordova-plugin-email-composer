
Cordova Email Plugin - Example
==============================

<img height="470px" align="right" src="images/overview.png">

[Cordova][cordova] plugin to access the specific email composition API on various mobile platforms including iOS, Android and Windows Phone.


## Instructions

Clone the _example_ branch:

    git clone -b example https://github.com/katzer/cordova-plugin-email.git

And then execute:

    cordova run [android|browser|ios|osx|windows]

These will lunch the simulator or any plugged in device and start the example application as seen below in the screenshots.

Its also possible to open the project with [Xcode][xcode], [Android Studio][studio] or [Visual Studio][vs].

A click on the _"Open"_ button will open the email client to further edit the draft and finally send to someone.

```javascript
cordova.plugins.email.open({
    subject: 'Cordova Icons',
    recipients: 'max@mustermann.de',
    cc: ['john@doe.com', 'jane@doe.com'],
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
    <img width="32.5%" src="images/ios.png"></img>
    <img width="32.5%" src="images/android.png"></img>
    <img width="32.5%" src="images/windows.png"></img>
</p>


## License

This software is released under the [Apache 2.0 License][apache2_license].

Made with :yum: from Leipzig

© 2017 [appPlant GmbH][appplant]


[cordova]: https://cordova.apache.org
[readme]: https://github.com/katzer/cordova-plugin-email-composer/blob/master/README.md
[zip]: https://github.com/katzer/cordova-plugin-email-composer/archive/example.zip
[xcode]: https://developer.apple.com/xcode/
[studio]: https://developer.android.com/sdk/installing/studio.html
[vs]: https://www.visualstudio.com
[apache2_license]: http://opensource.org/licenses/Apache-2.0
[appplant]: www.appplant.de
