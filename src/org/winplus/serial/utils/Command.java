package org.winplus.serial.utils;

import android.util.Log;

public class Command {
	public final static int CMD_LEN = 6; //指令长度
	private byte begin;
	private byte cmd;
	private byte para1;
	private byte para2;
	private byte checksum;
	private byte end;
	
	public boolean setCmd(byte[] b_array){
		if(b_array.length >= CMD_LEN){
			this.begin = b_array[0];
			this.cmd = b_array[1];
			this.para1 = b_array[2];
			this.para2 = b_array[3];
			this.checksum = b_array[4];
			this.end = b_array[5];
			Log.d("yzh","****************");
			return true;
		}
		Log.d("yzh","&&&&&&&&&&&&&&&&");
		return false;
	}
	
	/**将字节转化为16进制算法*/
	public static final String toHex(byte b) {
		  return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}
	
	public static String toString(Command comm){
		if(comm == null){
			return "null";
		}
		return ""+toHex(comm.getBegin())+"_"+toHex(comm.cmd)+"_"+toHex(comm.getPara1())
			   +"_"+toHex(comm.getPara2())+"_"+toHex(comm.getChecksum())+"_"+toHex(comm.getEnd());
	}
	public byte getBegin() {
		return begin;
	}
	public void setBegin(byte begin) {
		this.begin = begin;
	}
	public byte getCmd() {
		return cmd;
	}
	public void setCmd(byte cmd) {
		this.cmd = cmd;
	}
	public byte getPara1() {
		return para1;
	}
	public void setPara1(byte para1) {
		this.para1 = para1;
	}
	public byte getPara2() {
		return para2;
	}
	public void setPara2(byte para2) {
		this.para2 = para2;
	}
	public byte getChecksum() {
		return checksum;
	}
	public void setChecksum(byte checksum) {
		this.checksum = checksum;
	}
	public byte getEnd() {
		return end;
	}
	public void setEnd(byte end) {
		this.end = end;
	}
	
	
}
