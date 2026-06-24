package com.example.mindcare.platform

import android.media.MediaRecorder
import android.os.Build
import java.io.File

actual class AudioRecorder actual constructor() {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    actual fun start(): Boolean {
        return try {
            val context = ContextHolder.context
            val file = File(context.cacheDir, "voice_record.mp3")
            outputFile = file
            recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION") MediaRecorder()
            }).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            recorder = null
            false
        }
    }

    actual fun stop(): ByteArray? {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            outputFile?.readBytes()
        } catch (e: Exception) {
            recorder = null
            null
        }
    }

    actual fun cancel() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        recorder = null
    }

    actual fun getAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (e: Exception) {
        0
    }
}
