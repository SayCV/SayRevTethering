/*
 * Copyright (C) 2013, sayDroid.
 *
 * Copyright 2013 The sayDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.saydroid.tether.usb.Utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.saydroid.sgs.R;

public class DialerUtils {
	public static final int TAG_0 = 0;
	public static final int TAG_1 = 1;
	public static final int TAG_2 = 2;
	public static final int TAG_3 = 3;
	public static final int TAG_4 = 4;
	public static final int TAG_5 = 5;
	public static final int TAG_6 = 6;
	public static final int TAG_7 = 7;
	public static final int TAG_8 = 8;
	public static final int TAG_9 = 9;
	public static final int TAG_STAR = 10;
	public static final int TAG_SHARP = 11;
	public static final int TAG_CHAT = 12;
	public static final int TAG_AUDIO_CALL = 13;
	public static final int TAG_VIDEO_CALL = 14;
	public static final int TAG_DELETE = 15;
	
	public static void setDialerTextButton(View view, String num, String text, int tag, View.OnClickListener listener){
		view.setTag(tag);
		view.setOnClickListener(listener);
		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_num)).setText(num);
		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_text)).setText(text);
	}
	
	public static void setDialerTextButton(Activity parent, int viewId, String num, String text, int tag, View.OnClickListener listener){
		setDialerTextButton(parent.findViewById(viewId), num, text, tag, listener);
	}
	
	public static void setDialerTextButton(View parent, int viewId, String num, String text, int tag, View.OnClickListener listener){
		setDialerTextButton(parent.findViewById(viewId), num, text, tag, listener);
	}
	
	public static void setDialerImageButton(Activity parent, int viewId, int imageId, int tag, View.OnClickListener listener){
		View view = parent.findViewById(viewId);
		view.setTag(tag);
		view.setOnClickListener(listener);
		((ImageView)view.findViewById(R.id.view_dialer_button_image_imageView)).setImageResource(imageId);
	}
}