/*
 * Copyright (c) 2006-2007, 2009 Hyperic, Inc.
 * Copyright (c) 2009 SpringSource, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.util.HashMap;
import java.util.Map;

/**
 * Tcp sigar class.
 */
public class Tcp implements java.io.Serializable {

    private static final long serialVersionUID = 14992L;

    public Tcp() { }

    public native void gather(Sigar sigar) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getTcp() instead.
     * @exception SigarException on failure.
     * @see //org.hyperic.sigar.Sigar#getTcp
     */
    static Tcp fetch(Sigar sigar) throws SigarException {
        Tcp tcp = new Tcp();
        tcp.gather(sigar);
        return tcp;
    }

    long activeOpens = 0;

    /**
     * Get the active_opens.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return active_opens
     */
    public long getActiveOpens() { return activeOpens; }
    long passiveOpens = 0;

    /**
     * Get the passive_opens.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return passive_opens
     */
    public long getPassiveOpens() { return passiveOpens; }
    long attemptFails = 0;

    /**
     * Get the attempt_fails.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return attempt_fails
     */
    public long getAttemptFails() { return attemptFails; }
    long estabResets = 0;

    /**
     * Get the estab_resets.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return estab_resets
     */
    public long getEstabResets() { return estabResets; }
    long currEstab = 0;

    /**
     * Get the curr_estab.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return curr_estab
     */
    public long getCurrEstab() { return currEstab; }
    long inSegs = 0;

    /**
     * Get the in_segs.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return in_segs
     */
    public long getInSegs() { return inSegs; }
    long outSegs = 0;

    /**
     * Get the out_segs.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return out_segs
     */
    public long getOutSegs() { return outSegs; }
    long retransSegs = 0;

    /**
     * Get the retrans_segs.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return retrans_segs
     */
    public long getRetransSegs() { return retransSegs; }
    long inErrs = 0;

    /**
     * Get the in_errs.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return in_errs
     */
    public long getInErrs() { return inErrs; }
    long outRsts = 0;

    /**
     * Get the out_rsts.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * System equivalent commands:<ul>
     * <li> Linux: <code>cat /proc/net/snmp</code><br>
     * <li> Solaris: <code>netstat -s -P tcp</code><br>
     * </ul>
     * @return out_rsts
     */
    public long getOutRsts() { return outRsts; }

    void copyTo(Tcp copy) {
        copy.activeOpens = this.activeOpens;
        copy.passiveOpens = this.passiveOpens;
        copy.attemptFails = this.attemptFails;
        copy.estabResets = this.estabResets;
        copy.currEstab = this.currEstab;
        copy.inSegs = this.inSegs;
        copy.outSegs = this.outSegs;
        copy.retransSegs = this.retransSegs;
        copy.inErrs = this.inErrs;
        copy.outRsts = this.outRsts;
    }

    public Map toMap() {
        Map map = new HashMap();
        String stractiveOpens = 
            String.valueOf(this.activeOpens);
        if (!"-1".equals(stractiveOpens))
            map.put("ActiveOpens", stractiveOpens);
        String strpassiveOpens = 
            String.valueOf(this.passiveOpens);
        if (!"-1".equals(strpassiveOpens))
            map.put("PassiveOpens", strpassiveOpens);
        String strattemptFails = 
            String.valueOf(this.attemptFails);
        if (!"-1".equals(strattemptFails))
            map.put("AttemptFails", strattemptFails);
        String strestabResets = 
            String.valueOf(this.estabResets);
        if (!"-1".equals(strestabResets))
            map.put("EstabResets", strestabResets);
        String strcurrEstab = 
            String.valueOf(this.currEstab);
        if (!"-1".equals(strcurrEstab))
            map.put("CurrEstab", strcurrEstab);
        String strinSegs = 
            String.valueOf(this.inSegs);
        if (!"-1".equals(strinSegs))
            map.put("InSegs", strinSegs);
        String stroutSegs = 
            String.valueOf(this.outSegs);
        if (!"-1".equals(stroutSegs))
            map.put("OutSegs", stroutSegs);
        String strretransSegs = 
            String.valueOf(this.retransSegs);
        if (!"-1".equals(strretransSegs))
            map.put("RetransSegs", strretransSegs);
        String strinErrs = 
            String.valueOf(this.inErrs);
        if (!"-1".equals(strinErrs))
            map.put("InErrs", strinErrs);
        String stroutRsts = 
            String.valueOf(this.outRsts);
        if (!"-1".equals(stroutRsts))
            map.put("OutRsts", stroutRsts);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}
