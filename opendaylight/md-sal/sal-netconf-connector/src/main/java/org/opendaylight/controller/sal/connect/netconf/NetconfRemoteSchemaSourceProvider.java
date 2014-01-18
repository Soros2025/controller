package org.opendaylight.controller.sal.connect.netconf;

import java.util.Set;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.monitoring.rev101004.NetconfState;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;
import org.opendaylight.yangtools.yang.data.impl.ImmutableCompositeNode;
import org.opendaylight.yangtools.yang.data.impl.util.CompositeNodeBuilder;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

class NetconfRemoteSchemaSourceProvider implements SchemaSourceProvider<String> {

    public static final QName IETF_NETCONF_MONITORING = QName.create(
            "urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring", "2010-10-04", "ietf-netconf-monitoring");
    public static final QName GET_SCHEMA_QNAME = QName.create(IETF_NETCONF_MONITORING, "get-schema");
    public static final QName GET_DATA_QNAME = QName.create(IETF_NETCONF_MONITORING, "data");

    NetconfDevice device;

    public NetconfRemoteSchemaSourceProvider(NetconfDevice device) {
        super();
        this.device = device;
    }

    @Override
    public Optional<String> getSchemaSource(String moduleName, Optional<String> revision) {
        CompositeNodeBuilder<ImmutableCompositeNode> request = ImmutableCompositeNode.builder(); //
        request.setQName(GET_SCHEMA_QNAME) //
                .addLeaf("format", "yang") //
                .addLeaf("identifier", moduleName); //
        if (revision.isPresent()) {
            request.addLeaf("version", revision.get());
        }

        device.logger.info("Loading YANG schema source for {}:{}", moduleName, revision);
        RpcResult<CompositeNode> schemaReply = device.invokeRpc(GET_SCHEMA_QNAME, request.toInstance());
        if (schemaReply.isSuccessful()) {
            String schemaBody = getSchemaFromRpc(schemaReply.getResult());
            if (schemaBody != null) {
                device.logger.info("YANG Schema successfully retrieved from remote for {}:{}", moduleName, revision);
                return Optional.of(schemaBody);
            }
        }
        device.logger.info("YANG shcema was not successfully retrieved.");
        return Optional.absent();
    }

    private String getSchemaFromRpc(CompositeNode result) {
        if (result == null) {
            return null;
        }
        SimpleNode<?> simpleNode = result.getFirstSimpleByName(GET_DATA_QNAME.withoutRevision());
        Object potential = simpleNode.getValue();
        if (potential instanceof String) {
            return (String) potential;
        }
        return null;
    }
    
    public static final boolean isSupportedFor(Set<QName> capabilities) {
        return capabilities.contains(IETF_NETCONF_MONITORING);
    }
}