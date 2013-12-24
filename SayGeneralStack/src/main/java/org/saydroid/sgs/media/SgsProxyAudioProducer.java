/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
 *
 * Contact: Mamadou Diop <diopmamadou(at)saydroid(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.saydroid.sgs.media;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.sip.SgsAVSession;
import org.saydroid.sgs.utils.SgsConfigurationEntry;
import org.saydroid.tinyWRAP.ProxyAudioProducer;
import org.saydroid.tinyWRAP.ProxyAudioProducerCallback;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * MyProxyAudioProducer
 */
public class SgsProxyAudioProducer extends SgsProxyPlugin {
	private static final String TAG = SgsProxyAudioProducer.class
			.getCanonicalName();
	private final static float AUDIO_BUFFER_FACTOR = 2.f;
	@SuppressWarnings("unused")
	private final static int AUDIO_MIN_VALID_BUFFER_SIZE = 4096;
	@SuppressWarnings("unused")
	private final static int AUDIO_DEFAULT_BUFFER_SIZE = 6200;

	private final MyProxyAudioProducerCallback mCallback;
	private final ProxyAudioProducer mProducer;
	private boolean mRoutingChanged;
	private boolean mOnMute;
	private boolean mHasBuiltInAEC;

	private Thread mProducerThread;
	private AudioRecord mAudioRecord;
	private ByteBuffer mAudioFrame;
	private int mPtime, mRate, mChannels;

	public SgsProxyAudioProducer(BigInteger id, ProxyAudioProducer producer) {
		super(id, producer);
		mProducer = producer;
		mCallback = new MyProxyAudioProducerCallback(this);
		mProducer.setCallback(mCallback);
		mOnMute = false;
		mHasBuiltInAEC = false;
	}

	public void setOnPause(boolean pause) {
		if (super.mPaused == pause) {
			return;
		}
		try {
			if (this.mStarted) {

			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}

		super.mPaused = pause;
	}

	public void setOnMute(boolean mute) {
		mOnMute = mute;
	}

	public boolean isOnMute() {
		return mOnMute;
	}

	public void setSpeakerphoneOn(boolean speakerOn) {
		Log.d(TAG, "setSpeakerphoneOn(" + speakerOn + ")");
		if (SgsApplication.isAudioRecreateRequired()) {
			if (super.mPrepared) {
				mRoutingChanged = true;
			}
		}
	}

	public void toggleSpeakerphone() {
		setSpeakerphoneOn(!SgsApplication.getAudioManager().isSpeakerphoneOn());
	}

	public boolean onVolumeChanged(boolean bDown) {
		return true;
	}

	private int prepareCallback(int ptime, int rate, int channels) {
		Log.d(SgsProxyAudioProducer.TAG, "prepareCallback(" + ptime + ","
				+ rate + "," + channels + ")");
		return prepare(ptime, rate, channels);
	}

	private int startCallback() {
		Log.d(SgsProxyAudioProducer.TAG, "startCallback");
		if (mPrepared && mAudioRecord != null) {
			super.mStarted = true;
			mProducerThread = new Thread(mRunnableRecorder,
					"AudioProducerThread");
			// mProducerThread.setPriority(Thread.MAX_PRIORITY);
			mProducerThread.start();
			return 0;
		}
		return -1;
	}

	private int pauseCallback() {
		Log.d(SgsProxyAudioProducer.TAG, "pauseCallback");
		setOnPause(true);
		return 0;
	}

	private int stopCallback() {
		Log.d(SgsProxyAudioProducer.TAG, "stopCallback");
		super.mStarted = false;
		if (mProducerThread != null) {
			try {
				mProducerThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			mProducerThread = null;
		}
		return -1;
	}

	private synchronized int prepare(int ptime, int rate, int channels) {
		if (super.mPrepared) {
			Log.e(TAG, "already prepared");
			return -1;
		}
		final boolean aecEnabled = SgsEngine.getInstance()
				.getConfigurationService().getBoolean(
						SgsConfigurationEntry.GENERAL_AEC,
						SgsConfigurationEntry.DEFAULT_GENERAL_AEC);

		final int minBufferSize = AudioRecord.getMinBufferSize(rate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		final int shortsPerNotif = (rate * ptime) / 1000;
		// AEC won't work if there is too much delay
		// Too short bufferSize will produce BufferOverflow errors but we don't
		// have choice if we want AEC
		final float bufferFactor = aecEnabled ? SgsProxyAudioProducer.AUDIO_BUFFER_FACTOR
				: SgsProxyAudioProducer.AUDIO_BUFFER_FACTOR;
		final int bufferSize = Math.max(
				(int) ((float) minBufferSize * bufferFactor),
				shortsPerNotif << 1);

		mAudioFrame = ByteBuffer.allocateDirect(shortsPerNotif << 1);
		mPtime = ptime;
		mRate = rate;
		mChannels = channels;
		Log.d(TAG, "Configure aecEnabled:" + aecEnabled);
		int audioSrc = MediaRecorder.AudioSource.MIC;
		if (aecEnabled) {
			audioSrc = MediaRecorder.AudioSource.VOICE_RECOGNITION;
			// Do not use built-in AEC
			/*
			 * if(SgsApplication.getSDKVersion() >= 11){ try { final Field f =
			 * MediaRecorder
			 * .AudioSource.class.getDeclaredField("VOICE_COMMUNICATION");
			 * audioSrc = f.getInt(null); mHasBuiltInAEC = true; } catch
			 * (Exception e) { e.printStackTrace(); } }
			 */
		}
		mAudioRecord = new AudioRecord(audioSrc, rate,
				AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
				bufferSize);

		if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
			super.mPrepared = true;
			return 0;
		} else {
			Log.e(TAG, "prepare(" + mAudioRecord.getState() + ") failed");
			super.mPrepared = false;
			return -1;
		}
	}

	private synchronized void unprepare() {
		if (mAudioRecord != null) {
			synchronized (mAudioRecord) {
				if (super.mPrepared) { // only call stop() is the AudioRecord is
										// in initialized state
					mAudioRecord.stop();
				}
				mAudioRecord.release();
				mAudioRecord = null;
			}
		}
		super.mPrepared = false;
	}

	private Runnable mRunnableRecorder = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "===== Audio Recorder (Start) ===== ");
			android.os.Process
					.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

			mAudioRecord.startRecording();
			final int nSize = mAudioFrame.capacity();
			byte silenceBuffer[] = new byte[nSize];
			int nRead;

			if (SgsProxyAudioProducer.super.mValid) {
				mProducer.setPushBuffer(mAudioFrame, mAudioFrame.capacity());
				mProducer
						.setGain(SgsEngine
								.getInstance()
								.getConfigurationService()
								.getInt(
										SgsConfigurationEntry.MEDIA_AUDIO_PRODUCER_GAIN,
										SgsConfigurationEntry.DEFAULT_MEDIA_AUDIO_PRODUCER_GAIN));
			}
			// disable Doubango AEC
			if (mHasBuiltInAEC) {
				final SgsAVSession sgsAVSession = SgsAVSession
						.getSession(getSipSessionId());
				if (sgsAVSession != null) {
					sgsAVSession.setAECEnabled(false);
				}
			}

			while (SgsProxyAudioProducer.super.mValid && mStarted) {
				if (mAudioRecord == null) {
					break;
				}
				if (mRoutingChanged) {
					Log.d(TAG, "Routing changed: restart() recorder");
					mRoutingChanged = false;
					unprepare();
					if (prepare(mPtime, mRate, mChannels) != 0) {
						break;
					}
					if (!SgsProxyAudioProducer.super.mPaused) {
						mAudioRecord.startRecording();
					}
				}

				// To avoid overrun read data even if on pause/mute we have to
				// read
				if ((nRead = mAudioRecord.read(mAudioFrame, nSize)) > 0) {
					if (!SgsProxyAudioProducer.super.mPaused) {
						if (mOnMute) { // workaround because Android's
										// SetMicrophoneOnMute() is buggy
							mAudioFrame.put(silenceBuffer);
							mProducer.push(mAudioFrame, silenceBuffer.length);
							mAudioFrame.rewind();
						} else {
							if (nRead != nSize) {
								mProducer.push(mAudioFrame, nRead);
								Log.w(TAG, "BufferOverflow?");
							} else {
								mProducer.push();
							}
						}
					}
				}
			}

			unprepare();

			Log.d(TAG, "===== Audio Recorder (Stop) ===== ");
		}
	};

	static class MyProxyAudioProducerCallback extends
			ProxyAudioProducerCallback {
		final SgsProxyAudioProducer myProducer;

		public MyProxyAudioProducerCallback(SgsProxyAudioProducer producer) {
			super();
			myProducer = producer;
		}

		@Override
		public int prepare(int ptime, int rate, int channels) {
			return myProducer.prepareCallback(ptime, rate, channels);
		}

		@Override
		public int start() {
			return myProducer.startCallback();
		}

		@Override
		public int pause() {
			return myProducer.pauseCallback();
		}

		@Override
		public int stop() {
			return myProducer.stopCallback();
		}
	}
}
