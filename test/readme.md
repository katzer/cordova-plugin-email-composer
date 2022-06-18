To test this plugin create a new repo from scratch

```
cordova create test-email-composer com.test.email.composer testEmailComposer
cd test-email-composer
cordova platform add android|ios
cordova plugin add https://github.com/katzer/cordova-plugin-email-composer.git
```

In `www/index.html` add the following line right after the `<p>` tag containing `Device is Ready`

```html
<br><br><br>
<button id="test-email-composer" type="button">Click to test Email Composer</button>
```

In `www/js/index.js` add the following at the end of the function `onDeviceReady`

```js
document.getElementById('test-email-composer')
    .addEventListener('click', function (event) {
        cordova.plugins.email.open({
            from:       'from_test@test.com', // sending email account (iOS only)
            to:         ['to_test@test.com'], // email addresses for TO field
            cc:         ['cc_test@test.com'], // email addresses for CC field
            bcc:        ['bcc_test@test.com'], // email addresses for BCC field
            subject:    'teste', // subject of the email
            attachments: ['base64:icon.png//iVBORw0KGgoAAAANSUhEUgAAADwAAAA8CAYAAAA6'],
            body:       'teste' // email body
        });
    })
```

Then connect your device with a USB cable and run

```
cordova run android --device
```



