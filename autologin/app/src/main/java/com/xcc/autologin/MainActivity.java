package com.xcc.autologin;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    private final static int RUNTIME_PERMISSION_REQUEST_CODE = 0x1;
    private Button login_phoneNumber;
    boolean isRoot = false;
    UtilsFTP ftp = null;
    TextView tv;
    Activity self;
    private File mLocalFile;
    private Thread mThread;
    String chooseOne;
    DataOutputStream os;


    private void login(String path) {
        if (hasRootPerssion()) {
            toast("安装登录中...");
            try {
                Process process = null;
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("pm install -r " + path + "\n");
                os.writeBytes("uiautomator runtest 18267990494.jar --nohup -c Login.Login"+ "\n");
                os.writeBytes("exit\n");
                os.flush();
                os.close();
            } catch (Exception e) {
            }
        } else {
            if (!hasEnv()) {
                toast("未Root用户需打开辅助服务...");
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivityForResult(intent, 0);
            } else {
                if (path == "Dev") {
                    toast("自动安装中...");
                    startInstall("app-DT1.apk");
                } else {
                    toast("自动安装中...");
                    startInstall("app-" + path + ".apk");
                }
            }
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String path = (String) msg.obj;
                    toast("下载完毕");
                    login(path);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login_phoneNumber = (Button) findViewById(R.id.login_phoneNumber);
        login_phoneNumber.setOnClickListener(this);
        self = this;
        tv = (TextView) findViewById(R.id.tv);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Android M 处理Runtime Permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {//检查是否有写入SD卡的授权
                Log.i(TAG, "granted permission!");
            } else {
                Log.i(TAG, "denied permission!");
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "should show request permission rationale!");
                }
                requestPermission();
            }
        } else {
        }
        //root权限
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        buttonClick(R.id.btn_testdownloadDEV, "Dev");
        buttonClick(R.id.btn_testdownloadCIT, "CIT");
        buttonClick(R.id.btn_testdownloadSIT, "SIT");
        buttonClick(R.id.btn_testdownloadUAT, "UAT");

        downloadAndLogin(R.id.btn_testdownloadAndLoginDEV, "Dev");
        downloadAndLogin(R.id.btn_testdownloadAndLoginCIT, "CIT");
        downloadAndLogin(R.id.btn_testdownloadAndLoginSIT, "SIT");
        downloadAndLogin(R.id.btn_testdownloadAndLoginUAT, "UAT");


    }


    // 判断是否有root权限
    public static boolean hasRootPerssion() {
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean returnResult(int value) {
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }


    // 静默安装
    private void clientInstall(String apkPath) {
        InstallUtils.installSilent(apkPath);
    }

    //下载+安装并且登陆
    private void downloadAndLogin(int buttonID, final String enviroment) {
        findViewById(buttonID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                chooseOne = enviroment;
                downAndLogin(enviroment);

            }
        });

    }

    //下载+安装
    private void buttonClick(int buttonID, final String enviroment) {
        findViewById(buttonID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                chooseOne = enviroment;
                startDownAndInstall(enviroment);
                Debug.stopMethodTracing();

            }
        });
    }
    String path;
    private void downAndLogin(final String whatIt) {
        new Thread() {
            public void run() {
                try {
                    UtilsFTP.IProgressListener listener = new UtilsFTP.IProgressListener() {
                        long BEG = 0;
                        @Override
                        public void onProgress(long len, long total) {
                            if (System.currentTimeMillis() - BEG > 200 || len == total) {
                                BEG = System.currentTimeMillis();
                                String result = String.format("%.2f", 100 * (double) len / (double) total);
                                showTV(result + "%");
                                if (TextUtils.equals("100.00", result)) {
                                    Message msg = Message.obtain();
                                    msg.what = 0;
                                    msg.obj = path;
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                    };

                    if (whatIt == "Dev") {
                        path = Environment.getExternalStorageDirectory().toString() + "/app-DT1.apk";
                    } else {
                        path = Environment.getExternalStorageDirectory().toString() + "/app-" + whatIt + ".apk";
                    }
                    Log.d("path----->", path);
                    ///  storage/emulated/0/app-CIT.apk
                    mLocalFile = new File(path);
                    if (whatIt == "Dev") {
                        ftp.downloadWithProgress("Latest/" + whatIt + "/apk/app-DT1.apk", mLocalFile, listener);
                    } else {
                        ftp.downloadWithProgress("Latest/" + whatIt + "/apk/app-" + whatIt + ".apk", mLocalFile, listener);
                    }
                } catch (Exception e) {
                }
            }
        }.start();
    }


    private void startDownAndInstall(final String whatIt) {
        new Thread() {
            public void run() {
                try {
                    UtilsFTP.IProgressListener listener = new UtilsFTP.IProgressListener() {
                        long BEG = 0;

                        @Override
                        public void onProgress(long len, long total) {
                            if (System.currentTimeMillis() - BEG > 200 || len == total) {
                                BEG = System.currentTimeMillis();
                                String result = String.format("%.2f", 100 * (double) len / (double) total);
                                showTV(result + "%");
                            }
                        }
                    };
                    String path;
                    if (whatIt == "Dev") {
                        path = Environment.getExternalStorageDirectory().toString() + "/app-DT1.apk";
                    } else {
                        path = Environment.getExternalStorageDirectory().toString() + "/app-" + whatIt + ".apk";
                    }
                    Log.d("path----->", path);
                    ///  storage/emulated/0/app-CIT.apk
                    mLocalFile = new File(path);
                    if (whatIt == "Dev") {
                        ftp.downloadWithProgress("Latest/" + whatIt + "/apk/app-DT1.apk", mLocalFile, listener);
                    } else {
                        ftp.downloadWithProgress("Latest/" + whatIt + "/apk/app-" + whatIt + ".apk", mLocalFile, listener);
                    }
                    toast(whatIt + "下载完毕");

                    if (hasRootPerssion()) {
                        toast("Root安装中...");
                        try {
                            Process process = null;
                            process = Runtime.getRuntime().exec("su");
                            os = new DataOutputStream(process.getOutputStream());
                            os.writeBytes("pm install -r " + path + "\n");
                            os.writeBytes("am start -n com.iscs.mobilewcs/com.iscs.mobilewcs.activity.login.LoginActivity" + "\n");
                            os.writeBytes("exit\n");
                            os.flush();
                            os.close();

//                            Intent intent = getPackageManager().getLaunchIntentForPackage("com.iscs.mobilewcs");
//                            startActivity(intent);
                        } catch (Exception e) {

                        }
                    } else {
                        if (!hasEnv()) {
                            toast("未Root用户需打开辅助服务...");
                            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                            startActivityForResult(intent, 0);
                        } else {
                            if (whatIt == "Dev") {
                                toast("自动安装中...");
                                startInstall("app-DT1.apk");
                            } else {
                                toast("自动安装中...");
                                startInstall("app-" + whatIt + ".apk");
                            }
                        }
                    }
                } catch (Exception e) {
                    toast(e.toString());
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
            mThread = null;
        }
        super.onDestroy();
    }

    private void connect() {
        final String IP = "192.168.6.49";
        final String loginname = "ftpus";
        final String pwd = "ftpus";
        mThread = new Thread() {
            public void run() {
                ftp = new UtilsFTP(IP, 21, loginname, pwd, false);
                try {
                    ftp.connect();
                    toast("FTP连接成功");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    toast(e.toString());
                    e.printStackTrace();
                }
            }

        };
        mThread.start();
    }

    public static final String TAG = MainActivity.class.getSimpleName();

    private void startInstall(final String appWhat) {
        new Thread() {

            @Override
            public void run() {
                if (hasRootPerssion()) {
                    if (mLocalFile != null) {
                        String path = Environment.getExternalStorageDirectory().toString() + "/" + appWhat;
                        mLocalFile = new File(path);
                        final String apk = mLocalFile.getAbsolutePath();
                        clientInstall(apk);
                    }
                } else {
                    if (mLocalFile != null) {
                        String path = Environment.getExternalStorageDirectory().toString() + "/" + appWhat;
                        mLocalFile = new File(path);
                        final String apk = mLocalFile.getAbsolutePath();
                        MyAccessibilityService.addInstalledWhitelList(InstallUtils.getAppNameByReflection(MainActivity.this, apk));
                        InstallUtils.installNormal(MainActivity.this, apk);
                    }
                }
            }
        }.start();
    }

    //  这个是为了监听在没有打开辅助服务的情况下先去打开辅助服务然后返回直接就安装了
    //  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("tag", "requestCode = " + requestCode);
        Log.d("tag", "hasEnv() = " + hasEnv());
        if (requestCode == 0 && hasEnv()) {
            if (chooseOne == "Dev") {
                toast("自动安装中...");
                startInstall("app-DT1.apk");
            } else {
                toast("自动安装中...");
                startInstall("app-" + chooseOne + ".apk");
            }
        }
    }

    private boolean hasEnv() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ServiceUtils.isAccessibilitySettingsOn(MainActivity.this);
    }

    private final static int CMD_TOAST_INFO = 0;
    private final static int CMD_TEXTVIEW_INFO = 1;

    private void toast(String info) {
        Message msg = commonhandler.obtainMessage();
        msg.what = CMD_TOAST_INFO;
        msg.obj = info;
        commonhandler.sendMessage(msg);
    }

    private void showTV(String info) {
        Message msg = commonhandler.obtainMessage();
        msg.what = CMD_TEXTVIEW_INFO;
        msg.obj = info;
        commonhandler.sendMessage(msg);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            android.os.Process.killProcess(android.os.Process.myPid());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler commonhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case CMD_TOAST_INFO:
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case CMD_TEXTVIEW_INFO:
                    tv.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    public void toastStart() {
        Toast.makeText(getApplication(), "开始...", Toast.LENGTH_LONG).show();
    }


    private void rootRun(String command) throws IOException {
        os.writeBytes(command + "\n");
        toastStart();
        os.writeBytes("exit\n");
        os.flush();
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.login_phoneNumber:
                login();
                break;
            default:
                break;
        }
    }


    private void login() {
        try {
            Process process = null;
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("uiautomator runtest 18267990494.jar --nohup -c Login.Login"+ "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RUNTIME_PERMISSION_REQUEST_CODE) {
            for (int index = 0; index < permissions.length; index++) {
                String permission = permissions[index];
                if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
                    if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "onRequestPermissionsResult: permission is granted!");
                    } else {
                        showMissingPermissionDialog();
                    }
                }
            }
        }
    }


    /**
     * 显示打开权限提示的对话框
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("去打开");
        builder.setMessage("还是去打开");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "哈哈 错误了吧", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                turnOnSettings();
            }
        });

        builder.show();
    }


    /**
     * 启动系统权限设置界面
     */
    private void turnOnSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }


    /**
     * 申请写入sd卡的权限
     */
    private void requestPermission() {
        //	调用系统相机拍照是不需要6.0的camera权限的, 但是需要读写sd卡的权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RUNTIME_PERMISSION_REQUEST_CODE);
    }


}
