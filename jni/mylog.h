#ifndef __MYLOG_H__
#define __MYLOG_H__

#ifdef __cplusplus
extern "C" {
#endif

#include <android/log.h>
#ifndef TAG
#define TAG "yzh_jni"
#endif

#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG ,  TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO ,   TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN ,   TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR ,  TAG, __VA_ARGS__)

#define LOGV_FL(x, ...) LOGV("func:%s,line:%d, "x, __FUNCTION__,__LINE__, ##__VA_ARGS__)
#define LOGD_FL(x, ...) LOGD("func:%s,line:%d, "x, __FUNCTION__,__LINE__, ##__VA_ARGS__)
#define LOGI_FL(x, ...) LOGI("func:%s,line:%d, "x, __FUNCTION__,__LINE__, ##__VA_ARGS__)
#define LOGW_FL(x, ...) LOGW("func:%s,line:%d, "x, __FUNCTION__,__LINE__, ##__VA_ARGS__)
#define LOGE_FL(x, ...) LOGE("func:%s,line:%d, "x, __FUNCTION__,__LINE__, ##__VA_ARGS__)

#ifdef __cplusplus
}
#endif

#endif
