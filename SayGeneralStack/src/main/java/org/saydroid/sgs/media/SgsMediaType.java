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

import android.net.TrafficStats;

public enum SgsMediaType {
	None,
	Audio,
	Video,
	AudioVideo,
	SMS,
	Chat,
	FileTransfer,
    TrafficCount;
	
	public static boolean isAudioVideoType(SgsMediaType type){
		return type == Audio || type == AudioVideo || type == Video;
	}
	public static boolean isFileTransfer(SgsMediaType type){
		return type == FileTransfer;
	}
	public static boolean isChat(SgsMediaType type){
		return type == Chat;
	}
	public static boolean isMsrpType(SgsMediaType type){
		return isFileTransfer(type) || isChat(type);
	}
    public static boolean isTrafficCountType(SgsMediaType type){
        return type == TrafficCount;
    }
}