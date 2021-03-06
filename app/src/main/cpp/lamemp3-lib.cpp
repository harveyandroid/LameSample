#include <jni.h>
#include <string>
#include "lamemp3-lib.h"
#include "lame.h"
#include "lame_global_flags.h"

lame_global_flags *currentGfp;

/*
 * Class:     com_avatarcn_lame_AvatarLameMp3
 * Method:    getVersion
 * Signature: ()Ljava/lang/String;
 */
extern "C"
JNIEXPORT jstring JNICALL Java_com_avatarcn_lame_AvatarLameMp3_getVersion(
        JNIEnv *env,
        jobject /* this */) {
    const char *version = get_lame_version();
    return env->NewStringUTF(version);
}

/*
 * Class:     com_avatarcn_lame_AvatarLameMp3
 * Method:    init
 * Signature: (IIIII)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_com_avatarcn_lame_AvatarLameMp3_init
        (JNIEnv *, jobject, jint inSampleRate, jint numChannel, jint outSampleRate, jint outBitrate,
         jint quality) {
    currentGfp = lame_init();
    currentGfp->samplerate_in = inSampleRate;
    currentGfp->num_channels = numChannel;
    currentGfp->samplerate_out = outSampleRate;
    currentGfp->brate = outBitrate;
    currentGfp->quality = quality;
    return lame_init_params(currentGfp);
}

/*
 * Class:     com_avatarcn_lame_AvatarLameMp3
 * Method:    encode
 * Signature: ([S[SI[B)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_com_avatarcn_lame_AvatarLameMp3_encode
        (JNIEnv *env, jobject, jshortArray buffer_l, jshortArray buffer_r, jint samples,
         jbyteArray buffer_mp3) {
    jshort *lBuffer = env->GetShortArrayElements(buffer_l, NULL);
    jshort *rBuffer = env->GetShortArrayElements(buffer_r, NULL);
    jbyte *mp3Buffer = env->GetByteArrayElements(buffer_mp3, NULL);
    unsigned char *mp3Char = (unsigned char *) mp3Buffer;
    int chars_len = env->GetArrayLength(buffer_mp3);
    jint result = lame_encode_buffer(currentGfp, lBuffer, rBuffer, samples, mp3Char, chars_len);
    env->ReleaseShortArrayElements(buffer_l, lBuffer, 0);
    env->ReleaseShortArrayElements(buffer_r, rBuffer, 0);
    env->ReleaseByteArrayElements(buffer_mp3, mp3Buffer, 0);
    return result;
}

/*
 * Class:     com_avatarcn_lame_AvatarLameMp3
 * Method:    flush
 * Signature: ([B)I
 */
extern "C"
JNIEXPORT jint JNICALL Java_com_avatarcn_lame_AvatarLameMp3_flush
        (JNIEnv *env, jobject, jbyteArray mp3buf) {
    jbyte *bBuffer = env->GetByteArrayElements(mp3buf, NULL);
    unsigned char *buf = (unsigned char *) bBuffer;
    int chars_len = env->GetArrayLength(mp3buf);
    jint result = lame_encode_flush(currentGfp, buf, chars_len);
    env->ReleaseByteArrayElements(mp3buf, bBuffer, 0);
    return result;
}


/*
 * Class:     com_avatarcn_lame_AvatarLameMp3
 * Method:    close
 * Signature: ()I
 */
extern "C"
JNIEXPORT jint JNICALL Java_com_avatarcn_lame_AvatarLameMp3_close
        (JNIEnv *, jobject) {
    return lame_close(currentGfp);
}