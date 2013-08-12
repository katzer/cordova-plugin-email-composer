Cordova EmailComposer-Plugin
====================

A bunch of email compositions for Cordova 3.x.x

by Sebastián Katzer ([github.com/katzer](https://github.com/katzer))

## Supported Platforms ##
- **iOS**

## Adding the Plugin to your project ##
Through the [Command-line Interface](http://cordova.apache.org/docs/en/3.0.0/guide_cli_index.md.html#The%20Command-line%20Interface):
```
cordova plugin add https://github.com/katzer/cordova-plugin-email-composer.git
```

## Release Notes ##
#### Version 0.2.0 (13.08.2013) ####
- Added iOS support<br>
  *Based on the EmailComposer(WithAttachments) iOS plugin made by* ***Randy McMillan*** *and* ***guidosabatini***

## Using the plugin ##
The plugin creates the object ```window.plugin.email``` with two methods:

### isServiceAvailable() ###
Email service is only available on devices capable which are able to send emails. You can use this function to hide email functionality from users who will be unable to use it. Function takes a callback function, passed to which is a boolean property.
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

### open() ###
