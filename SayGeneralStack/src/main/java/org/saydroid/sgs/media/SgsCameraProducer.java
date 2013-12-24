/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.saydroid.sgs.SgsApplication;
import org.saydroid.sgs.SgsEngine;
import org.saydroid.sgs.utils.SgsConfigurationEntry;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public class SgsCameraProducer {
	private static final String TAG = SgsCameraProducer.class.getCanonicalName();
	private static Camera instance;
	private static boolean useFrontFacingCamera;
	
	// Default values
	private static int fps = 15;
	private static int width = 176;
	private static int height = 144;
	private static SurfaceHolder holder = null;
	private static PreviewCallback callback = null;
	
	private static final int MIN_SDKVERSION_addCallbackBuffer = 7;
	private static final int MIN_SDKVERSION_setPreviewCallbackWithBuffer = 7;
	private static final int MIN_SDKVERSION_setDisplayOrientation = 8;
	//private static final int MIN_SDKVERSION_getSupportedPreviewSizes = 5;
	
	private static Method addCallbackBufferMethod = null;
	private static Method setDisplayOrientationMethod = null;
	private static Method setPreviewCallbackWithBufferMethod = null;
	
	static{
		SgsCameraProducer.useFrontFacingCamera = SgsEngine
				.getInstance().getConfigurationService().getBoolean(SgsConfigurationEntry.GENERAL_USE_FFC,
						SgsConfigurationEntry.DEFAULT_GENERAL_USE_FFC);
	}
	
	static{
		if(SgsApplication.getSDKVersion() >= SgsCameraProducer.MIN_SDKVERSION_addCallbackBuffer){
			// According to http://developer.android.com/reference/android/hardware/Camera.html both addCallbackBuffer and setPreviewCallbackWithBuffer
			// are only available starting API level 8. But it's not true as these functions exist in API level 7 but are hidden.
			try {
				SgsCameraProducer.addCallbackBufferMethod = Camera.class.getMethod("addCallbackBuffer", byte[].class);
			} catch (Exception e) {
				Log.e(SgsCameraProducer.TAG, e.toString());
			} 
		}
		
		if(SgsApplication.getSDKVersion() >= SgsCameraProducer.MIN_SDKVERSION_setPreviewCallbackWithBuffer){
			try {
				SgsCameraProducer.setPreviewCallbackWithBufferMethod = Camera.class.getMethod(
					"setPreviewCallbackWithBuffer", PreviewCallback.class);
			}  catch (Exception e) {
				Log.e(SgsCameraProducer.TAG, e.toString());
			}
		}
				
		if(SgsApplication.getSDKVersion() >= SgsCameraProducer.MIN_SDKVERSION_setDisplayOrientation){
			try {
				SgsCameraProducer.setDisplayOrientationMethod = Camera.class.getMethod("setDisplayOrientation", int.class);
			} catch (Exception e) {
				Log.e(SgsCameraProducer.TAG, e.toString());
			} 
		}
	}
	
	public static Camera getCamera(){
		return SgsCameraProducer.instance;
	}
	
	public static void test()
	{
	
	}
	public static SurfaceHolder getHolder()
	{
		return SgsCameraProducer.holder;
	}
	
	public static PreviewCallback getPreviewCallback()
	{
		return SgsCameraProducer.callback;
	}
	
	public static Camera openCamera(int fps, int width, int height, SurfaceHolder holder, PreviewCallback callback){
		if(SgsCameraProducer.instance == null){
			try{
				if(SgsCameraProducer.useFrontFacingCamera){
					SgsCameraProducer.instance = SgsCameraProducer.openFrontFacingCamera();
				}
				else{
					SgsCameraProducer.instance = Camera.open();
				}
				
				SgsCameraProducer.fps = fps;
				SgsCameraProducer.width = width;
				SgsCameraProducer.height = height;
				SgsCameraProducer.holder = holder;
				SgsCameraProducer.callback = callback;
				
				Camera.Parameters parameters = SgsCameraProducer.instance.getParameters();
				
				/*
				 * http://developer.android.com/reference/android/graphics/ImageFormat.html#NV21
				 * YCrCb format used for images, which uses the NV21 encoding format. 
				 * This is the default format for camera preview images, when not otherwise set with setPreviewFormat(int). 
				 */
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);
				parameters.setPreviewFrameRate(SgsCameraProducer.fps);
				SgsCameraProducer.instance.setParameters(parameters);
				
				try{
					parameters.setPictureSize(SgsCameraProducer.width , SgsCameraProducer.height);
					SgsCameraProducer.instance.setParameters(parameters);
				}
				catch(Exception e){
					// FFMpeg converter will resize the video stream
					Log.d(SgsCameraProducer.TAG, e.toString());
				}
				
				SgsCameraProducer.instance.setPreviewDisplay(SgsCameraProducer.holder);
				SgsCameraProducer.initializeCallbacks(SgsCameraProducer.callback);
			}
			catch(Exception e){
				SgsCameraProducer.releaseCamera();
				Log.e(SgsCameraProducer.TAG, e.toString());
			}
		}
		return SgsCameraProducer.instance;
	}
	
	public static void releaseCamera(Camera camera){
		if(camera != null){
			camera.stopPreview();
			SgsCameraProducer.deInitializeCallbacks(camera);
			camera.release();
			if(camera == SgsCameraProducer.instance){
				SgsCameraProducer.instance = null;
			}
		}
	}
	
	public static void releaseCamera(){
		if(SgsCameraProducer.instance != null){
			SgsCameraProducer.instance.stopPreview();
			SgsCameraProducer.deInitializeCallbacks();
			SgsCameraProducer.instance.release();
			SgsCameraProducer.instance = null;
		}
	}
	
	public static void setDisplayOrientation(int degrees){
		if(SgsCameraProducer.instance != null && SgsCameraProducer.setDisplayOrientationMethod != null){
			try {
				SgsCameraProducer.setDisplayOrientationMethod.invoke(SgsCameraProducer.instance, degrees);
			} catch (Exception e) {
				Log.e(SgsCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void setDisplayOrientation(Camera camera, int degrees){
		if(camera != null && SgsCameraProducer.setDisplayOrientationMethod != null){
			try {
				SgsCameraProducer.setDisplayOrientationMethod.invoke(camera, degrees);
			} catch (Exception e) {
				Log.e(SgsCameraProducer.TAG, e.toString());
			}
		}
	}
	
	public static void addCallbackBuffer(Camera camera, byte[] buffer) {
		try {
			SgsCameraProducer.addCallbackBufferMethod.invoke(camera, buffer);
		} catch (Exception e) {
			Log.e(SgsCameraProducer.TAG, e.toString());
		}
	}
	
	public static void addCallbackBuffer(byte[] buffer) {
		try {
			SgsCameraProducer.addCallbackBufferMethod.invoke(SgsCameraProducer.instance, buffer);
		} catch (Exception e) {
			Log.e(SgsCameraProducer.TAG, e.toString());
		}
	}

	public static boolean isAddCallbackBufferSupported(){
		return SgsCameraProducer.addCallbackBufferMethod != null;
	}
	
	public static boolean isFrontFacingCameraEnabled(){
		return SgsCameraProducer.useFrontFacingCamera;
	}
	
	public static void useRearCamera(){
		SgsCameraProducer.useFrontFacingCamera = false;
	}
	
	public static void useFrontFacingCamera(){
		SgsCameraProducer.useFrontFacingCamera = true;
	}
	
	public static Camera toggleCamera(){
		if(SgsCameraProducer.instance != null){
			SgsCameraProducer.useFrontFacingCamera = !SgsCameraProducer.useFrontFacingCamera;
			SgsCameraProducer.releaseCamera();
			SgsCameraProducer.openCamera(SgsCameraProducer.fps, 
					SgsCameraProducer.width, 
					SgsCameraProducer.height,
					SgsCameraProducer.holder, 
					SgsCameraProducer.callback);
		}
		return SgsCameraProducer.instance;
	}
	
	public static Camera changeFrameRate(int fps)
	{
		if(SgsCameraProducer.instance != null){
			//SgsCameraProducer.useFrontFacingCamera = !SgsCameraProducer.useFrontFacingCamera;
			SgsCameraProducer.releaseCamera();
			SgsCameraProducer.openCamera(fps, 
					SgsCameraProducer.width, 
					SgsCameraProducer.height,
					SgsCameraProducer.holder, 
					SgsCameraProducer.callback);
		}
		return SgsCameraProducer.instance;
	}
	
	private static void initializeCallbacks(PreviewCallback callback){
		initializeCallbacks(callback, SgsCameraProducer.instance);
	}
	
	private static void initializeCallbacks(PreviewCallback callback, Camera camera){
		if(camera != null){
			if(SgsCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					SgsCameraProducer.setPreviewCallbackWithBufferMethod.invoke(camera, callback);
				} catch (Exception e) {
					Log.e(SgsCameraProducer.TAG, e.toString());
				}
			}
			else{
				camera.setPreviewCallback(callback);
			}
		}
	}
	
	private static void deInitializeCallbacks(){
		deInitializeCallbacks(SgsCameraProducer.instance);
	}
	
	private static void deInitializeCallbacks(Camera camera){
		if(camera!= null){
			if(SgsCameraProducer.setPreviewCallbackWithBufferMethod != null){
				try {
					SgsCameraProducer.setPreviewCallbackWithBufferMethod.invoke(camera, new Object[]{ null });
				} catch (Exception e) {
					Log.e(SgsCameraProducer.TAG, e.toString());
				}
			}
			else{
				camera.setPreviewCallback(null);
			}
		}
	}
	
	public static int getNumberOfCameras() {
		// 1. Android 2.3 or later
		if (SgsApplication.getSDKVersion() >= 9) {
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if (getNumberOfCamerasMethod != null) {
					return (Integer) getNumberOfCamerasMethod.invoke(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	public static Camera openFrontFacingCamera() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Camera camera = null;
		
		// 1. Android 2.3 or later
		if(SgsApplication.getSDKVersion() >= 9){
			try {
				Method getNumberOfCamerasMethod = Camera.class.getDeclaredMethod("getNumberOfCameras");
				if(getNumberOfCamerasMethod != null){
					Integer numberOfCameras = (Integer)getNumberOfCamerasMethod.invoke(null);
					if(numberOfCameras > 1){
						Method openMethod = Camera.class.getDeclaredMethod("open", int.class);
						if((camera = (Camera)openMethod.invoke(null, (numberOfCameras - 1))) != null){
							return camera;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//2. From mapper
		if((camera = FrontFacingCameraMapper.getPreferredCamera()) != null){
			return camera;
		}
		
		//3. Use switcher
		if(FrontFacingCameraSwitcher.getSwitcher() != null){
			camera = Camera.open();
			FrontFacingCameraSwitcher.getSwitcher().invoke(camera, (int)1);
			return camera;
		}
		
		//4. Use parameters
		camera = Camera.open();
		Camera.Parameters parameters = camera.getParameters();
		parameters.set("camera-id", 2);
		camera.setParameters(parameters);
		return camera;
	}
	
	/***
	 * FrontFacingCameraSwitcher
	 * @author Mamadou Diop
	 *
	 */
	static class FrontFacingCameraSwitcher
	{
		private static Method DualCameraSwitchMethod;
		
		static{
			try{
				FrontFacingCameraSwitcher.DualCameraSwitchMethod = Class.forName("android.hardware.Camera").getMethod("DualCameraSwitch",int.class);
			}
			catch(Exception e){
				Log.d(SgsCameraProducer.TAG, e.toString());
			}
		}
		
		static Method getSwitcher(){
			return FrontFacingCameraSwitcher.DualCameraSwitchMethod;
		}
	}
	
	static class FrontFacingCameraMapper
	{
		private static int preferredIndex = -1;
		
		static FrontFacingCameraMapper Map[] = {
			new FrontFacingCameraMapper("android.hardware.HtcFrontFacingCamera", "getCamera"),
			// Sprint: HTC EVO 4G and Samsung Epic 4G
			// DO not forget to change the manifest if you are using OS 1.6 and later
			new FrontFacingCameraMapper("com.sprint.hardware.twinCamDevice.FrontFacingCamera", "getFrontFacingCamera"),
			// Huawei U8230
            new FrontFacingCameraMapper("android.hardware.CameraSlave", "open"),
			// Default: Used for test reflection
			// new FrontFacingCameraMapper("android.hardware.Camera", "open"),
		};
		
		static{
			int index = 0;
			for(FrontFacingCameraMapper ffc: FrontFacingCameraMapper.Map){
				try{
					Class.forName(ffc.className).getDeclaredMethod(ffc.methodName);
					FrontFacingCameraMapper.preferredIndex = index;
					break;
				}
				catch(Exception e){
					Log.d(SgsCameraProducer.TAG, e.toString());
				}
				
				++index;
			}
		}
		
		private final String className;
		private final String methodName;
		
		FrontFacingCameraMapper(String className, String methodName){
			this.className = className;
			this.methodName = methodName;
		}
		
		static Camera getPreferredCamera(){
			if(FrontFacingCameraMapper.preferredIndex == -1){
				return null;
			}
			
			try{				
				Method method = Class.forName(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].className)
				.getDeclaredMethod(FrontFacingCameraMapper.Map[FrontFacingCameraMapper.preferredIndex].methodName);
				return (Camera)method.invoke(null);
			}
			catch(Exception e){
				Log.e(SgsCameraProducer.TAG, e.toString());
			}
			return null;
		}
	}
}
