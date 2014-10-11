/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/

var Q     = require('q'),
    fs    = require('fs'),
    path  = require('path'),
    exec  = require('./exec'),
    spawn = require('./spawn'),
    utils = require('./utils');

// returns folder that contains package with chip architecture,
// build and project types specified by script parameters
module.exports.getPackage = function (projectType, buildtype, buildArch) {
    var appPackages = path.resolve(path.join(__dirname, '..', '..', 'AppPackages'));
    // reject promise if apppackages folder doesn't exists
    if (!fs.existsSync(appPackages)) {
        return Q.reject('AppPackages doesn\'t exists');
    }
    // find out and resolve paths for all folders inside AppPackages
    var pkgDirs = fs.readdirSync(appPackages).map(function(relative) {
        // resolve path to folder
        return path.join(appPackages, relative);
    }).filter(function(pkgDir) {
        // check that it is a directory
        return fs.statSync(pkgDir).isDirectory();
    });

    // Retrieve package infos for all packages found
    // that corresponds to package properties provided
    return Q.all(pkgDirs.map(function (pkgDir) {
        // get info about package
        return module.exports.getInfo(pkgDir).then(function(pkgInfo) {
            if (pkgInfo && pkgInfo.type == projectType &&
                pkgInfo.arch == buildArch && pkgInfo.buildtype == buildtype) {
                // if package's properties are corresponds to properties provided
                // resolve the promise with this package's info
                return Q.resolve(pkgInfo);
            }
            // else resove with no info
            return Q.resolve();
        });
    })).then(function (packages) {
        for (var idx in packages) {
            // iterate through infos found and resolve with first
            if (packages[idx]) {
                return Q.resolve(packages[idx]);
            }
        }
        // else reject because seems that no corresponding packages found
        return Q.reject('Package with specified parameters not found in AppPackages folder');
    });
};

// returns package info object or null if it is not valid package
module.exports.getInfo = function (packagePath) {
    if (!fs.statSync(packagePath).isDirectory()){
        return Q.reject('Provided path is not a directory');
    }
    // This RE matches with package folder name like:
    // CordovaApp.Phone_0.0.1.0_AnyCPU_Debug_Test
    // Group:     ( 1 ) (  2  ) (  3 ) ( 4 )
    var props = /.*\.(Phone|Windows|Windows80)_((?:\d\.)*\d)_(AnyCPU|x64|x86|ARM)(?:_(Debug))?_Test$/i.exec(path.basename(packagePath));
    if (props){
        // return package info object inside of promise
        return Q.resolve({path: packagePath,
            type      : props[1].toLowerCase(),
            arch      : props[3].toLowerCase(),
            buildtype : props[4] ? props[4].toLowerCase() : "release",
            file      : props[1].toLowerCase() != "phone" ?
                path.join(packagePath, 'Add-AppDevPackage.ps1') :
                path.join(packagePath, path.basename(packagePath).replace(/_test$/i, '') + '.appx')
        });
    }
    return Q.reject('Can\'t fetch info for package at ' + packagePath);
};

// return package app ID fetched from appxmanifest
// return rejected promise if appxmanifest not valid
module.exports.getAppId = function (platformPath) {
    var manifest = path.join(platformPath, 'package.phone.appxmanifest');
    try {
        return Q.resolve(/PhoneProductId="(.*?)"/gi.exec(fs.readFileSync(manifest, 'utf8'))[1]);
    } catch (e) {
        return Q.reject('Can\'t read appId from phone manifest' + e);
    }
};

// return package name fetched from appxmanifest
// return rejected promise if appxmanifest not valid
module.exports.getPackageName = function (platformPath) {
    var manifest = path.join(platformPath, 'package.windows.appxmanifest');
    try {
        return Q.resolve(/Application Id="(.*?)"/gi.exec(fs.readFileSync(manifest, 'utf8'))[1]);
    } catch (e) {
        return Q.reject('Can\'t read package name from manifest ' + e);
    }
};

// returns one of available devices which name match with parovided string
// return rejected promise if device with name specified not found
module.exports.findDevice = function (target) {
    return module.exports.listDevices().then(function(deviceList) {
        for (var idx in deviceList){
            if (deviceList[idx].indexOf(target) > -1) {
                return Q.resolve(idx);
            }
        }
        return Q.reject('Specified device not found');
    });
};

// returns array of available devices names
module.exports.listDevices = function () {
    return utils.getAppDeployUtils().then(function(appDeployUtils) {
        return exec('"' + appDeployUtils + '" /enumeratedevices').then(function(output) {
            return Q.resolve(output.split('\n').map(function(line) {
                var match = /\s*(\d)+\s+(.*)/.exec(line);
                return match && match[2];
            }).filter(function (line) {
                return line;
            }));
        });
    });
};

// deploys specified phone package to device/emulator
module.exports.deployToPhone = function (appxPath, deployTarget) {
    var getTarget = deployTarget == "device" ? Q("de") :
        deployTarget == "emulator" ? Q("xd") : module.exports.findDevice(deployTarget);

    // /installlaunch option sometimes fails with 'Error: The parameter is incorrect.'
    // so we use separate steps to /install and then /launch
    return getTarget.then(function(target) {
        return utils.getAppDeployUtils().then(function(appDeployUtils) {
            console.log('Installing application');
            return spawn(appDeployUtils, ['/install', appxPath, '/targetdevice:' + target]).then(function() {
                // TODO: resolve AppId without specifying project root;
                return module.exports.getAppId(path.join(__dirname, '..', '..'));
            }).then(function(appId) {
                console.log('Running application');
                return spawn(appDeployUtils, ['/launch', appId, '/targetdevice:' + target]);
            });
        });
    });
};

// deploys specified package to desktop
module.exports.deployToDesktop = function (appxScript, deployTarget) {
    if (deployTarget != "device" && deployTarget != "emulator") {
        return Q.reject("Deploying desktop apps to specific target not supported");
    }

    return utils.getAppStoreUtils().then(function(appStoreUtils) {
        return module.exports.getPackageName(path.join(__dirname, '..', '..')).then(function(pkgname) {
            // uninstalls previous application instance (if exists)
            console.log("Attempt to uninstall previous application version...");
            return spawn('powershell', ['-ExecutionPolicy', 'RemoteSigned', 'Import-Module "' + appStoreUtils + '"; Uninstall-App ' + pkgname])
            .then(function() {
                console.log("Attempt to install application...");
                return spawn('powershell', ['-ExecutionPolicy', 'RemoteSigned', 'Import-Module "' + appStoreUtils + '"; Install-App', utils.quote(appxScript)]);
            }).then(function() {
                console.log("Starting application...");
                return spawn('powershell', ['-ExecutionPolicy', 'RemoteSigned', 'Import-Module "' + appStoreUtils + '"; Start-Locally', pkgname]);
            });
        });
    });
};