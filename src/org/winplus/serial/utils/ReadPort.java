package org.winplus.serial.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ReadPort {
	
	private String path = "/dev/ttyS3";
	private int baudrate  = 115200;
	private SerialPort mSerialPort = null;
	protected OutputStream mOutputStream;
	private InputStream mInputStream = null;
	private ReadThread mReadThread = null;
	public boolean isRun = true;
	private Handler mHandler;
	
	private final static int H_ReadPortData = 1111;
	
	public ReadPort(Handler handler) {
		mHandler = handler;
		try {
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
		} catch (Exception e) {
			Log.e("yzh","can not creat SerialPort");
			e.printStackTrace();
		} 
		if(mSerialPort != null) {
			mReadThread = new ReadThread();
		}
	}
	
	/**线程开启*/
	public void startReadThread() {
		if(mReadThread != null) {
			Log.v("yzh","startReadThread");
			isRun = true;
			mReadThread.start();
		}
	}
	/**线程停止*/
	public void stopReadThread() {
		if(mReadThread != null) {
			Log.v("yzh","stopReadThread");
			isRun = false;
			mSerialPort.close();
		}
	}
	
	public OutputStream getOutputStream() {
		return mOutputStream;
	}
	
	/**将字节转化为16进制算法*/
	public static final String toHex(byte b) {
		  return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}
	
	private class ReadThread extends Thread {
		int size;
		
		int byte_len = 8;
		String str ="";
		byte readByte = 0;
		Command command = new Command();
		int readSum = 0;
		byte[] bufferSum = new byte[Command.CMD_LEN];
		@Override
		public void run() {
			while (isRun) {
				try {
					size = 0;
					byte[] buffer = new byte[Command.CMD_LEN*2];
//					byte[] ack_buf = new byte[Command.CMD_LEN];
					for(int i=0; i<Command.CMD_LEN; i++) {
						buffer[i] = 48;
//						ack_buf[1] = 0x55;
					}
					if (mInputStream == null) {
						return;
					}
					Log.v("yzh","+++++size = "+size);
					size = mInputStream.read(buffer);
					if(size == Command.CMD_LEN){ //每次应该读取6个字节长度的命令  == Command.CMD_LEN
						for(int i= 0;i<size;i++) {
							Log.d("yzh","SerialPort read data = " + buffer[i] + "---" + toHex(buffer[i]));
//							readByte = (byte)buffer[i];
						}
						command.setCmd(buffer);
						
						Log.d("yzh","command = "+ Command.toString(command));
//						mOutputStream.write(buffer, 0, size);
//						mOutputStream.write(ack_buf, 0, size);
						Log.v("yzh","-----size = "+size);
						Log.d("yzh","===========================================");

						Message msg ;
//						msg = mHandler.obtainMessage(H_ReadPortData,readByte);
						msg = mHandler.obtainMessage(H_ReadPortData,command);
						mHandler.sendMessage(msg);
						Thread.sleep(200);
						
					}else if(size > 0 && size < Command.CMD_LEN){
						if(readSum < Command.CMD_LEN) { //读取到的字节为6个时处理
							for(int i=0;i<size;i++) {
								bufferSum[readSum] = buffer[i];
								readSum++;
							}
						}
						if(readSum == Command.CMD_LEN){
							for(int i= 0;i<readSum;i++) {
								Log.d("yzh","SerialPort read data = " + bufferSum[i] + "---" + toHex(bufferSum[i]));
//								readByte = (byte)buffer[i];
							}
							Log.d("yzh","-----readSum = " + readSum);
							Log.d("yzh","-----------------------------------------");
							readSum = 0;
							command.setCmd(bufferSum);
							Message msg ;
							msg = mHandler.obtainMessage(H_ReadPortData,command);
							mHandler.sendMessage(msg);
							Thread.sleep(200);
						}

					}else {
						Log.d("yzh","SerialPort read data size = " + size);
					}

				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			Log.v("yzh", "Thread stop");
		}
	}
}
