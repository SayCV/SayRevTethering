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

package org.saydroid.tether.usb.Events;

public enum TetheringErrorsEventTypes{
    TETHER_ERROR_NO_ERROR(0),
    TETHER_ERROR_UNKNOWN_IFACE(1),
    TETHER_ERROR_SERVICE_UNAVAIL(2),
    TETHER_ERROR_UNSUPPORTED(3),
    TETHER_ERROR_UNAVAIL_IFACE(4),
    TETHER_ERROR_MASTER_ERROR(5),
    TETHER_ERROR_TETHER_IFACE_ERROR(6),
    TETHER_ERROR_UNTETHER_IFACE_ERROR(7),
    TETHER_ERROR_ENABLE_NAT_ERROR(8),
    TETHER_ERROR_DISABLE_NAT_ERROR(9),
    TETHER_ERROR_IFACE_CFG_ERROR(10);

    private final int value;
    private TetheringErrorsEventTypes(int value){
        this.value = value;
    }

    // same to Override Enum.ValueOf(class, string).ordinal()
    public int getValue() {
        return value;
    }
}


