package com.forrest.videopl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class VideoFileScaner {
	private final static String TAG = "SdcardFileScaner";
	//�ֱ�����Ƶ�ļ�·������
	private ArrayList<String> videoPathList = new ArrayList<String>();
	//�ֱ���APK�ļ�·������
	private ArrayList<String> apkPathList = new ArrayList<String>();

	private static final String rootPath = "/mnt";
	/**��һ��ֻɨ��ָ��Ŀ¼*/
//	private static final String firstPath = "/storage/sdcard0/3D��Ƶ";

	private static String[] videoTypes = {".mp4", ".3gp", ".avi", ".mpeg", ".flv", ".mkv", ".mov" ,".wmv", ".ts"};

	/** Ĭ�ϲ�ɨ���Ŀ¼ */
	private static String[] notScanFileName = {"tencent","QQ","taobao","Music","Movies","BaiduCloudBackup"};
	
	/**sdcard��Ŀ¼*/
	private static String[] sdcardDic = {"/storage/sdcard0","/storage/sdcard1"};
		
	public ArrayList<String> getVideoList() {
		return videoPathList;
	}
	
	public ArrayList<String> getApkList() {
		return apkPathList;
	}
	public void reset() {
		videoPathList.clear(); 
	}

	// ɨ��ָ��Ŀ¼�µ��ļ�ָ�����ļ�����
//	public void scanSDCardFile(String path, String type) {
//		reset();
//	}

	// ɨ��ָ��Ŀ¼�µ��ļ�ָ��
	public void scanPathFile(String path) {
		reset();
		scanFiles(path);
		VCompareAlgorithm compare = new VCompareAlgorithm();
		Collections.sort(videoPathList, compare);
	}

	// ɨ��ָ��·���µ��ļ�
	public synchronized void scanFiles(String Path) {
		File rootFile = new File(Path);
		/**��ʶ��ǰĿ¼�Ƿ�ɨ��*/
		boolean scanDic = true;
		File[] files = rootFile.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				scanDic = true;
				File file = files[i];
				if (file.isFile()) {
					chooseVideo(file);
					chooseAPK(file); //����APK
				}else if (file.isDirectory() && file.getPath().indexOf("/.") == -1){ // �����ų���Ŀ¼���ϲ�Ŀ¼   (ͬʱ�ų�����.��ͷ���ļ���Ŀ¼��ɨ��)
					//����Ǹ�Ŀ¼�� /mnt/sdcard ���� /mnt/sdcard2 �ͽ����ж�Ŀ¼���Ƿ��ڲ�ɨ��Ŀ¼֮��,
					if(file.getParent().equals(sdcardDic[0]) || file.getParent().equals(sdcardDic[1]) ) { 
						for(int j=0; j<notScanFileName.length; j++) {     //�ж�Ŀ¼�����ڲ�ɨ��Ŀ¼֮��
							if(notScanFileName[j].equals(file.getName())) {
								scanDic = false;
								Log.v(TAG,"not scan DicName = " + file.getPath());
								break;
							}
						}
					}
					if(scanDic) {
						scanFiles(file.getPath());
					}
				}
			}
		}
	}

	/**����Ƶ���浽������*/
	private void saveVideo(File file) {
		videoPathList.add(file.getPath());
	}

	/**�ж��Ƿ�����Ƶ�ļ������� */
	private synchronized void chooseVideo(File file) {
		if (file == null) {
			return;
		}
		String path = file.getPath().toLowerCase();
		for (String suffix : videoTypes) {
			if (path.endsWith(suffix) && chooseVideoName(path)) {
				saveVideo(file);
			}
		}
	}

	/**�ж���Ƶ�Ƿ���v0 ���� v1 V0 V1*/
	private synchronized boolean chooseVideoName(String videoName) {
		Log.d("yzh","videoName = " + videoName);
		char ch0;
		char ch1;
		int index = 0;
		index = videoName.lastIndexOf('/');
		if( (videoName.charAt(index+1) == '0' || videoName.charAt(index+1) == '1')) {
			return true;
		}else {
			return false;
		}
	}
	
	/**�ж��Ƿ���apk�ļ������� */
	private synchronized void chooseAPK(File file) {
		if (file == null) {
			return;
		}
		String path = file.getPath().toLowerCase();
			if (path.endsWith("apk")) {
				saveAPK(file);
		}
	}
	
	/**��APK���浽������*/
	private void saveAPK(File file) {
		apkPathList.add(file.getPath());
	}
	
	// ����ļ��Ƿ���Ȼ����
	public boolean fileExist(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			return true;
		return false;
	}
	

}