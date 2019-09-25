
## ChangeLog

#### Version 0.9.2 (24.01.2019)
- Fix package android.support.v4.content does not exist

#### Version 0.9.1 (13.12.2018)
- Fix line breaks (\r\n) on Android

#### Version 0.9.0 (13.12.2018)
- [feature:] Added `getClients` that returns a list of available email clients (Android)
- [__change__]: Replace `isAvailable` through `hasClient` and `hasAccount`.
- [__change__]: Plugin does not add any permissions by itself like `GET_ACCOUNTS` or `READ_EXTERNAL_STORAGE`.
- [__change__]: `isAvailable` does not request for missing permission (`GET_ACCOUNTS`).
- [__change__]: `hasPermission` takes 3 arguments now. The first one has to be a value of `cordova.plugins.email.permission`.
- [__change__]: `requestPermission` takes 3 arguments now. The first one has to be a value of `cordova.plugins.email.permission`.
- [__change__]: Remove support lib from being installed (Android).
- [__change__]: Remove deprecated namespace `plugin.email`.
- [__change__]: Remove deprecated support for `isHTML`.
- [__change__]: Change default value for `isHtml` to `false`.
- [__change__]: Remove Android specific `type` property.
- [enhancement]: Skip chooser if there's only the default app (#302)
- [enhancement]: Improve chooser to only display email clients.
- [enhancement]: Add `from` to specify the sending email account.
- [bugfix:] Do not open old email draft [fixes #303]

#### Version 0.8.15 (08.02.2018)
- Fix iOS not working if `app:` wasn't specified.
- Fix `attachments:` to accept a string.

#### Version 0.8.14 (31.01.2018)
- Fix wrong uri encoding for browser platform.

#### Version 0.8.13 (25.01.2018)
- Fix potential wrong result for isAvailable on iOS+OSX by using scheme other then `mailto:`
- Fix open app from background thread by using scheme other then `mailto:`

#### Version 0.8.12 (09.01.2018)
- Internal code refactoring
- Added `type` property to specify the content type (#283)

#### Version 0.8.11 (25.10.2017)
- Apply URL encoding when constructing mailto: link (#273)

#### Version 0.8.10 (25.09.2017)
- Open gmail on ios and macos [fixes #272]
- Added alias for outlook
- Fix warnings with iOS 11

#### Version 0.8.9 (14.09.2017)
- Fix opening email with file attachment causes app crash on Android 8 (#270)

#### Version 0.8.8 (07.09.2017)
- Fix crash on iOS if attachment could not be found
- Fix wrong plugin ID in package.json

#### Version 0.8.7 (26.06.2017)
- Add support for an app:// URL #158 (Android)
- Add support for an app:// (iOS, OSX, Windows)

#### Version 0.8.6 (12.06.2017)
- Fixed issue with Android 4.x

#### Version 0.8.5 (09.06.2017)
10 commits including bug fixes and enhancements:
- [enhancement]: Support for osx platform
- [enhancement]: Added `isAvailable2` which works equal except the callback args are in reverse order.
- [enhancement]: Fixed possible attachment issues some Android email clients.

#### Version 0.8.4 (06.06.2017)
25 commits including bug fixes and enhancements:
- [__change__]: Skip availability checks with `email.open()`
- [__change__]: Upgrade minimum required engine versions 
- [enhancement]: Treat callback functions as optional
- [enhancement]: Support for Android API 23 Permission API
- [enhancement]: Test the account name if they match the email pattern (#180)
- [enhancement]: Support newest cordova platform versions
- [enhancement]: Use @synthesize to prevent EXC_BAD_ACCESS errors with non-ARC code (#207)
- [bugfix]: res:// uri not resolved on cordova-android@6 (6334d0)
- [bugfix]: Require old plugin id for windows platform (#176)
- [bugfix]: Memory leak for iOS

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
