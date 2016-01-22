package com.forrest.videopl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * 接收开机启动完成后的广播
 * */
public class BootBroadcastReceiver extends BroadcastReceiver {
	private final String ACTION = "android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			//Intent intent2 = new Intent(context, MainActivity.class);
			//intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//context.startActivity(intent2);
			//Log.d("yzh", "startActivity");

			// Intent intentService = new Intent();
			// intentService.setClass(context, MyService.class);
			// context.startService(intentService);
//			Intent intent = new Intent();
		}
	}
}