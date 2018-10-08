var exec = require('cordova/exec');

exports.autoDInstallAPK = function(arg0, success, error) {
    exec(success, error, "DownInstall", "autoDInstallAPK", arg0);
};

exports.onDownLoadEvent = function (data) {
    data = JSON.parse(JSON.stringify(data));
    cordova.fireDocumentEvent('window.UHF.onInventoryLoopTagEvent', data);
};

exports.canDownloadState = function (arg0, success, error) {
    exec(success, error, "DownInstall", "canDownloadState", arg0);
};