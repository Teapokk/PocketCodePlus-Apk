package com.example.model

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object SoundPlayer {
  private val scope = CoroutineScope(Dispatchers.Default)

  fun playSound(name: String) {
    scope.launch {
      try {
        when (name.lowercase()) {
          "jump" -> playJumpSound()
          "laser" -> playLaserSound()
          "coin" -> playCoinSound()
          "pop" -> playPopSound()
          "boom", "explosion" -> playExplosionSound()
          "win", "fanfare" -> playWinSound()
          "beep" -> playTone(523f, 150)
          else -> playTone(440f, 120)
        }
      } catch (_: Exception) {}
    }
  }

  fun playTone(freqHz: Float, durationMs: Int) {
    val sampleRate = 22050
    val numSamples = (durationMs * sampleRate / 1000).coerceAtLeast(1)
    val buffer = ShortArray(numSamples)
    val angularFreq = 2.0 * PI * freqHz / sampleRate

    for (i in 0 until numSamples) {
      // Apply smooth envelope fade-in / fade-out
      val envelope = when {
        i < numSamples * 0.1 -> i / (numSamples * 0.1)
        i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
        else -> 1.0
      }
      val sample = sin(angularFreq * i) * envelope * 0.8
      buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playJumpSound() {
    val sampleRate = 22050
    val durationMs = 180
    val numSamples = (durationMs * sampleRate / 1000)
    val buffer = ShortArray(numSamples)

    for (i in 0 until numSamples) {
      val progress = i.toFloat() / numSamples
      val freq = 260f + progress * 480f
      val sample = sin(2.0 * PI * freq * i / sampleRate) * (1f - progress * 0.5f)
      buffer[i] = (sample * Short.MAX_VALUE * 0.7f).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playLaserSound() {
    val sampleRate = 22050
    val durationMs = 150
    val numSamples = (durationMs * sampleRate / 1000)
    val buffer = ShortArray(numSamples)

    for (i in 0 until numSamples) {
      val progress = i.toFloat() / numSamples
      val freq = 880f * (1f - progress * 0.75f)
      val sample = sin(2.0 * PI * freq * i / sampleRate) * (1f - progress)
      buffer[i] = (sample * Short.MAX_VALUE * 0.7f).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playCoinSound() {
    val sampleRate = 22050
    val totalSamples = (200 * sampleRate / 1000)
    val half = totalSamples / 2
    val buffer = ShortArray(totalSamples)

    for (i in 0 until totalSamples) {
      val freq = if (i < half) 987.77f else 1318.51f
      val decay = 1f - (i.toFloat() / totalSamples)
      val sample = sin(2.0 * PI * freq * i / sampleRate) * decay
      buffer[i] = (sample * Short.MAX_VALUE * 0.65f).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playPopSound() {
    val sampleRate = 22050
    val durationMs = 60
    val numSamples = (durationMs * sampleRate / 1000)
    val buffer = ShortArray(numSamples)

    for (i in 0 until numSamples) {
      val progress = i.toFloat() / numSamples
      val freq = 600f - progress * 350f
      val sample = sin(2.0 * PI * freq * i / sampleRate) * (1f - progress)
      buffer[i] = (sample * Short.MAX_VALUE * 0.8f).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playExplosionSound() {
    val sampleRate = 22050
    val durationMs = 260
    val numSamples = (durationMs * sampleRate / 1000)
    val buffer = ShortArray(numSamples)
    var lastSample = 0f

    for (i in 0 until numSamples) {
      val progress = i.toFloat() / numSamples
      val whiteNoise = (Random.nextFloat() * 2f - 1f)
      // Low-pass filter noise for explosion rumble
      val filtered = lastSample * 0.85f + whiteNoise * 0.15f
      lastSample = filtered
      val sample = filtered * (1f - progress) * (1f - progress)
      buffer[i] = (sample * Short.MAX_VALUE * 0.9f).toInt().toShort()
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playWinSound() {
    val notes = listOf(523.25f, 659.25f, 783.99f, 1046.50f)
    val sampleRate = 22050
    val noteSamples = (90 * sampleRate / 1000)
    val totalSamples = noteSamples * notes.size
    val buffer = ShortArray(totalSamples)

    for (n in notes.indices) {
      val freq = notes[n]
      for (i in 0 until noteSamples) {
        val sampleIdx = n * noteSamples + i
        val decay = 1f - (i.toFloat() / noteSamples * 0.3f)
        val sample = sin(2.0 * PI * freq * i / sampleRate) * decay
        buffer[sampleIdx] = (sample * Short.MAX_VALUE * 0.65f).toInt().toShort()
      }
    }
    playRawPcm(buffer, sampleRate)
  }

  private fun playRawPcm(buffer: ShortArray, sampleRate: Int) {
    try {
      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
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
        .setBufferSizeInBytes(buffer.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(buffer, 0, buffer.size)
      audioTrack.play()
    } catch (_: Exception) {}
  }
}
