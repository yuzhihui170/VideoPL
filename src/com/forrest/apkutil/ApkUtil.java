package com.forrest.apkutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.util.Log;

public class ApkUtil {
	private int getUninstallAPKInfo(Context ctx,String archiveFilePath) {
		int versionCode = 0;
		String versionName = null;
		String appName = null;
		String pakName = null;
		PackageManager pm=ctx.getPackageManager();
		PackageInfo pakinfo=pm.getPackageArchiveInfo(archiveFilePath,PackageManager.GET_ACTIVITIES);
		if (pakinfo!=null) {
			ApplicationInfo appinfo=pakinfo.applicationInfo;
			//versionName=pakinfo.versionName;
			versionCode = pakinfo.versionCode;
			//Drawable icon=pm.getApplicationIcon(appinfo);
			//appName=(String) pm.getApplicationLabel(appinfo);
			//pakName=appinfo.packageName;
			Log.d("yzh","versionCode = " + versionCode);
			//Log.d("yzh","versionName = " + versionName);
			//Log.d("yzh","appName = " + appName);
			//Log.d("yzh","pakName = " + pakName);
		}
		
		return versionCode;
	}
	
	public int getCurrentAPKInfo(Context ctx) {
		int versionCode = 0;
		String versionName = null;
		String appName = null;
		String pakName = null;
		PackageManager pm=ctx.getPackageManager();
		PackageInfo pakinfo;
		try {
			pakinfo = pm.getPackageInfo(ctx.getPackageName(),PackageManager.GET_ACTIVITIES);
			if (pakinfo!=null) {
				ApplicationInfo appinfo=pakinfo.applicationInfo;
				//versionName=pakinfo.versionName;
				versionCode = pakinfo.versionCode;
				//Drawable icon=pm.getApplicationIcon(appinfo);
				//appName=(String) pm.getApplicationLabel(appinfo);
				//pakName=appinfo.packageName;
			}
			Log.d("yzh","versionCode = " + versionCode);
			//Log.d("yzh","versionName = " + versionName);
			//Log.d("yzh","appName = " + appName);
			//Log.d("yzh","pakName = " + pakName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}
	
	/**鍒ゆ柇鏄惁瀹夎鑾簲鐢�
	 * @param packname 搴旂敤鐨勫寘鍚�
	 * @return return true 宸插皢瀹夎; return false 鏈畨瑁�
	 * */
	public static boolean isInstallPackage(Context mcontext, String packname) {
		try {
			PackageManager manager = mcontext.getPackageManager();
			List<PackageInfo> packageInfos = manager.getInstalledPackages(0);
			PackageInfo info = null;
			for (PackageInfo packageInfo : packageInfos) {
				String name = packageInfo.packageName;
				if (name != null && name.indexOf(packname) > -1) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Log.e("yzh",e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	/**判断是否是新版apk
	 * @param oldPackname 
	 * @param newPacknameFilePath
	 * @return return true ; return false
	 * */
	public static boolean isNewVersion(Context mcontext, String oldPackname, String newPacknameFilePath) {
		PackageManager pm = mcontext.getPackageManager();
		PackageInfo pakinfo_old;
		PackageInfo pakinfo_new;
		try {
			pakinfo_old = pm.getPackageInfo(oldPackname, PackageManager.GET_ACTIVITIES);
			pakinfo_new = pm.getPackageArchiveInfo(newPacknameFilePath, PackageManager.GET_ACTIVITIES);
			
			if(pakinfo_old.packageName.equals(pakinfo_new.packageName)) { 
				Log.d("yzh","pakinfo_old.versionCode/pakinfo_new.versionCode = " + pakinfo_old.versionCode  + "/" + pakinfo_new.versionCode);
				if(pakinfo_old.versionCode < pakinfo_new.versionCode) {
					Log.d("yzh","find new version");
					return true;
				}else {
					Log.d("yzh","not find new version");
					return false;
				}
			}else {
				Log.d("yzh","not same apk");
				return false;
			}
		} catch (NameNotFoundException e) {
			Log.e("yzh",e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	/** 
	 * 静默安装
	 * @param file 
	 * @return 
	 */  
	public static boolean slientInstall(File file) {  
	    boolean result = false;  
	    Process process = null;  
	    OutputStream out = null; 
	    String line = null;  
	    try {  
	    		String cmd = "pm install -r " + file.getPath();
	    		Log.d("yzh","cmd = " + cmd);
	    		process = Runtime.getRuntime().exec(cmd);  
	    		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));  
	    		while ((line = in.readLine()) != null) {  
	    			Log.d("yzh","pm install " + line);
	    		}  
	      //  out = process.getOutputStream();  
	       // DataOutputStream dataOutputStream = new DataOutputStream(out);  
	       // dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");  
	        //dataOutputStream.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + file.getPath());  
	       // dataOutputStream.writeBytes("pm install" + file.getPath());  
	      
	       // dataOutputStream.flush();  
	       
	       // dataOutputStream.close();  
	      //  out.close();  
	        int value = process.waitFor();  
	        Log.d("yzh","+++++++++");  
	       
	        if (value == 0) {  
	            result = true;
	            Log.d("yzh","install ok");  
	        } else if (value == 1) { 
	        	 Log.d("yzh","install faile");  
	            result = false;  
	        } else { 
	        	 Log.d("yzh","install unknown");  
	            result = false;  
	        }  
	    } catch (IOException e) {
	    	Log.e("yzh",e.toString());
	        e.printStackTrace();  
	    } catch (InterruptedException e) {  
	    	Log.e("yzh",e.toString());
	        e.printStackTrace();  
	    }  
	    return result;  
	}
	
	public static void myInstall(Context mcontext, String newApkPath) {
		File file1 = new File(newApkPath);
		if(file1.exists()) {
			if(isNewVersion(mcontext,mcontext.getPackageName(),newApkPath)) {
				slientInstall(new File(newApkPath));
			}
		}else {
			Log.d("yzh","apk not exit");
		}
	}
}







