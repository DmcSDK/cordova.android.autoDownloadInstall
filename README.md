# cordova-plugin-downinstall
Download automatic installation APK 



## Example

index.js code:

    window.DownInstall.autoDInstallAPK(["newapp",    //你的文件名
                        "http://www.***.com/app/newapp.apk",    //你的APK下载地址
                        "app download",   //你下载APK描述
                        "new app downloading"],   //你下载APP的标题
                        function(ok) {
                        },
                        function(error) {
                           if("permission refused"==error)}{
                              alert("permission refused");
                            }
                        });
