package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class DialUpSoundEngine {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  private var currentHandshakeJob: Job? = null

  @Volatile var isEnabled: Boolean = true

  fun playDialUpHandshake() {
    if (!isEnabled) return
    currentHandshakeJob?.cancel()
    currentHandshakeJob =
      scope.launch {
        val sampleRate = 22050
        val totalSamples = (sampleRate * 3.1f).toInt()
        val pcm = ShortArray(totalSamples)
        val rng = Random(19980706)

        val dtmfFreqs =
          arrayOf(
            697.0 to 1209.0,
            770.0 to 1336.0,
            852.0 to 1477.0,
            697.0 to 1477.0,
            941.0 to 1336.0,
            770.0 to 1209.0,
          )

        for (i in 0 until totalSamples) {
          val t = i.toDouble() / sampleRate
          val sample =
            when {
              t < 0.25 -> {
                // Dial tone (350Hz + 440Hz)
                (sin(2.0 * PI * 350.0 * t) + sin(2.0 * PI * 440.0 * t)) * 0.25
              }
              t < 0.90 -> {
                // DTMF dialing sequence
                val localT = t - 0.25
                val slot = (localT / 0.11).toInt().coerceIn(0, dtmfFreqs.lastIndex)
                val inSlot = localT % 0.11
                if (inSlot < 0.075) {
                  val (f1, f2) = dtmfFreqs[slot]
                  (sin(2.0 * PI * f1 * t) + sin(2.0 * PI * f2 * t)) * 0.28
                } else {
                  0.0
                }
              }
              t < 1.40 -> {
                // 2100Hz CED answer tone with subtle phase click
                val phaseFlip = if ((t * 4).toInt() % 2 == 0) 1.0 else -1.0
                sin(2.0 * PI * 2100.0 * t) * 0.30 * phaseFlip
              }
              t < 2.25 -> {
                // FSK negotiation warble
                val carrier =
                  when (((t * 14).toInt()) % 4) {
                    0 -> 980.0
                    1 -> 1650.0
                    2 -> 2400.0
                    else -> 1320.0
                  }
                val mod = sin(2.0 * PI * 18.0 * t) * 140.0
                (sin(2.0 * PI * (carrier + mod) * t) * 0.28) +
                  ((rng.nextDouble() * 2.0 - 1.0) * 0.08)
              }
              t < 2.75 -> {
                // V.90 QAM carrier hiss
                val fadeOut = (2.75 - t) / 0.50
                val hiss = (rng.nextDouble() * 2.0 - 1.0) * 0.24
                val undertone = sin(2.0 * PI * 1800.0 * t) * 0.10
                (hiss + undertone) * fadeOut.coerceIn(0.0, 1.0)
              }
              else -> {
                // Frutiger Aero connected chime (C5 -> E5 -> G5)
                val ct = t - 2.75
                val env = exp(-ct * 6.5)
                val f =
                  when {
                    ct < 0.10 -> 523.25
                    ct < 0.20 -> 659.25
                    else -> 783.99
                  }
                sin(2.0 * PI * f * t) * 0.35 * env
              }
            }
          pcm[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.65).toInt().toShort()
        }

        playPcmBuffer(pcm, sampleRate)
      }
  }

  fun playRetroClick() {
    if (!isEnabled) return
    scope.launch {
      val sampleRate = 22050
      val count = (sampleRate * 0.028f).toInt()
      val pcm = ShortArray(count)
      for (i in 0 until count) {
        val t = i.toDouble() / sampleRate
        val env = exp(-t * 120.0)
        val s = sin(2.0 * PI * (1500.0 - t * 20000.0) * t) * env * 0.45
        pcm[i] = (s.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
      }
      playPcmBuffer(pcm, sampleRate)
    }
  }

  fun playWindowOpen() {
    if (!isEnabled) return
    scope.launch {
      val sampleRate = 22050
      val count = (sampleRate * 0.22f).toInt()
      val pcm = ShortArray(count)
      for (i in 0 until count) {
        val t = i.toDouble() / sampleRate
        val env = exp(-t * 14.0)
        val f = if (t < 0.09) 587.33 else 880.0
        val s = sin(2.0 * PI * f * t) * env * 0.35
        pcm[i] = (s.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
      }
      playPcmBuffer(pcm, sampleRate)
    }
  }

  fun playWindowMinimize() {
    if (!isEnabled) return
    scope.launch {
      val sampleRate = 22050
      val count = (sampleRate * 0.15f).toInt()
      val pcm = ShortArray(count)
      for (i in 0 until count) {
        val t = i.toDouble() / sampleRate
        val env = exp(-t * 18.0)
        val f = 740.0 - t * 2600.0
        val s = sin(2.0 * PI * f * t) * env * 0.30
        pcm[i] = (s.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
      }
      playPcmBuffer(pcm, sampleRate)
    }
  }

  fun playCrtBlip() {
    if (!isEnabled) return
    scope.launch {
      val sampleRate = 22050
      val count = (sampleRate * 0.065f).toInt()
      val pcm = ShortArray(count)
      for (i in 0 until count) {
        val t = i.toDouble() / sampleRate
        val env = exp(-t * 45.0)
        val s = (sin(2.0 * PI * 1920.0 * t) * 0.25 + sin(2.0 * PI * 960.0 * t) * 0.15) * env
        pcm[i] = (s.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
      }
      playPcmBuffer(pcm, sampleRate)
    }
  }

  private fun playPcmBuffer(pcm: ShortArray, sampleRate: Int) {
    var track: AudioTrack? = null
    try {
      val minBuf =
        AudioTrack.getMinBufferSize(
          sampleRate,
          AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
        )
      val bufSize = maxOf(minBuf, pcm.size * 2)
      track =
        AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_MEDIA)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(sampleRate)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(bufSize)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

      track.write(pcm, 0, pcm.size)
      track.play()
      val durationMs = ((pcm.size.toDouble() / sampleRate) * 1000.0).toLong() + 40L
      Thread.sleep(durationMs)
    } catch (e: InterruptedException) {
      Thread.currentThread().interrupt()
    } catch (e: Exception) {
      Log.w(TAG, "Retro audio playback skipped: ${e.message}")
    } finally {
      try {
        track?.stop()
        track?.release()
      } catch (_: Exception) {}
    }
  }

  private companion object {
    const val TAG = "DialUpSoundEngine"
  }
}
