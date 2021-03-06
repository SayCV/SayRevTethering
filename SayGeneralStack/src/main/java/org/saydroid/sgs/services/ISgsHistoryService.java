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
package org.saydroid.sgs.services;

import java.util.List;

import org.saydroid.sgs.model.SgsHistoryEvent;
import org.saydroid.sgs.utils.SgsObservableList;
import org.saydroid.sgs.utils.SgsPredicate;


/**@page SgsHistoryService_page History Service
 * This service is used to store/retrieve history event (audio/video, messaging, ...). You should never create or start this service by yourself. <br />
 * An instance of this service could be retrieved like this:
 * @code
 * final ISgsHistoryService mHistoryService = SgsEngine.getInstance().getHistoryService();
 * @endcode
 * 
 */
public interface ISgsHistoryService extends ISgsBaseService{
	boolean load();
	/**
	 * Checks whether the service is loading the entries
	 * @return true if the entries are being loaded and false otherwise
	 */
	boolean isLoading();
	/**
	 * Adds new event into the history. The event will be put in front of the list.
	 * @param event the event to put into the list of events
	 */
	void addEvent(SgsHistoryEvent event);
	/**
	 * Updates and event and commit the changes.
	 * @param event the event to update
	 */
    void updateEvent(SgsHistoryEvent event);
    /**
     * Deletes an event from the history list
     * @param event the event to delete
     */
    void deleteEvent(SgsHistoryEvent event);
    /**
     * Deletes an event from the history list
     * @param location the location (zero-based index) of the event to remove from the history list
     */
    void deleteEvent(final int location);
    /**
     * Deletes events matching the given criteria from the history list
     * @param predicate the predicate function used to check if an event should be deleted or not
     * @code
     * // Delete all "File Transfer" events stored in the history list
     * final ISgsHistoryService historyService = SgsEngine.getInstance().getHistoryService();
     * historyService.deleteEvents(new SgsPredicate<SgsHistoryEvent>() {
			@Override
			public boolean apply(SgsHistoryEvent event) {
				// TODO Auto-generated method stub
				return event.getMediaType() == SgsMediaType.FileTransfer;
			}
		});
     * @endcode
     */
    void deleteEvents(SgsPredicate<SgsHistoryEvent> predicate);
    /**
     * Removes all events from the history list
     */
    void clear();
    /**
     * Gets the list of all stored events
     * @return an observable collection containing all the events
     * @sa @ref getEvents()
     */
    SgsObservableList<SgsHistoryEvent> getObservableEvents();
    /**
     * Gets the list of all stored events
     * @return a collection containing all the events
     * @sa @ref getObservableEvents()
     */
    List<SgsHistoryEvent> getEvents();
}
