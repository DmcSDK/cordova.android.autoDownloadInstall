var exec = require('cordova/exec');

exports.autoDInstallAPK = function(arg0, success, error) {
    exec(success, error, "InstallApk", "autoDInstallAPK", arg0);
};

exports.onDownLoadEvent = function (data) {
    data = JSON.parse(JSON.stringify(data));
    cordova.fireDocumentEvent('window.DownInstall.onDownLoadEvent', data);
};

exports.canDownloadState = function (arg0, success, error) {
    exec(success, error, "InstallApk", "canDownloadState", arg0);
};

exports.downloadState = function (arg0, success, error) {
    exec(success, error, "InstallApk", "downloadState", arg0);
};

exports.installAPK = function (arg0, success, error) {
    exec(success, error, "InstallApk", "installAPK", arg0);
};