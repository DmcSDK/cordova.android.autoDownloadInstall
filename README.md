# cordova-plugin-downinstall
Download automatic installation APK 

```
 cordova plugin add https://github.com/DmcSDK/cordova.android.autoDownloadInstall.git
 
```


## Example

index.js code:

    window.DownInstall.autoDInstallAPK(["newapp",    //fileName 你的文件名
                        "http://www.***.com/app/newapp.apk",    //fileUrl你的APK下载地址
                        "app download",   //download description你下载APK描述
                        "new app downloading"],   //download title 你下载APP的标题
                        function(ok) {
                        },
                        function(error) {
                           if("permission refused"==error){
                              alert("permission refused");
                            }
                        });
                        
    document.addEventListener("DownInstall.onDownLoadEvent", function onCallBack(data) {
            //根据data显示你的下载进度条  show your progress ui by data
            console.log(data.progress);
            console.log(data.size);
        }, false);
