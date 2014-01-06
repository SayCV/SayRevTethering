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

package org.saydroid.tether.usb;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomDialog {
	
	public static void show(Context context, int icon, String title, String msg, String positiveText, DialogInterface.OnClickListener positive, String negativeText, DialogInterface.OnClickListener negative){
		AlertDialog.Builder builder;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_dialog, null);

		ImageView ivIcon = (ImageView) layout.findViewById(R.id.custom_dialog_imageView_icon);
		ivIcon.setImageResource(icon);
		TextView tvTitle = (TextView) layout.findViewById(R.id.custom_dialog_textView_title);
		tvTitle.setText((title == null) ? "" : title);
		TextView tvMsg = (TextView) layout.findViewById(R.id.custom_dialog_textView_msg);
		tvMsg.setText(msg);

		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		if(positive != null && positiveText != null){
			builder.setPositiveButton(positiveText, positive);
		}
		if(negative != null && negativeText != null){
			builder.setNegativeButton(negativeText, negative);
		}
		
		builder.create().show();
	}

    public static Dialog showProgressDialog(Context context, int icon, String title, String msg, DialogInterface.OnCancelListener onCancelListener){
//        ProgressDialog.Builder builder;
        ProgressDialog pd;
//
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View layout = inflater.inflate(R.layout.custom_dialog, null);
//
//        ImageView ivIcon = (ImageView) layout.findViewById(R.id.custom_dialog_imageView_icon);
//        ivIcon.setImageResource(icon);
//        TextView tvTitle = (TextView) layout.findViewById(R.id.custom_dialog_textView_title);
//        tvTitle.setText((title == null) ? "" : title);
//        TextView tvMsg = (TextView) layout.findViewById(R.id.custom_dialog_textView_msg);
//        tvMsg.setText(msg);
//
//        builder = new ProgressDialog.Builder(context);
//        //builder.setView(layout);
//        builder.setTitle(title);
//        builder.setMessage(msg);
//        builder.setCancelable(false);
//        if(positive != null && positiveText != null){
//            builder.setPositiveButton(positiveText, positive);
//        }
//        if(negative != null && negativeText != null){
//            builder.setNegativeButton(negativeText, negative);
//        }

//        builder.create().show();

//        show(Context context, CharSequence title,
//                CharSequence message, boolean indeterminate,
//                boolean cancelable, OnCancelListener cancelListener)

        pd = new ProgressDialog(context);
        pd.setTitle(title);
        pd.setMessage(msg);
        pd.setIndeterminate(false);
        pd.setCancelable(false);
        if(onCancelListener != null){
            /*ProgressDialog.show(context, title,
                    msg, false, false, onCancelListener);*/
            pd.setOnCancelListener(onCancelListener);
        }
        pd.show();
        return pd;
    }
}
