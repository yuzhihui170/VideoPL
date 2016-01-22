package com.forrest.videopl;

public class VideoPath {
	/**保存所有视频的路径*/
	private String[] allVideo;
	VideoPath() {
		allVideo = new String[15];
//		allVideo[0] = "/mnt/sdcard/videos/niceView_2d.mp4";
//		allVideo[1] = "/mnt/sdcard/videos/error_111401 .flv";
//		allVideo[2] = "/mnt/sdcard/videos/error_111402.flv";
//		allVideo[3] = "/mnt/sdcard2/3d/mbsh1.mkv";
//		allVideo[4] = "/mnt/sdcard2/3d/god_4m.mkv";
//		
//		allVideo[0] = "/mnt/external_sd/Video/niceView_2d.mp4";
//		allVideo[1] = "/mnt/external_sd/Video/jiazhoulvguan_2d.avi";
//		allVideo[2] = "/mnt/external_sd/Video/niceView_2d.mp4";
//		allVideo[3] = "/mnt/external_sd/Video/qingwaxia_2d.avi";
//		allVideo[4] = "/mnt/external_sd/Video/oxds.flv";
		
		allVideo[0] = "/mnt/usb_storage/USB_DISK2/v00.mp4";
		allVideo[1] = "/mnt/usb_storage/USB_DISK2/v01.mp4";
		allVideo[2] = "/mnt/usb_storage/USB_DISK2/v02.mp4";
		allVideo[3] = "/mnt/usb_storage/USB_DISK2/v03.mp4";
		allVideo[4] = "/mnt/usb_storage/USB_DISK2/v04.mp4";
		allVideo[5] = "/mnt/usb_storage/USB_DISK2/v05.mp4";
		allVideo[6] = "/mnt/usb_storage/USB_DISK2/v06.mp4";
		allVideo[7] = "/mnt/usb_storage/USB_DISK2/v07.mp4";
		allVideo[8] = "/mnt/usb_storage/USB_DISK2/v08.mp4";
		allVideo[9] = "/mnt/usb_storage/USB_DISK2/v09.mp4";
		allVideo[10] = "/mnt/usb_storage/USB_DISK2/v10.mp4";
		allVideo[11] = "/mnt/usb_storage/USB_DISK2/v11.mp4";
		allVideo[12] = "/mnt/usb_storage/USB_DISK2/v12.mp4";
		allVideo[13] = "/mnt/usb_storage/USB_DISK2/v13.mp4";
		allVideo[14] = "/mnt/usb_storage/USB_DISK2/v14.mp4";
	}
	public String[] getAllVideo(){
		return allVideo;
	}
}
