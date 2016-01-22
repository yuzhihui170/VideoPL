package com.forrest.videopl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class KeyFileScaner {
	private final static String TAG = "KeyFileScaner";

	/**������뱾�ľ���·��*/
	private String mkeyPath ;
	private static final String rootPath = "/mnt";
	/** Ĭ�ϲ�ɨ���Ŀ¼ */
	private static String[] notScanFileName = {"tencent","QQ","taobao","Music","Movies","BaiduCloudBackup"};
	
	/**sdcard��Ŀ¼*/
	private static String[] sdcardDic = {"/storage/sdcard0","/storage/sdcard1"};
	
	public String getKeyPath() {
		return mkeyPath;
	}

	public void reset() {
		mkeyPath = null; 
	}

	// ɨ��ָ��Ŀ¼�µ��ļ�ָ�����ļ�����
//	public void scanSDCardFile(String path, String type) {
//		reset();
//	}

	// ɨ��ָ��Ŀ¼�µ��ļ�ָ��,����ָ��name���ļ�
	public void scanPathFile(String path,String name) {
		reset();
		scanFiles(path,name);
	}

	// ɨ��ָ��·���µ��ļ�
	public synchronized void scanFiles(String Path,String name) {
		File rootFile = new File(Path);
		/**��ʶ��ǰĿ¼�Ƿ�ɨ��*/
		boolean scanDic = true;
		File[] files = rootFile.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				scanDic = true;
				File file = files[i];
				
				if (file.isFile()) {
					if(chooseVideo(file,name)) { //�ҵ���password���˳�ɨ��
						break;
					}
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
						scanFiles(file.getPath(),name);
					}
				}
			}
		}
	}

	/**����Ƶ���浽������*/
	private void saveVideo(File file) {
		mkeyPath = file.getPath();
	}

	/**�ж��Ƿ�����Ƶ�ļ������� */
	private synchronized boolean chooseVideo(File file,String name) {
		if (file == null) {
			return false;
		}
		String path = file.getPath().toLowerCase();
		if (path.contains(name)) {
			saveVideo(file);
			return true;
		}
		return false;
	}

	// ����ļ��Ƿ���Ȼ����
	public boolean fileExist(String filePath) {
		File file = new File(filePath);
		if (file.exists())
			return true;
		return false;
	}
	

}