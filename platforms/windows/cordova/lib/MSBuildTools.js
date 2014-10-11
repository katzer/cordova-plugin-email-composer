var Q     = require('Q'),
    path  = require('path'),
    exec  = require('./exec'),
    spawn  = require('./spawn');

function MSBuildTools (version, path) {
    this.version = version;
    this.path = path;
}

MSBuildTools.prototype.buildProject = function(projFile, buildType, buildarch) {
    console.log("Building project: " + projFile);
    console.log("\tConfiguration : " + buildType);
    console.log("\tPlatform      : " + buildarch);

    var args = ['/clp:NoSummary;NoItemAndPropertyList;Verbosity=minimal', '/nologo',
    '/p:Configuration=' + buildType,
    '/p:Platform=' + buildarch];

    return spawn(path.join(this.path, 'msbuild'), [projFile].concat(args));
}

// returns full path to msbuild tools required to build the project and tools version
module.exports.findAvailableVersion = function () {
    var versions = ['12.0', '4.0'];

    return Q.all(versions.map(checkMSBuildVersion)).then(function (versions) {
        // select first msbuild version available, and resolve promise with it
        var msbuildTools = versions[0] || versions[1];

        return msbuildTools ? Q.resolve(msbuildTools) : Q.reject('MSBuild tools not found');
    });
};

function checkMSBuildVersion(version) {
    var deferred = Q.defer();
    exec('reg query HKLM\\SOFTWARE\\Microsoft\\MSBuild\\ToolsVersions\\' + version + ' /v MSBuildToolsPath')
    .then(function(output) {
        // fetch msbuild path from 'reg' output
        var path = /MSBuildToolsPath\s+REG_SZ\s+(.*)/i.exec(output);
        if (path) {
            deferred.resolve(new MSBuildTools(version, path[1]));
            return;
        }
        deferred.resolve(null); // not found
    }, function (err) {
        // if 'reg' exits with error, assume that registry key not found
        deferred.resolve(null);
    });
    return deferred.promise;
}