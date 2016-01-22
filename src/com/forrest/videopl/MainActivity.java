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
	
	private final static int H_AudioManager = 1; //��ʼ��AudioManager
	private final static int H_SmallPop = 2; //����ͼ�궯̬��ʾ
	private final static int H_SmallPop_Hide = 3; //��������ͼ��
	private final static int H_ReadPort = 4; //��ʼ�������ڶ���
	private final static int H_startReadPortThread = 5; //������������
	private final static int H_stopReadPortThread = 6;	//�رն�������
	
	private final static int H_ReadPortData = 1111; //���ڷ�������
	private final static int H_Back2Luncher = 1112; //����launcher3
	
	private final static int H_SetLeft = 2001;
	private final static int H_SetRight = 2002;
	private final static int H_SetUp = 2003;
	private final static int H_SetDown = 2004;
	
	private final static int H_InstallAPK = 3001;
	
	private final static int H_Register = 4001; //ע�� mRegisterHandler��Ϣ
	private final static int H_Register_For_Debug = 4002;
	
	private SurfaceView mSurfaceView;
	private MediaPlayer mMediaPlayer;
	private SurfaceHolder mSurfaceHolder;
	
	private AudioManager mAudioManager;
	private int mMaxSound;
	private int mCurrentSound;
	private int mCurrentSound_50; //50�׶�Ӧ�ĵ�ǰ����ֵ
	/**�����Ƿ�ѭ�����ŵı��,true�ǣ�false����*/
	private boolean mIsRircle = true;
	
//	private String path_tmp = "/mnt/sdcard2/niceView_2d.mp4";
//	private String path_tmp = "/mnt/usb_storage/USB_DISK2/video/v01.mp4";
	/**����������Ƶ�ļ���·��*/
//	private String[] pathArr;
	/**ʵ���е���Ƶ����*/
	private int mRealVideoNum = 0; 
	
	private VideoFileScaner mVideoFileScaner;
	private ArrayList<String> mVideoPath;
	private ArrayList<String> mApkPath;
	private String mCurPath; //��¼��ǰ������Ƶ��·��
	
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
	
	private boolean mPauseflag = false; //��ͣ�����
	private boolean mKeyBack = false; //�ж�back���Ƿ��£�false��ʾδ����
	
	private Register mRegister;
	private int mRegsResult; //ע����
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private Layout mlayout;
	private final Timer mTimer = new Timer();
	private TimerTask mTimerTask;
	
	
	private boolean debug = false;
	private boolean show = false;
	
	private boolean hasNewApkflag = false;  //�Ƿ���newAPK�ı��
	private String newApkPath = null; //newAPK·��
	
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
			builder.setTitle("��ʾ");
			builder.setMessage("����APK,����ʱ��ʹ����ʽ�汾APK");
			builder.setCancelable(false);
			builder.create().show();
		}else {
			//mRegisterHandler.sendEmptyMessage(H_Register); //Ҫ��
		}
		
		//����SystemUI�㲥
		Intent intent=new Intent();
		intent.setAction("rk.android.phonestatusbar.hide");
		intent.putExtra("show", false);
		sendBroadcast(intent);

		//Log.v("yzh","tmp = " + tmp);
//		pathArr = new VideoPath().getAllVideo();
//		
//		for(String path: pathArr) { //��ȡʵ����Ƶ�ĸ�ʽ
//			if(path != null) {
//				mRealVideoNum ++;
//			}
//		}
		//�����Զ������ ������Ļ
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
		
		//ѭ�����USB���������Ƶ,Ŀ���Ǳ�֤����ʱ�ܹ���⵽��Ƶ�ļ�
		while(mScanNum-- >= 0) {
			tv_notice.setText(R.string.find_video);
			mVideoFileScaner.scanPathFile("/mnt/usb_storage"); 		//ɨ��U��������ļ�
//			mVideoFileScaner.scanPathFile("/storage/sdcard0");      //ɨ������SD��������ļ�
			mVideoPath = mVideoFileScaner.getVideoList();
			mApkPath = mVideoFileScaner.getApkList();
			mRealVideoNum = mVideoPath.size();
			if(mRealVideoNum == 0) {
				try {//�˶β���ɾ��,��֤����ʱ����������Ƶ�ļ�
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}else {
				break;
			}
		}
		
		//����Ƿ����µ�apk
		if(mApkPath.size() > 0) {
			for(int i=0; i<mApkPath.size();i++) {
				if(mApkPath.get(i).contains("VideoPL.apk")) {
					newApkPath = mApkPath.get(i); //��ȡnewAPK·��
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
		
//		{ //����ȫ��
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
	
	/**�����ƶ�·������Ƶ*/
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
	
	/**��ͣ��Ƶ����*/
	private void pause() {
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mPauseflag = true;
			Log.v("yzh","mMediaPlayer.pause()");
		}
	}
	
	/**ȡ����ͣ*/
	private void unpause() {
		if(mMediaPlayer != null && mPauseflag) {
			mMediaPlayer.start();
			mPauseflag = false;
			Log.v("yzh","mMediaPlayer.unpause()");
		}
	}
	
	/**�л�����һ����Ƶ����*/
	private void playNext() {
		int index=0;
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		index = mVideoPath.indexOf(mCurPath); //�ҵ����ڲ�����Ƶ���±�
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
	
	/**�л�����һ����Ƶ*/
	private void playPrev() {
		int index=0;
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		index = mVideoPath.indexOf(mCurPath); //�ҵ����ڲ�����Ƶ���±�
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
	
	
	/**�л����Զ�·����Ƶ����*/
	private void playPath(String str) {
		if(mMediaPlayer != null) {
			mMediaPlayer.stop();
		}
		play(str);
	}
	
	/**��MediaPlayer���ø��ּ�����*/
	private void setup() {
		/**����׼�ú�ļ�����*/
		mMediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mMediaPlayer.start();
//				Log.d("yzh","mMediaPlayer.start " + (System.currentTimeMillis()-time00));
				//Toast.makeText(MainActivity.this, "time = " + (System.currentTimeMillis()-time00), Toast.LENGTH_LONG).show();
			}
		});
		/**������Ƶ������ɵļ�����*/
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.v("yzh","onCompletion");
				if(mIsRircle){ //������ѭ�����ţ�������ɺ�ѭ��������һ��
//					playNext();
//					playPath(pathArr[0]);
					
					byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));//��ȡ��ǰ������Ƶ���±�
					
					playPath(mVideoPath.get(0));
					
					writePort((byte)0x04, curVideoIndex, (byte)0x00);
				}
			}
		});
		/**���ó���ļ�����*/
		mMediaPlayer.setOnErrorListener(errorListener);
	}
	
	/**��Ƶ��������*/
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
	
	//����
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
		 /* ���Դ�
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
	/**���ؼ����ü�����*/
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
	
	byte tmp_cmp[] =new byte[]{-42,-74,-106,-118,86,54,22}; //��ʱָ��
	
	Handler handler =  new Handler() {
		private int vol;
		byte readByte = 0;
		long time0  = 0;
		Command cmd = null;
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case H_AudioManager: //��ʼ������������ȡ�������ֵ�͵�ǰ����ֵ
				handler.removeMessages(H_AudioManager);
				mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
				mMaxSound = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			    mCurrentSound = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			    Log.v("yzh","Sound Max/Curr "+mMaxSound+"/"+mCurrentSound);
				break;
			case H_SmallPop: //����ͼ�궯̬��ʾ
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
			case H_SmallPop_Hide://����ͼ�궯̬��ʧ
				/*
				handler.removeMessages(H_SmallPop_Hide);
				if(mSmallPopWindow.isShowing()) {
					mSmallPopWindow.dismiss();
				}
				// */
				break;
			case H_ReadPort: //��ʼ�������ڶ���
				handler.removeMessages(H_ReadPort);
				mReadPort = new ReadPort(handler);
				handler.sendEmptyMessageDelayed(H_startReadPortThread,5000);
				break;
			case H_startReadPortThread: //������������
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
			case H_ReadPortData: //��ȡ�����������
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
					Toast.makeText(MainActivity.this,"û�ж�Ӧ����" + "���յ�ָ����:" + readByte,Toast.LENGTH_SHORT).show();
					break;
				}*/
				break;
			case H_Back2Luncher: //����launcher3
				ComponentName comp = new ComponentName("com.android.launcher3", "com.android.launcher3.Launcher");
				Intent intent = new Intent();
				intent.setComponent(comp);
				MainActivity.this.startActivity(intent);
				Log.d("yzh","back to Launcher3");
				break;
			//-----------������Ļ------------------- begin
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
				//-----------������Ļ------------------- end
			
			case H_InstallAPK: //��װ��apk
				ApkUtil.myInstall(MainActivity.this, newApkPath);
				Log.d("yzh","H_InstallAPK");
				break;
			default:
				break;
			}
		};
	};
	
	/**���������*/
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
			case MediaPlayer.MEDIA_ERROR_UNKNOWN:// δ֪����
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
	/**����������*/
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
		//GpioTest.writeGpio('1'); //дGPIOʵ��HDMI���
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
			case H_Register_For_Debug: //����ʱʹ�õ���ʾ��
				Log.d("yzh","register H_Register_For_Debug");
				register_debug();
				break;
			default:
				break;
			}
		};
	};
	
	/**д��������*/
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
	/**��Ƭ���������ݴ������*/
	public boolean processCmd(Command cmd){
		//Toast.makeText(MainActivity.this,"���յ�ָ����:" + Command.toString(cmd),Toast.LENGTH_SHORT).show();
		if(cmd == null || cmd.getBegin() != (byte)0x55 || cmd.getEnd() != (byte)0xAA){
			return false;			
		}
		Log.d("yzh","cmd =" + Command.toString(cmd));
		if(cmd.getCmd() == (byte)0X01 ) { //0x01������Ƶָ��//>=0 && readByte < mRealVideoNum
			Log.d("yzh","+++");
			if(cmd.getPara1() >= 0 && cmd.getPara1() < mRealVideoNum) {
				Log.d("yzh","cmd.getPara1() = " + cmd.getPara1());
				play(mVideoPath.get(cmd.getPara1()));
				writePort(cmd.getCmd(), (byte)0x00, (byte)0x00);
			}
			return true;
		}else if(cmd.getCmd() == (byte)0X02) {
			//0x02��ȡ��ǰ���ŵ���Ƶ״̬ 
			if(mPauseflag){ 
				byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));
				writePort(cmd.getCmd(), (byte)0x01, curVideoIndex);
			}else{
				byte curVideoIndex = (byte)(mVideoPath.indexOf(mCurPath));
				writePort(cmd.getCmd(), (byte)0x02, curVideoIndex);
			}
			return true;
		
		}else if(cmd.getCmd() == (byte)0X03) { /******��Ƶ����*********/
			
			if(cmd.getPara1() == (byte)0X01) { //01���� 0x02��ͣ 0x03��һ�� 0x04��һ��
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
			//0x04��Ŀ���Ž��� ��4K���Ű���������
			return true;
		
		}else if(cmd.getCmd() == (byte)0X05) {  //0x05 ��ȡ��Ƶ�ܸ���,�͵�ǰ���ڲ��ŵ���Ƶ���
			int totalVideo = mRealVideoNum;
			int curVideo = mVideoPath.indexOf(mCurPath);
			writePort(cmd.getCmd(), (byte)totalVideo, (byte)curVideo);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X11) {
			//��������
			//���������Ϊָ��ֵ
			volumeCtl((int)cmd.getPara1());
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, cmd.getPara1(), (byte)0x00);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X12) { 
			//��ȡ��ǰ����ֵ
			writePort((byte)0x12, (byte)mCurrentSound_50, (byte)0x00);
			return true;
		
		}else if(cmd.getCmd() == (byte)0X13) {
			//����+
			volumeAdd();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X14) {
			//����-
			volumeSub();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X15) {
			//����
			mute();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
		}else if(cmd.getCmd() == (byte)0X16) {
			//ȡ������
			unmute();
			handler.sendEmptyMessage(H_SmallPop);
			writePort((byte)0x12, (byte)mCurrentSound, (byte)0x00);
			return true;
			
		}else if(cmd.getCmd() == (byte)0X21) { /********����ͨ��ѡ�񣬲�ִ��**************/
			
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
			Toast.makeText(MainActivity.this,"û�ж�Ӧ����" + "���յ�ָ����:" + Command.toString(cmd),Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	
	/**ע��*/
	private void register() {
		
		mSharedPreferences = getSharedPreferences("helloworld20150924",Context.MODE_PRIVATE);
		mEditor = mSharedPreferences.edit();
		 
//		mEditor.clear();
//		mEditor.commit();
		
		//ע����ʾ��
		LayoutInflater factory = LayoutInflater.from(this);
		View view = factory.inflate(R.layout.register_edit, null);
		
		Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog;
		final Button btn_ok = (Button)view.findViewById(R.id.btn_ok);
		final Button btn_cancel = (Button)view.findViewById(R.id.btn_cancel);;
		final EditText editText = (EditText)view.findViewById(R.id.editText1);
		Log.d("yzh","estar = " +mSharedPreferences.getInt("John_Nash", 523) );
		if(!(mSharedPreferences.getInt("John_Nash", 523) == 1928)) {  //û��ע���ʱ�� John_Nash = 1928 ��ʾ�Ѿ�ע�����
			Toast.makeText(this, "û��ע��", Toast.LENGTH_SHORT).show();
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
						editText.setText("���ܳ���");
						editText.setTextColor(Color.RED);
					}
					else if(mRegsResult == -3) {
						editText.setText(R.string.register_haveRegs);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == -2) {
						editText.setText(R.string.register_errorsdcard);
						editText.setTextColor(Color.RED);
					}else if(mRegsResult == -1) { //password ������
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
	
	/**������ʹ�õ���ʾ��*/
	private void register_debug(){
		//ע����ʾ��
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
