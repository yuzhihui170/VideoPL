#include <jni.h>
#include <string.h>
#include "mylog.h"


static const char *classPathName = "com/forrest/videopl/Encrypt";
int encrypt_jni(JNIEnv *env, jobject thiz, jstring plainFileJ, jstring keyStrJ, jstring cipherFileJ)
{
	int ret = 0;
	const char *plainFile = (*env)->GetStringUTFChars(env,plainFileJ, 0);
	const char *keyStr = (*env)->GetStringUTFChars(env,keyStrJ, 0);
	const char *cipherFile = (*env)->GetStringUTFChars(env,cipherFileJ, 0);

	if(plainFile!= NULL && keyStr != NULL && cipherFile != NULL) {
		LOGV("EN plainFile = %s", plainFile);
		LOGV("EN keyStr = %s", keyStr);
		LOGV("EN cipherFile = %s", cipherFile);
	}
	ret = DES_Encrypt(plainFile, keyStr,cipherFile); //¼ÓÃÜ
	(*env)->ReleaseStringUTFChars(env,plainFileJ, plainFile);
	(*env)->ReleaseStringUTFChars(env,keyStrJ, keyStr);
	(*env)->ReleaseStringUTFChars(env,cipherFileJ, cipherFile);
	return ret;
}

int decrypt_jni(JNIEnv *env, jobject thiz, jstring cipherFileJ, jstring keyStrJ, jstring plainFileJ)
{
	int ret = 1;
	const char *plainFile = (*env)->GetStringUTFChars(env,plainFileJ, 0);
	const char *keyStr = (*env)->GetStringUTFChars(env,keyStrJ, 0);
	const char *cipherFile = (*env)->GetStringUTFChars(env,cipherFileJ, 0);

	if(plainFile!= NULL && keyStr != NULL && cipherFile != NULL) {
			LOGV("DE cipherFile = %s", cipherFile);
			LOGV("DE keyStr = %s", keyStr);
			LOGV("DE plainFile = %s", plainFile);
	}
	ret = DES_Decrypt(cipherFile, keyStr, plainFile); //½âÃÜ
	(*env)->ReleaseStringUTFChars(env,plainFileJ, plainFile);
	(*env)->ReleaseStringUTFChars(env,keyStrJ, keyStr);
	(*env)->ReleaseStringUTFChars(env,cipherFileJ, cipherFile);
	return ret;
}

static JNINativeMethod g_methods[] = {
		{ "native_encrypt", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I",(void*) encrypt_jni },
		{ "native_decrypt","(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I", (void*) decrypt_jni },
		};

/* Register several native methods for one class. */
static int registerNativeMethods(JNIEnv* env, const char* className, JNINativeMethod* gMethods, int numMethods) {
	jclass clazz;
	clazz = (*env)->FindClass(env,className);
	if (clazz == NULL) {
		LOGV("Native registration unable to find class '%s'", className);
		return JNI_FALSE;
	}
	if ((*env)->RegisterNatives(env,clazz, gMethods, numMethods) < 0) {
		LOGV("RegisterNatives failed for '%s'", className);
		return JNI_FALSE;
	}
	return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	LOGV("JNI_OnLoad");

	if (JNI_OK != (*vm)->GetEnv(vm,(void **) &env, JNI_VERSION_1_4)) {
		LOGV("ERROR: GetEnv failed");
		goto bail;
	}

	if (!registerNativeMethods(env, classPathName, g_methods, sizeof(g_methods) / sizeof(g_methods[0]))) {
		LOGV("ERROR: registerNatives failed");
		goto bail;
	}

	result = JNI_VERSION_1_4;

	bail: return result;
}
