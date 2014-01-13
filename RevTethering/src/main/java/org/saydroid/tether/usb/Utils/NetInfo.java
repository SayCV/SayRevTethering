/*****************************************************
 * WARNING: this file was generated by -e
 * on Mon Jan 13 23:21:07 2014.
 * Any changes made here will be LOST.
 *****************************************************/

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
 * NetInfo sigar class.
 */
public class NetInfo implements java.io.Serializable {

    private static final long serialVersionUID = 12688L;

    public NetInfo() { }

    public native void gather(Sigar sigar) throws SigarException;

    /**
     * This method is not intended to be called directly.
     * use Sigar.getNetInfo() instead.
     * @exception SigarException on failure.
     * @see //org.hyperic.sigar.Sigar#getNetInfo
     */
    static NetInfo fetch(Sigar sigar) throws SigarException {
        NetInfo netInfo = new NetInfo();
        netInfo.gather(sigar);
        return netInfo;
    }

    String defaultGateway = null;

    /**
     * Get the default_gateway.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return default_gateway
     */
    public String getDefaultGateway() { return defaultGateway; }
    String defaultGatewayInterface = null;

    /**
     * Get the default_gateway_interface.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return default_gateway_interface
     */
    public String getDefaultGatewayInterface() { return defaultGatewayInterface; }
    String hostName = null;

    /**
     * Get the host_name.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return host_name
     */
    public String getHostName() { return hostName; }
    String domainName = null;

    /**
     * Get the domain_name.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return domain_name
     */
    public String getDomainName() { return domainName; }
    String primaryDns = null;

    /**
     * Get the primary_dns.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return primary_dns
     */
    public String getPrimaryDns() { return primaryDns; }
    String secondaryDns = null;

    /**
     * Get the secondary_dns.<p>
     * Supported Platforms: Undocumented.
     * <p>
     * @return secondary_dns
     */
    public String getSecondaryDns() { return secondaryDns; }

    void copyTo(NetInfo copy) {
        copy.defaultGateway = this.defaultGateway;
        copy.defaultGatewayInterface = this.defaultGatewayInterface;
        copy.hostName = this.hostName;
        copy.domainName = this.domainName;
        copy.primaryDns = this.primaryDns;
        copy.secondaryDns = this.secondaryDns;
    }

    public Map toMap() {
        Map map = new HashMap();
        String strdefaultGateway = 
            String.valueOf(this.defaultGateway);
        if (!"-1".equals(strdefaultGateway))
            map.put("DefaultGateway", strdefaultGateway);
        String strdefaultGatewayInterface = 
            String.valueOf(this.defaultGatewayInterface);
        if (!"-1".equals(strdefaultGatewayInterface))
            map.put("DefaultGatewayInterface", strdefaultGatewayInterface);
        String strhostName = 
            String.valueOf(this.hostName);
        if (!"-1".equals(strhostName))
            map.put("HostName", strhostName);
        String strdomainName = 
            String.valueOf(this.domainName);
        if (!"-1".equals(strdomainName))
            map.put("DomainName", strdomainName);
        String strprimaryDns = 
            String.valueOf(this.primaryDns);
        if (!"-1".equals(strprimaryDns))
            map.put("PrimaryDns", strprimaryDns);
        String strsecondaryDns = 
            String.valueOf(this.secondaryDns);
        if (!"-1".equals(strsecondaryDns))
            map.put("SecondaryDns", strsecondaryDns);
        return map;
    }

    public String toString() {
        return toMap().toString();
    }

}
