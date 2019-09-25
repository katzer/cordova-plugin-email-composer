cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
  {
    "id": "cordova-plugin-email-composer.EmailComposer",
    "file": "plugins/cordova-plugin-email-composer/www/email_composer.js",
    "pluginId": "cordova-plugin-email-composer",
    "clobbers": [
      "cordova.plugins.email"
    ]
  },
  {
    "id": "cordova-plugin-email-composer.EmailComposerProxy",
    "file": "plugins/cordova-plugin-email-composer/src/windows/EmailComposerProxy.js",
    "pluginId": "cordova-plugin-email-composer",
    "runs": true
  },
  {
    "id": "cordova-plugin-email-composer.EmailComposerProxyImpl",
    "file": "plugins/cordova-plugin-email-composer/src/windows/EmailComposerProxyImpl.js",
    "pluginId": "cordova-plugin-email-composer",
    "runs": true
  },
  {
    "id": "cordova-plugin-camera.Camera",
    "file": "plugins/cordova-plugin-camera/www/CameraConstants.js",
    "pluginId": "cordova-plugin-camera",
    "clobbers": [
      "Camera"
    ]
  },
  {
    "id": "cordova-plugin-camera.CameraPopoverOptions",
    "file": "plugins/cordova-plugin-camera/www/CameraPopoverOptions.js",
    "pluginId": "cordova-plugin-camera",
    "clobbers": [
      "CameraPopoverOptions"
    ]
  },
  {
    "id": "cordova-plugin-camera.camera",
    "file": "plugins/cordova-plugin-camera/www/Camera.js",
    "pluginId": "cordova-plugin-camera",
    "clobbers": [
      "navigator.camera"
    ]
  },
  {
    "id": "cordova-plugin-camera.CameraPopoverHandle",
    "file": "plugins/cordova-plugin-camera/www/CameraPopoverHandle.js",
    "pluginId": "cordova-plugin-camera",
    "clobbers": [
      "CameraPopoverHandle"
    ]
  },
  {
    "id": "cordova-plugin-camera.CameraProxy",
    "file": "plugins/cordova-plugin-camera/src/windows/CameraProxy.js",
    "pluginId": "cordova-plugin-camera",
    "runs": true
  },
  {
    "id": "cordova-plugin-device.device",
    "file": "plugins/cordova-plugin-device/www/device.js",
    "pluginId": "cordova-plugin-device",
    "clobbers": [
      "device"
    ]
  },
  {
    "id": "cordova-plugin-device.DeviceProxy",
    "file": "plugins/cordova-plugin-device/src/windows/DeviceProxy.js",
    "pluginId": "cordova-plugin-device",
    "runs": true
  },
  {
    "id": "cordova-plugin-x-toast.Toast",
    "file": "plugins/cordova-plugin-x-toast/www/Toast.js",
    "pluginId": "cordova-plugin-x-toast",
    "clobbers": [
      "window.plugins.toast"
    ]
  },
  {
    "id": "cordova-plugin-x-toast.ToastProxy",
    "file": "plugins/cordova-plugin-x-toast/src/windows/toastProxy.js",
    "pluginId": "cordova-plugin-x-toast",
    "merges": [
      ""
    ]
  }
];
module.exports.metadata = 
// TOP OF METADATA
{
  "cordova-plugin-email-composer": "0.9.2",
  "cordova-plugin-camera": "4.1.0",
  "cordova-plugin-device": "2.0.3",
  "cordova-plugin-x-toast": "2.7.2"
};
// BOTTOM OF METADATA
});