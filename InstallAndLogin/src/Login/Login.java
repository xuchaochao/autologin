package Login;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;
import java.io.IOException;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

//   本项目地址：/Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin
//   android list : id 2 or "android-23" Android 6.0
//  步骤：
//  android create uitest-project -n 18267990494 -t 2 -p /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin
//  /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml
//  ant -buildfile /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/build.xml
//  adb -s O7E6HYS499999999 push /Users/xcc/Documents/Code/JavaCode/Test/InstallAndLogin/bin/18267990494.jar /data/local/tmp/
//  adb -s O7E6HYS499999999 shell uiautomator runtest 18267990494.jar -c Login.Login

public class Login extends UiAutomatorTestCase {
	public void testLogin() throws InterruptedException, UiObjectNotFoundException, IOException {
		Runtime.getRuntime().exec("am start -n com.iscs.mobilewcs/com.iscs.mobilewcs.activity.login.LoginActivity");
		try {
			// 华为上弹出的允许获得地理位置的弹窗，在全新安装上会出现
			UiObject okButton = new UiObject(new UiSelector().resourceId("com.huawei.systemmanager:id/btn_allow"));
			okButton.click();
		} catch (Exception e) {
			// TODO: handle exception
		}
		UiObject mobileEditText = new UiObject(new UiSelector().resourceId("com.iscs.mobilewcs:id/et_login_id"));
		UiObject passwordEditText = new UiObject(new UiSelector().resourceId("com.iscs.mobilewcs:id/et_login_pwd"));
		UiObject loginButton = new UiObject(new UiSelector().resourceId("com.iscs.mobilewcs:id/btn_login"));
		mobileEditText.clearTextField();

		mobileEditText.setText("18267990494");
		passwordEditText.clearTextField();
		passwordEditText.setText("123456a");
		UiDevice.getInstance().pressBack();
		loginButton.click();
		try {
			// 绑定按钮，适用于不同设备登录
			UiObject bindButton = new UiObject(new UiSelector().resourceId("android:id/button1"));
			bindButton.click();
		} catch (Exception e) {
		}
	}
}