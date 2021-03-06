/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.util.ArrayList;
import java.util.Date;




import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.enlinkd.snmp.CdpCacheTableTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpGlobalGroupTracker;
import org.opennms.netmgt.enlinkd.snmp.CdpInterfacePortNameGetter;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OspfElement.TruthValue;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public final class NodeDiscoveryCdp extends NodeDiscovery {
	private final static Logger LOG = LoggerFactory.getLogger(NodeDiscoveryCdp.class);

	/**
	 * Constructs a new SNMP collector for Cdp Node Discovery. 
	 * The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
	 * @param EnhancedLinkd linkd
	 * @param LinkableNode node
	 */
    public NodeDiscoveryCdp(final EnhancedLinkd linkd, final Node node) {
    	super(linkd, node);
    }

    protected void runCollection() {

    	final Date now = new Date(); 
        LOG.debug("run: collecting : {}", getPeer());

        final CdpGlobalGroupTracker cdpGlobalGroup = new CdpGlobalGroupTracker();


        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), cdpGlobalGroup).
            withDescription("cdpGlobalGroup").
            withLocation(getLocation()).
            execute().
            get();
       } catch (ExecutionException e) {
           LOG.info("run: Agent error while scanning the cdpGlobalGroup table", e);
           return;
       } catch (final InterruptedException e) {
           LOG.info("run: Cdp cdpGlobalGroup collection interrupted, exiting",e);
           return;
       }
       if (cdpGlobalGroup.getCdpDeviceId() == null ) {
            LOG.info("run: cdp mib not supported on: {}", str(getPeer().getAddress()));
            return;
       } 
       CdpElement cdpElement = cdpGlobalGroup.getCdpElement();
       m_linkd.getQueryManager().store(getNodeId(), cdpElement);
       if (cdpElement.getCdpGlobalRun() == TruthValue.FALSE) {
           LOG.info("run: cdp disabled on: {}", str(getPeer().getAddress()));
           return;
       }

        
       List<CdpLink> links = new ArrayList<CdpLink>();
        CdpCacheTableTracker cdpCacheTable = new CdpCacheTableTracker() {

            public void processCdpCacheRow(final CdpCacheRow row) {
                links.add(row.getLink());
            }
       };

        try {
            m_linkd.getLocationAwareSnmpClient().walk(getPeer(), cdpCacheTable).
            withDescription("cdpCacheTable").
            withLocation(getLocation()).
            execute().
            get();
        } catch (ExecutionException e) {
            LOG.error("run: collection execution failed, exiting",e);
            return;
        } catch (final InterruptedException e) {
            LOG.error("run: Cdp Linkd collection interrupted, exiting",e);
            return;
        }
        final CdpInterfacePortNameGetter cdpInterfacePortNameGetter = new CdpInterfacePortNameGetter(getPeer(), 
                                                                                                     m_linkd.getLocationAwareSnmpClient(),
                                                                                                     getLocation());
        for (CdpLink link: links)
            m_linkd.getQueryManager().store(getNodeId(),cdpInterfacePortNameGetter.get(link));
        
        m_linkd.getQueryManager().reconcileCdp(getNodeId(),now);
    }

	@Override
	public String getInfo() {
        return "ReadyRunnable CdpLinkNodeDiscovery" + " ip=" + str(getTarget())
                + " port=" + getPort() + " community=" + getReadCommunity()
                + " package=" + getPackageName();
	}

	@Override
	public String getName() {
		return "CdpLinksDiscovery";
	}

}
