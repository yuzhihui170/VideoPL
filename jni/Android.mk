#libencrypt.so
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

#½â¾ö ndk±àÒë¾¯¸æ:note: the mangling of 'va_list' has changed in GCC 4.4
LOCAL_CFLAGS = -Wno-psabi

LOCAL_SRC_FILES := encrypt.c encrypt_jni.c

#LOCAL_C_INCLUDES += 
        
LOCAL_LDLIBS := -llog -landroid 
 
LOCAL_MODULE := encrypt
include $(BUILD_SHARED_LIBRARY)