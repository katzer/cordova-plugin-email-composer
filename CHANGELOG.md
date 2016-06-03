## ChangeLog
#### Version 0.8.3 (01.03.2016)
63 commits including bug fixes and enhancements:
- [___change:___] New plugin ID: _cordova-plugin-email-composer_
- [enhancement:] Published on npm
- [enhancement:] Allowed the chooser header text to be configured (#113)
- [enhancement:] Plain mailto: support
- [enhancement:] Specify email client using `app:` flag
- [enhancement:] More samples in Sample-App
- [bugfix:] Build issues with iOS and Android
- [bugfix:] Compatibility with newest OS and cordova platform versions
- [bugfix:] Crash on iOS when presenting view controller from background (#169)
- [bugfix:] Crash on iOS when no email account is setup
- [bugfix:] Resolved issues with attachments on all platforms
- ...

#### Version 0.8.2 (01.03.2015)
- Added new namespace `cordova.plugins.email`<br>
  **Note:** The former `plugin.email` namespace is now deprecated and will be removed with the next major release.
- [___change:___] Unified `absolute:` and `relative:` to `file:`
- [___change:___] Renamed `isServiceAvailable` to `isAvailable`
- [feature:] `app:` allows to specify target mail app on Android
- [feature:] `res:` prefix for native ressource attachments
- [enhancement:] Support attachments on Windows Phone 8.1
- [enhancement:] `open` supports callbacks
- [enhancement:] `isHTML` can be used next `isHtml`
- [enhancement:] Set mime type to binary if unknown
- [bugfix:] Defaults were ignored

#### Version 0.8.1 (06.04.2014)
- [enhancement:] Make use Cordovas NSData+Base64 extension.
- [enhancement:] Log error message if attachment path does not exist.
- [feature:] Add support for amazon fire
- [bugfix:] Fix INSTALL_FAILED_CONFLICTING_PROVIDER error
- [bugfix:] `relative://` attachment path wasnt working due to a missing permission.
- [bugfix:] `base64://` attachment path looked up in the wrong directory.
- [enhancement:] `relative://` supports now any file types and not only images.
- [___change:___] `relative://` URI's even for Android need a file extension.

#### Version 0.8.0 (02.03.2014)
- [enhancement:] New `absolute://` and `relative://` attachment prefixes.
- [feature:] New `base64://` prefix to attach base64 encoded data streams.

#### Version 0.7.2 (01.03.2014)
- [enhancement:] Attachments are added with their real name.

#### Version 0.7.1 (17.12.2013)
- [bugfix:] Only the last attachment was added to the email composer on android.

#### Version 0.7.0 (05.12.2013)
- Release under the Apache 2.0 license.
- [___change:___] Removed the `callback` property from the `open` interface.
- [___change:___] Renamed the properties `recipients`, `ccRecipients`, `bccRecipients`.
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
