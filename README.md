# cordova-plugin-downinstall
Download automatic installation APK By android SDK DownloadManager

```
 cordova plugin add https://github.com/DmcSDK/cordova.android.autoDownloadInstall.git
 
```


## Example

index.js code:

    window.DownInstall.autoDInstallAPK(["newapp",    //fileName 你的文件名
                        "http://www.***.com/app/newapp.apk",    //fileUrl你的APK下载地址
                        "app download",   //download description你下载APK描述
                        "new app downloading"],   //download title 你下载APP的标题
                        okCallBack(data),
                        function(error) {
                           if("permission refused"==error){
                              alert("permission refused");
                            }
                        });

                        
                        
    function okCallBack(data) {
        //根据data显示你的下载进度条  show your download progress ui by data.progress
        console.log(data.progress);
        console.log(data.size);
    };
