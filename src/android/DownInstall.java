package com.dmc.installAPK;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by Dmc
 */

public class DownInstall extends CordovaPlugin{

    public String [] permissionArray = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    private CallbackContext myCallbackContext;

    private static DownInstall instance;
    DownloadManager manager;
    long downloadId=0;
    BroadcastReceiver onComplete;
    File apkFile;
    public DownInstall() {
        instance = this;
    }
    JSONArray args;
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.args=args;
        myCallbackContext=callbackContext;
        /**
         * appVersion
         */
        if (action.equals("autoDInstallAPK")) {
            //Toast.makeText(cordova.getActivity(),"downloadId:"+downloadId,Toast.LENGTH_LONG).show();
            for( int i = 0; i < permissionArray.length - 1; i++)
            {
                if (!cordova.hasPermission(permissionArray[i]))
                {
                    cordova.requestPermission(this, i, permissionArray[i]);
                }
            }
            start();
            return  true;
        }else if(action.equals("canDownloadState")){
             if(canDownloadState()){
                callbackContext.success("true");
             }else{
                callbackContext.success("false");
             }
        }else if(action.equals("installAPK")){
            for( int i = 0; i < permissionArray.length - 1; i++)
            {
                if (!cordova.hasPermission(permissionArray[i]))
                {
                    cordova.requestPermission(this, i, permissionArray[i]);
                }
            }
            start();
            return  true;
        }else if(action.equals("downloadState")){
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("downloadId",downloadId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            myCallbackContext.success(jsonObject);
            return  true;
        }
        return false;
    }

    void start(){
        try {

            apkFile=createFile( args.getString(0)+""+System.currentTimeMillis());
            //get url of app on server
            String url = args.getString(1);
            //set downloadmanager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription(args.getString(2));
            request.setTitle(args.getString(3));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED );
            //set destination
            request.setDestinationUri(Uri.fromFile(apkFile));

            // get download service and enqueue file
            manager = (DownloadManager) cordova.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
            if(downloadId!=-1){//防重复下载
                manager.remove(downloadId);
            }
            if(null!=onComplete){//防重复广播
                cordova.getActivity().unregisterReceiver(onComplete);
            }
            downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Bundle bundle = intent.getExtras();
                    long doId = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                    if(downloadId==doId&&isAppOnForeground(cordova.getActivity())) {
                        installApk(apkFile);
                        cordova.getActivity().unregisterReceiver(this);
                        cordova.getActivity().finish();
                    }
                }
            };

            //register receiver for when .apk download is compete
            cordova.getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            getProgress(downloadId,manager);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isAppOnForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        boolean isOnForground = false;
        try {
            List<ActivityManager.RunningAppProcessInfo> runnings = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo running : runnings) {
                if (running.processName.equals(context.getPackageName())) {
                    if (running.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                            || running.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                        //前台显示...
                        Log.e("my", "前台显示");
                        isOnForground = true;
                    } else {
                        //后台显示...
                        Log.e("my", "后台显示");
                        isOnForground = false;
                    }
                    break;
                }
            }
            String currentPackageName = "";
            if (am.getRunningTasks(1).size() > 0) {
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                currentPackageName = cn.getPackageName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOnForground;
    }


    void installApk(File apkFile){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(cordova.getActivity(),  cordova.getActivity().getApplication().getPackageName() + ".dmcInstall", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        cordova.getActivity().startActivity(intent);
    }

    File createFile(String name){
         String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
         String fileName = name;
         destination += fileName;
         Uri uri = Uri.parse("file://" + destination);
         //Delete update file if exists
         File file = new File(destination);
         if (file.exists()) {
             try {
                 file.delete();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return  file;
     }

    void getProgress(final long downloadId,final DownloadManager dm){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = dm.query(query);
                    if (cursor.moveToFirst()) {
                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int status=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status==DownloadManager.STATUS_FAILED ) {
                            downloading = false;
                        }

                        int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("progress",dl_progress);
                            jsonObject.put("size",bytes_total);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String format = "window.DownInstall.onDownLoadEvent(%s);";
                        final String js = String.format(format,jsonObject.toString());

                        instance.cordova.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                instance.webView.loadUrl("javascript:" + js);
                            }
                        });
                        cursor.close();
                    }
                }
            }
        }).start();
    }

    void getProgress2(final long downloadId,final DownloadManager dm){
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    Cursor cursor = dm.query(query);
                    if (cursor.moveToFirst()) {
                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int status=cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL || status==DownloadManager.STATUS_FAILED ) {
                            downloading = false;
                        }

                        int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                        JSONObject jsonObject=new JSONObject();
                        try {
                            jsonObject.put("progress",dl_progress);
                            jsonObject.put("size",bytes_total);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        PluginResult progressResult = new PluginResult(PluginResult.Status.OK, jsonObject);
                        progressResult.setKeepCallback(true);
                        myCallbackContext.sendPluginResult(progressResult);
                        cursor.close();
                    }
                }
            }
        }).start();
    }

    public void clearCurrentTask(long downloadId) {
        DownloadManager dm = (DownloadManager) cordova.getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            dm.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults)
        {
            if(r == PackageManager.PERMISSION_DENIED)
            {
                myCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "permission refused"));
                return;
            }
        }
        myCallbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "PERMS_GRANTED"));
        start();
    }


    private boolean canDownloadState() {
        try {
            int state = cordova.getActivity().getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");

            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
