/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.netconf.impl.osgi;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.Set;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperationService;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperationServiceFactory;
import org.opendaylight.controller.netconf.mapping.api.NetconfOperationServiceSnapshot;
import org.opendaylight.controller.netconf.util.CloseableUtil;

public class NetconfOperationServiceSnapshotImpl implements NetconfOperationServiceSnapshot {

    private final Set<NetconfOperationService> services;
    private final String netconfSessionIdForReporting;

    public NetconfOperationServiceSnapshotImpl(final Set<NetconfOperationServiceFactory> factories, final String sessionIdForReporting) {
        final Builder<NetconfOperationService> b = ImmutableSet.builder();
        netconfSessionIdForReporting = sessionIdForReporting;
        for (NetconfOperationServiceFactory factory : factories) {
            b.add(factory.createService(netconfSessionIdForReporting));
        }
        this.services = b.build();
    }

    @Override
    public String getNetconfSessionIdForReporting() {
        return netconfSessionIdForReporting;
    }

    @Override
    public Set<NetconfOperationService> getServices() {
        return services;
    }

    @Override
    public void close() throws Exception {
        CloseableUtil.closeAll(services);
    }

    @Override
    public String toString() {
        return "NetconfOperationServiceSnapshotImpl{" + netconfSessionIdForReporting + '}';
    }
}
