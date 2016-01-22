package com.forrest.videopl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.winplus.serial.utils.Command;
import org.winplus.serial.utils.ReadPort;

import com.example.gpiocontroller.GpioTest;
import com.forrest.apkutil.ApkUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
	
	private final static int H_AudioManager = 1; //初始化AudioManager
	private final static int H_SmallPop = 2; //音量图标动态显示
	private final static int H_SmallPop_Hide = 3; //隐藏音量图标
	private final static int H_ReadPort = 4; //初始化读串口对象
	private final static int H_startReadPortThread = 5; //开启读串口线
	private final static int H_stopReadPortThread = 6;	//关闭读串口线
	
	private final static int H_ReadPortData = 1111; //串口发送数据
	private final static int H_Back2Luncher = 1112; //启动launcher3
	
	private final static int H_SetLeft = 2001;
	private final static int H_SetRight = 2002;
	private final static int H_SetUp = 2003;
	private final static int H_SetDown = 2004;
	
	private final static int H_InstallAPK = 3001;
	
	private final static int H_Register = 4001; //注册 mRegisterHandler消息
	private final static int H_Register_For_Debug = 4002;
	
	private SurfaceView mSurfaceView;
	private MediaPlayer mMediaPlayer;
	private SurfaceHolder mSurfaceHolder;
	
	private AudioManager mAudioManager;
	private int mMaxSound;
	private int mCurrentSound;
	private int mCurrentSound_50; //50阶对应的当前音量值
	/**设置是否循环播放的标记,true是，false不是*/
	private boolean mIsRircle = true;
	
//	private String path_tmp = "/mnt/sdcard2/niceView_2d.mp4";
//	private String path_tmp = "/mnt/usb_storage/USB_DISK2/video/v01.mp4";
	/**保存所有视频文件的路径*/
//	private String[] pathArr;
	/**实际有的视频个数*/
	private int mRealVideoNum = 0; 
	
	private VideoFileScaner mVideoFileScaner;
	private ArrayList<String> mVideoPath;
	private ArrayList<String> mApkPath;
	private String mCurPath; //记录当前播放视频的路径
	
	private Button btn_play;
	private Button btn_pause;
	private Button btn_next;
	private Button btn_volumeAdd;
	private Button btn_volumeSub;
	
	private View mSmallPopView = null;
	private PopupWindow mSmallPopWindow = null;
	private TextView mSmallPopTextV = null;
	private ImageView mSmallPopImageV = null;
	
	private ProgressBar pb_load;
	private TextView tv_notice;
	
	private boolean mPauseflag = false; //暂停键标记
	private boolean mKeyBack = false; //判断back键是否按下，false表示未按下
	
	private Register mRegister;
	private int mRegsResult; //注册结果
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private Layout mlayout;
	private final Timer mTimer = new Timer();
	private TimerTask mTimerTask;
	
	
	private boolean debug = false;
	private boolean show = false;
	
	private boolean hasNewApkflag = false;  //是否有newAPK的标记
	private String newApkPath = null; //newAPK路径
	
	private int mScanNum = 10;
	
	//----------------------------------------------
	private ReadPort mReadPort;
	private byte mSoundChannel = (byte)0xE1;
	
	//----------------------------------------------
	
	long time00;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
	            | View.SYSTEM_UI_FLAG_FULLSCREEN
	            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//		
		getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
		         | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
		         | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		         | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
		         | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
		         | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		
         
		setContentView(R.layout.activity_main);
		mMediaPlayer = new MediaPlayer();
		mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		
		btn_play = (Button)findViewById(R.id.btn_play);
		btn_pause = (Button)findViewById(R.id.btn_pause);
		btn_next = (Button)findViewById(R.id.btn_next);
		btn_volumeAdd = (Button)findViewById(R.id.btn_volumeAdd);
		btn_volumeSub = (Button)findViewById(R.id.btn_volumeSub);
		
		pb_load = (ProgressBar)findViewById(R.id.pb_loading);
		tv_notice = (TextView)findViewById(R.id.tv_notice);
				
		mSmallPopView = getLayoutInflater().inflate(R.layout.small_pop, null);
		mSmallPopWindow = new PopupWindow(mSmallPopView, 350, LayoutParams.WRAP_CONTENT);
		mSmallPopTextV = (TextView) mSmallPopView.findViewById(R.id.size);
		mSmallPopImageV = (ImageView) mSmallPopView.findViewById(R.id.sizeIcon);
		
		handler.sendEmptyMessage(H_AudioManager);
		
//		option();
//		int tmp = Encrypt.native_encrypt("/mnt/usb_storage/USB_DISK2/mingma", "helloworld", "/mnt/usb_storage/USB_DISK2/password");
		if(debug){
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage("测试APK,量产时请使用正式版本APK");
			builder.setCancelable(false);
			builder.create().show();
		}else {
			//mRegisterHandler.sendEmptyMessage(H_Register); //要打开
		}
		
		//隐藏SystemUI广播
		Intent intent=new Intent();
		intent.setAction("rk.android.phonestatusbar.hide");
		intent.putExtra("show", false);
		sendBroadcast(intent);

		//Log.v("yzh","tmp = " + tmp);
//		pathArr = new VideoPath().getAllVideo();
//		
//		for(String path: pathArr) { //获取实际视频的格式
//			if(path != null) {
//				mRealVideoNum ++;
//			}
//		}
		//开启自定义服务 设置屏幕
		ComponentName comp0 = new ComponentName("com.example.screenset", "com.example.screenset.ScreenService");
		Intent intent0 = new Intent();
		intent0.setComponent(comp0);
		startService(intent0);
		
		mVideoFileScaner = new VideoFileScaner();
//		mVideoFileScaner.scanFiles("/mnt");
		
		time00 = System.currentTimeMillis();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		//循环检测USB卡里面的视频,目的是保证开机时能够检测到视频文件
		while(mScanNum-- >= 0) {
			tv_notice.setText(R.string.find_video);
			mVideoFileScaner.scanPathFile("/mnt/usb_storage"); 		//扫描U盘里面的文件
//			mVideoFileScaner.scanPathFile("/storage/sdcard0");      //扫描内置SD卡里面的文件
			mVideoPath = mVideoFileScaner.getVideoList();
			mApkPath = mVideoFileScaner.getApkList();
			mRealVideoNum = mVideoPath.size();
			if(mRealVideoNum == 0) {
				try {//此段不能删除,保证开机时能搜索到视频文件
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				break;
			}
		}
		
		//检查是否有新的apk
		if(mApkPath.size() > 0) {
			for(int i=0; i<mApkPath.size();i++) {
				if(mApkPath.get(i).contains("VideoPL.apk")) {
					newApkPath = mApkPath.get(i); //获取newAPK路径
					if(ApkUtil.isNewVersion(this, this.getPackageName(), newApkPath)) {
						hasNewApkflag = true;
						pb_load.setVisibility(View.VISIBLE);
						tv_notice.setText(R.string.install_apk);
						tv_notice.setVisibility(View.VISIBLE);
						handler.sendEmptyMessageDelayed(H_InstallAPK,5000);
					}
					Log.d("yzh","APK Path = " + mApkPath.get(i));
				} 
			}
		}
		
		if(mRealVideoNum == 0 && !hasNewApkflag) {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.notice);
			builder.setMessage(R.string.no_video);
//			builder.setCancelable(false);
			builder.create().show();
//			handler.sendEmptyMessageDelayed(H_Back2Luncher, 3000);
		}
		
//		{ //设置全屏
//			handler.sendEmptyMessage(H_SetLeft);
//			handler.sendEmptyMessageDelayed(H_SetRight,200);
//			handler.sendEmptyMessageDelayed(H_SetUp,400);
//			handler.sendEmptyMessageDelayed(H_SetDown,600);
//		}
		
		for(int j=0;j<mRealVideoNum;j++) {
			Log.v("yzh","mVideoPath = " + mVideoPath.get(j));
		}
		Log.v("yzh","mRealVideoNum = " + mRealVideoNum);
	}
	
	/**播放制定路径的视频*/
	private void play(String path) {
		try{
			Log.e("yzh","play path = " + path);
			if(mMediaPlayer != null) {
				mMediaPlayer.reset();
			}
			setup();
			mCurPath = path;
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setDisplay(mSurfaceHolder);
			mMediaPlayer.setDataSource(path);
//			mMediaPlayer.prepare();
			mMediaPlayer.prepareAsync();
			}catch(Exception e){
				Log.e("yzh","can not play!");
				Log.e("yzh",e.toString());
				e.printStackTrace();
			}
		int sound = mMaxSound/2+mMaxSound%2;
//		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,sound, 0);
	}
	
	/**暂停视频操作*/
	private void pause() {
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mPauseflag = true;
			Log.v("yzh","mMediaPlayer.pause()");
		}
	}
	
	/**取消暂停*/
	private void unpause() {
		if(mMediaPlayer != null && mPauseflag) {
			mMediaPlayer.start();
			mPauseflag = false;
			Log.v("yzh","mMediaPlayer.unpause()");
		}
	}
	
	/**切换到下一个视频播放*/
	private void playNext() {
		int index=0;
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		index = mVideoPath.indexOf(mCurPath); //找到正在播放视频的下标
		index++;
		if(index<mRealVideoNum) {
			play(mVideoPath.get(index));
			index++;
		}else if(index == mRealVideoNum || index > mRealVideoNum) {
			index=0;
			play(mVideoPath.get(index));
			index++;
		}
	}
	
	/**切换到上一个视频*/
	private void playPrev() {
		int index=0;
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		index = mVideoPath.indexOf(mCurPath); //找到正在播放视频的下标
		index--;
		if(index < 0 ){
			index=0;
			play(mVideoPath.get(index));
			index--;
		}else{
			play(mVideoPath.get(index));
			index--;
		}
	}
	
	
	/**切换到自定路径视频播放*/
	private void playPath(String str) {
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		play(str);
	}
	
	/**给MediaPlayer设置各种监听器*/
	private void setup() {
		/**设置准好后的监听器*/
		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mMediaPlayer.start();
//				Log.d("yzh","mMediaPlayer.start " + (System.currentTimeMillis()-time00));
				//Toast.makeText(MainActivity.this, "time = " + (System.currentTimeMillis()-time00), Toast.LENGTH_LONG).show();
			}
		});
		/**设置视频播放完成的监听器*/
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.v("yzh","onCompletion");
				if(mIsRircle){ //设置了循环播放，播放完成后循环播放下一曲
//					playNext();
//					playPath(pathArr[0]);
					
					byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));//获取当前播放视频的下标
					
					playPath(mVideoPath.get(0));
					
					writePort((byte)0x04, curVideoIndex, (byte)0x00);
				}
			}
		});
		/**设置出错的监听器*/
		mMediaPlayer.setOnErrorListener(errorListener);
	}
	
	/**视频音量操作*/
	private void volumeAdd(){
		mCurrentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mCurrentSound += 1; 
		//if(mCurrentSound > mMaxSound || mCurrentSound == mMaxSound) {
		if(mCurrentSound > 16 || mCurrentSound == 16) {
			mCurrentSound = mMaxSound;
		}
		mCurrentSound_50 = mCurrentSound * 50 / mMaxSound;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentSound, 0);
	}
	
	private void volumeSub(){
		mCurrentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mCurrentSound -= 1; 
		if(mCurrentSound < 0 || mCurrentSound == 0) {
			mCurrentSound = 0;
		}
		mCurrentSound_50 = mCurrentSound * 50 / mMaxSound;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentSound, 0);
	}
	
	private void volumeCtl(int vol){
		if(vol < 0 || vol > 50){ //mMaxSound
			return ;
		}
		mCurrentSound_50 = vol;
		mCurrentSound = vol*mMaxSound/50;
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentSound, 0);
		Log.d("yzh","volumeCtl mCurrentSound = " + mCurrentSound);
	}
	
	//静音
	private boolean isMute = false;
	private void mute() {
		if(!isMute){
			mCurrentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			isMute = true;
		}
	}
	private void unmute() {
		if(isMute) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentSound, 0);
			isMute = false;
		}
	}
	
	@Override
	protected void onResume() {
		Log.v("yzh","onResume");
		super.onResume();
		 /* 测试打开
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				mRegisterHandler.sendEmptyMessage(H_Register_For_Debug);
			}
		};
		mTimer.schedule(mTimerTask, 1000*60*60*6, 1000*60*60*6);
		//*/
	}
	
	@Override
	protected void onPause() {
		Log.v("yzh","onPause");
		super.onPause();
		mTimer.cancel();
//		pause();
	}
	
	@Override
	protected void onStop() {
		Log.v("yzh","onStop");
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent("com.forrest.service.SCREENSERVICE"));
		Log.v("yzh","onDestroy");
	}
	/**给控件设置监听器*/
	private void option() {
		btn_play.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				play(pathArr[0]);
				play(mVideoPath.get(0));
			}
		});
		btn_pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pause();
			}
		});
		btn_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
			}
		});
		btn_volumeAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				volumeAdd();
				handler.sendEmptyMessage(H_SmallPop);
			}
		});
		btn_volumeSub.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				volumeSub();
				handler.sendEmptyMessage(H_SmallPop);
			}
		});
	}
	
	public void updateSizePopupWindowView(int type, float size) {
		int result;
		switch (type) {
		case 1:
			int totalVolum = mMaxSound;
			result = (int) (size * 100) / totalVolum;
			if (result < 0)
				result = 0;
			if (result > 100)
				result = 100;
			if (result == 100) {
				mSmallPopImageV.setImageResource(R.drawable.voice_100);
			} else if (result < 100 && result >= 50) {
				mSmallPopImageV.setImageResource(R.drawable.voice_60);
			} else if (result < 60 && result > 0) {
				mSmallPopImageV.setImageResource(R.drawable.voice_30);
			} else if (result == 0) {
				mSmallPopImageV.setImageResource(R.drawable.voice_mute);
			}
			mSmallPopTextV.setText(result + "%");
			break;
		case 1024:
			break;
		default:
			break;
		}
	}
	
	byte tmp_cmp[] =new byte[]{-42,-74,-106,-118,86,54,22}; //零时指令
	
	Handler handler =  new Handler() {
		private int vol;
		byte readByte = 0;
		long time0  = 0;
		Command cmd = null;
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case H_AudioManager: //初始化音量管理，获取最大音量值和当前音量值
				handler.removeMessages(H_AudioManager);
				mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				mMaxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			    mCurrentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			    Log.v("yzh","Sound Max/Curr "+mMaxSound+"/"+mCurrentSound);
				break;
			case H_SmallPop: //音量图标动态显示
				/*
				handler.removeMessages(H_SmallPop);
				handler.removeMessages(H_SmallPop_Hide);
//				time0 = System.currentTimeMillis();
				if(!mSmallPopWindow.isShowing()) {
//					mSmallPopWindow.showAtLocation(mSurfaceView, Gravity.CENTER_VERTICAL, 0, 0);
					mSmallPopWindow.showAsDropDown(mSurfaceView,810,-540);
				}
				if(mAudioManager != null){
					//vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					vol = mCurrentSound;
				}
				updateSizePopupWindowView(1,vol);
				handler.sendEmptyMessageDelayed(H_SmallPop_Hide, 3000);
				// */
				break;
			case H_SmallPop_Hide://音量图标动态消失
				/*
				handler.removeMessages(H_SmallPop_Hide);
				if(mSmallPopWindow.isShowing()) {
					mSmallPopWindow.dismiss();
				}
				// */
				break;
			case H_ReadPort: //初始化读串口对象
				handler.removeMessages(H_ReadPort);
				mReadPort = new ReadPort(handler);
				handler.sendEmptyMessageDelayed(H_startReadPortThread,5000);
				break;
			case H_startReadPortThread: //开启读串口线
				handler.removeMessages(H_startReadPortThread);
				if(mReadPort != null) {
					mReadPort.startReadThread();
				}
				break;
			case H_stopReadPortThread:
				handler.removeMessages(H_stopReadPortThread);
				if(mReadPort != null) {
					mReadPort.stopReadThread();
				}
				break;
			case H_ReadPortData: //读取串口命令并处理
				handler.removeMessages(H_ReadPortData);
//				readByte = (byte)msg.obj;
				readByte = -1;
				cmd = (Command)msg.obj;
				processCmd(cmd);
				
				/*
				if(readByte >=0 && readByte < mRealVideoNum) {
					play(mVideoPath.get(readByte));
					break;
				}else if(readByte == (byte)0XF1) {
					volumeAdd();
					handler.sendEmptyMessage(H_SmallPop);
					break;
				}else if(readByte == (byte)0XF2) {
					volumeSub();
					handler.sendEmptyMessage(H_SmallPop);
					break;
				}else if(readByte == (byte)0XF3) {
					mute();
					handler.sendEmptyMessage(H_SmallPop);
					break;
					
				}else if(readByte == (byte)0XE0) {
					GpioTest.writeGpio('0');
					break;
					
				}else if(readByte == (byte)0XE1) {
					GpioTest.writeGpio('1');
					break;
					
				}else if(readByte == (byte)0XE2) {
					GpioTest.writeGpio('2');
					break;
					
				}else if(readByte == (byte)0XE3) {
					GpioTest.writeGpio('3');
					break;
					
				}else if(readByte == (byte)0XE4) {
					GpioTest.writeGpio('4');
					break;
					
				}else if(readByte == (byte)0XE5) {
					GpioTest.writeGpio('5');
					break;
					
				}else if(readByte == (byte)0XE6) {
					GpioTest.writeGpio('6');
					break;
					
				}else if(readByte == (byte)0XE7) {
					GpioTest.writeGpio('7');
					break;
					
				}
				else {
					Toast.makeText(MainActivity.this,"没有对应命令" + "接收到指令是:" + readByte,Toast.LENGTH_SHORT).show();
					break;
				}*/
				break;
			case H_Back2Luncher: //启动launcher3
				ComponentName comp = new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher");
				Intent intent = new Intent();
				intent.setComponent(comp);
				MainActivity.this.startActivity(intent);
				Log.d("yzh","back to Launcher3");
				break;
			//-----------调整屏幕------------------- begin
			case H_SetRight:
				Intent intent1 = new Intent("com.forrest.ScreenSet");
				intent1.putExtra("orientation", "right");
				MainActivity.this.sendBroadcast(intent1);
				Log.d("yzh","H_SetRight");
				break;
			case H_SetLeft:
				Intent intent2 = new Intent("com.forrest.ScreenSet");
				intent2.putExtra("orientation", "left");
				MainActivity.this.sendBroadcast(intent2);
				Log.d("yzh","H_SetLeft");
				break;
			case H_SetUp:
				Intent intent3 = new Intent("com.forrest.ScreenSet");
				intent3.putExtra("orientation", "up");
				MainActivity.this.sendBroadcast(intent3);
				Log.d("yzh","H_SetUp");
				break;
			case H_SetDown:
				Intent intent4 = new Intent("com.forrest.ScreenSet");
				intent4.putExtra("orientation", "down");
				MainActivity.this.sendBroadcast(intent4);
				Log.d("yzh","H_SetDown");
				break;
				//-----------调整屏幕------------------- end
			
			case H_InstallAPK: //安装新apk
				ApkUtil.myInstall(MainActivity.this, newApkPath);
				Log.d("yzh","H_InstallAPK");
				break;
			default:
				break;
			}
		};
	};
	
	/**出错监听器*/
	private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
			Log.e("yzh", "********OnErrorListener*******+framework_err="
					+ framework_err + "impl_err=" + impl_err);
			switch (framework_err) {
			case MediaPlayer.MEDIA_ERROR_IO:
				break;
			case MediaPlayer.MEDIA_ERROR_MALFORMED:
				break;
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				break;
			case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
				break;
			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				break;
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:// 未知错误
				return true;
			case -38:
				return true;
			default:
				break;
			}
//			handler.sendEmptyMessage(MEDIALPLAY_ERROR);
			return true;
		}
	};
	/**物理按键监听*/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			mKeyBack = true;
			Log.v("yzh","backkey");
			handler.sendEmptyMessage(H_Back2Luncher);
			finish();
			break;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.v("yzh","surfaceCreated");
		//GpioTest.writeGpio('1'); //写GPIO实现HDMI输出
		if(mVideoPath.size()>0 && !hasNewApkflag /*&& mRegsResult == 1*/) {
//			if(pb_load.getVisibility() == View.VISIBLE) {
//				pb_load.setVisibility(View.GONE);
//				tv_notice.setVisibility(View.GONE);
//			}
			play(mVideoPath.get(0));
			handler.sendEmptyMessage(H_ReadPort);
		}else if(mVideoPath.size()==0){
			Log.d("yzh","no video");
			Toast.makeText(this, R.string.no_videofile, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,	int height) {
		Log.v("yzh","surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.v("yzh","surfaceDestroyed");
		if(mMediaPlayer != null){
			mMediaPlayer.pause();
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		handler.sendEmptyMessage(H_stopReadPortThread);
	}
	
	Handler mRegisterHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case H_Register:
				Log.d("yzh","register handler");
				register();
				break;
			case H_Register_For_Debug: //测试时使用的提示框
				Log.d("yzh","register H_Register_For_Debug");
				register_debug();
				break;
			default:
				break;
			}
		};
	};
	
	/**写串口数据*/
	public boolean writePort(byte cmd, byte para1, byte para2){
		byte write_buf[] = new byte[Command.CMD_LEN];
		write_buf[0] = (byte)0x55;
		write_buf[1] = (byte)cmd;
		write_buf[2] = (byte)para1;
		write_buf[3] = (byte)para2;
		write_buf[4] = (byte)(cmd+para1+para2);
		write_buf[5] = (byte)0xAA;
		if(mReadPort != null) {
			try {
				mReadPort.getOutputStream().write(write_buf);
				Log.d("yzh","writePort = " + Command.toHex(write_buf[0]) +"_"+ Command.toHex(write_buf[1]) +"_"+ Command.toHex(write_buf[2]) 
						+"_"+ Command.toHex(write_buf[3]) +"_"+ Command.toHex(write_buf[4]) +"_"+ Command.toHex(write_buf[5]));
				return true;
			} catch (IOException e) {
				Log.e("yzh",e.toString());
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	/**单片机串口数据处理程序*/
	public boolean processCmd(Command cmd){
		//Toast.makeText(MainActivity.this,"接收到指令是:" + Command.toString(cmd),Toast.LENGTH_SHORT).show();
		if(cmd == null || cmd.getBegin() != (byte)0x55 || cmd.getEnd() != (byte)0xAA){
			return false;			
		}
		Log.d("yzh","cmd =" + Command.toString(cmd));
		if(cmd.getCmd() == (byte)0X01 ) { //0x01播放视频指令//>=0 && readByte < mRealVideoNum
			Log.d("yzh","+++");
			if(cmd.getPara1() >= 0 && cmd.getPara1() < mRealVideoNum) {
				Log.d("yzh","cmd.getPara1() = " + cmd.getPara1());
				play(mVideoPath.get(cmd.getPara1()));
				writePort(cmd.getCmd(), (byte)0x00, (byte)0x00);
			}
			return true;
		}else if(cmd.getCmd() == (byte)0X02) {
			//0x02获取当前播放的视频状态 
			if(mPauseflag){ 
				byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));
				writePort(cmd.getCmd(), (byte)0x01, curVideoIndex);
			}else{
				byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));
				writePort(cmd.getCmd(), (byte)0x02, curVideoIndex);
			}
			return true;
		
		}else if(cmd.getCmd() == (byte)0X03) { /******视频控制*********/
			
			if(cmd.getPara1() == (byte)0X01) { //01播放 0x02暂停 0x03上一曲 0x04下一曲
				unpause();
			}else if(cmd.getPara1() == (byte)0X02) {
				pause();
			}else if(cmd.getPara1() == (byte)0X03) {
				playPrev();
			}else if(cmd.getPara1() == (byte)0X04) {
				playNext();
			}else{
				return false;
			}
			writePort(cmd.getCmd(), (byte)0x00, (byte)0x00);
			return true;
			
		}else if(cmd.getCmd() == (byte)0X04) {
			//0x04曲目播放结束 由4K播放板主动发起
			return true;
		
		}else if(cmd.getCmd() == (byte)0X05) {  //0x05 获取视频总个数,和当前正在播放的视频序号
			int totalVideo = mRealVideoNum;
			int curVideo = mVideoPath.indexOf(mCurPath);
			writePort(cmd.getCmd(), (byte)totalVideo, (byte)curVideo);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X11) {
			//声音控制
			//将音量设计为指定值
			volumeCtl((int)cmd.getPara1());
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, cmd.getPara1(), (byte)0x00);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X12) { 
			//获取当前音量值
			writePort((byte)0x12, (byte)mCurrentSound_50, (byte)0x00);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X13) {
			//音量+
			volumeAdd();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X14) {
			//音量-
			volumeSub();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X15) {
			//静音
			mute();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X16) {
			//取消静音
			unmute();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
			
		}else if(cmd.getCmd() == (byte)0X21) { /********声音通道选择，并执行**************/
			
			if(cmd.getPara1() == (byte)0XE0) {
				GpioTest.writeGpio('0');
				mSoundChannel = (byte)0XE0;
				writePort((byte)0x12, (byte)0XE0, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE1) {
				mSoundChannel = (byte)0XE1;
				GpioTest.writeGpio('1');
				writePort((byte)0x12, (byte)0XE1, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE2) {
				GpioTest.writeGpio('2');
				mSoundChannel = (byte)0XE2;
				writePort((byte)0x12, (byte)0XE2, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE3) {
				mSoundChannel = (byte)0XE3;
				GpioTest.writeGpio('3');
				writePort((byte)0x12, (byte)0XE3, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE4) {
				mSoundChannel = (byte)0XE4;
				GpioTest.writeGpio('4');
				writePort((byte)0x12, (byte)0XE4, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE5) {
				mSoundChannel = (byte)0XE5;
				GpioTest.writeGpio('5');
				writePort((byte)0x12, (byte)0XE5, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE6) {
				mSoundChannel = (byte)0XE6;
				GpioTest.writeGpio('6');
				writePort((byte)0x12, (byte)0XE6, (byte)0x00);
				
			}else if(cmd.getPara1() == (byte)0XE7) {
				GpioTest.writeGpio('7');
				mSoundChannel = (byte)0XE7;
				writePort((byte)0x12, (byte)0XE7, (byte)0x00);
				
			}
			return true;
			
		}else if(cmd.getCmd() == (byte)0X22) {
			writePort((byte)0x12, (byte)mSoundChannel, (byte)0x00);
			return true;
			
		}else {
			Toast.makeText(MainActivity.this,"没有对应命令" + "接收到指令是:" + Command.toString(cmd),Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	/**注册*/
	private void register() {
		
		mSharedPreferences = getSharedPreferences("helloworld20150924",Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		 
//		mEditor.clear();
//		mEditor.commit();
		
		//注册提示框
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.register_edit, null);
		
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog;
		final Button btn_ok = (Button)view.findViewById(R.id.btn_ok);
		final Button btn_cancel = (Button)view.findViewById(R.id.btn_cancel);;
		final EditText editText = (EditText)view.findViewById(R.id.editText1);
		Log.d("yzh","estar = " +mSharedPreferences.getInt("John_Nash", 523) );
		if(!(mSharedPreferences.getInt("John_Nash", 523) == 1928)) {  //没有注册的时候 John_Nash = 1928 表示已经注册好了
			Toast.makeText(this, "没有注册", Toast.LENGTH_SHORT).show();
			mRegister = new Register();
			builder.setTitle(R.string.register);
			builder.setView(view);
			builder.setCancelable(false);
			dialog = builder.create();
			dialog.show();
			dialog.getWindow().setLayout(900, 600);
			
			btn_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String str = editText.getText().toString();
					Log.v("yzh","str = " + str);
					mRegsResult = mRegister.register(str);
					Log.v("yzh","register = " + mRegsResult);
					if(mRegsResult == -4){
						editText.setText("解密出错");
						editText.setTextColor(Color.RED);
					}
					else if(mRegsResult == -3) {
						editText.setText(R.string.register_haveRegs);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == -2) {
						editText.setText(R.string.register_errorsdcard);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == -1) { //password 不存在
						editText.setText(R.string.register_nopassword);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == 0) {
						editText.setText(R.string.register_failure);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == 1) {
						dialog.dismiss();
						mEditor.putInt("John_Nash", 1928); 
						mEditor.putInt("Isaac_Newton", 1643);
						mEditor.putInt("Albert_Einstein", 1879);
						mEditor.commit();
						Log.v("yzh","ok regs");
						Toast.makeText(MainActivity.this, R.string.register_ok, Toast.LENGTH_LONG).show();
					}
				}
			});
			
			btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
			

		}else {
			Log.v("yzh","ok!!");
			mRegsResult = 1;
		}
	}
	
	/**测试用使用的提示框*/
	private void register_debug(){
		//注册提示框
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.register_edit, null);
		
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog;
		final Button btn_ok = (Button)view.findViewById(R.id.btn_ok);
		final Button btn_cancel = (Button)view.findViewById(R.id.btn_cancel);;
		final EditText editText = (EditText)view.findViewById(R.id.editText1);
		
			Toast.makeText(this, "Not Register!", Toast.LENGTH_SHORT).show();
			builder.setTitle(R.string.register);
			builder.setView(view);
			builder.setCancelable(false);
			dialog = builder.create();
			dialog.show();
			
			dialog.getWindow().setLayout(900, 600);
			
			btn_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String str = editText.getText().toString();
					if(str.equals("ml400")) {
						mRegsResult = 1;
					}else {
						mRegsResult = 0;
					}
					if(mRegsResult == 1) {
						dialog.dismiss();
					}else if(mRegsResult == 0) {
						editText.setText(R.string.register_failure);
						editText.setTextColor(Color.RED);
					}
				}
			});
			
			btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
	}
}
