/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.controller.sal.match;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.Property;
import org.opendaylight.controller.sal.core.Tables;
import org.opendaylight.controller.sal.core.Tier;
import org.opendaylight.controller.sal.utils.EtherTypes;
import org.opendaylight.controller.sal.utils.IPProtocols;
import org.opendaylight.controller.sal.utils.NodeConnectorCreator;
import org.opendaylight.controller.sal.utils.NodeCreator;

public class MatchTest {
    @Test
    public void testMatchCreation() {
        Node node = NodeCreator.createOFNode(7L);
        NodeConnector port = NodeConnectorCreator.createOFNodeConnector((short) 6, node);
        MatchField field = new MatchField(MatchType.IN_PORT, port);

        Assert.assertTrue(field != null);
        Assert.assertTrue(field.getType() == MatchType.IN_PORT);
        Assert.assertTrue((NodeConnector) field.getValue() == port);
        Assert.assertTrue(field.isValid());

        field = null;
        field = new MatchField(MatchType.TP_SRC, Long.valueOf(23));
        Assert.assertFalse(field.isValid());

        field = null;
        field = new MatchField(MatchType.TP_SRC, (long) 45);
        Assert.assertFalse(field.isValid());

        field = null;
        field = new MatchField(MatchType.TP_SRC, 120000);
        Assert.assertFalse(field.isValid());

        byte mac[] = { (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 11, (byte) 22 };
        byte mask[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
        field = null;
        field = new MatchField(MatchType.DL_SRC, mac, mask);
        Assert.assertFalse(field.getValue() == null);

        field = null;
        field = new MatchField(MatchType.NW_TOS, (byte) 0x22, (byte) 0x3);
        Assert.assertFalse(field.getValue() == null);
    }

    @Test
    public void testMatchSetGet() {
        Match x = new Match();
        short val = 2346;
        NodeConnector inPort = NodeConnectorCreator.createOFNodeConnector(val, NodeCreator.createOFNode(1L));
        x.setField(MatchType.IN_PORT, inPort);
        Assert.assertTrue(((NodeConnector) x.getField(MatchType.IN_PORT).getValue()).equals(inPort));
        Assert.assertTrue((Short) ((NodeConnector) x.getField(MatchType.IN_PORT).getValue()).getID() == val);
    }

    @Test
    public void testMatchSetGetMAC() {
        Match x = new Match();
        byte mac[] = { (byte) 0xaa, (byte) 0xbb, (byte) 0xcc, (byte) 0xdd, (byte) 11, (byte) 22 };
        byte mac2[] = { (byte) 0xaa, (byte) 0xbb, 0, 0, 0, (byte) 0xbb };
        byte mask1[] = { (byte) 0x11, (byte) 0x22, (byte) 0x33, (byte) 0x44, (byte) 0x55, (byte) 0x66 };
        byte mask2[] = { (byte) 0xff, (byte) 0xff, (byte) 0, (byte) 0, (byte) 0, (byte) 0xff };

        x.setField(MatchType.DL_SRC, mac.clone(), mask1);
        x.setField(MatchType.DL_DST, mac2.clone(), mask2);
        Assert.assertTrue(Arrays.equals(mac, (byte[]) x.getField(MatchType.DL_SRC).getValue()));
        Assert.assertFalse(Arrays.equals((byte[]) x.getField(MatchType.DL_SRC).getValue(),
                (byte[]) x.getField(MatchType.DL_DST).getValue()));
        Assert.assertFalse(x.getField(MatchType.DL_SRC).getBitMask() == x.getField(MatchType.DL_DST).getBitMask());

        x.setField(new MatchField(MatchType.DL_DST, mac.clone(), mask1));
        Assert.assertTrue(Arrays.equals((byte[]) x.getField(MatchType.DL_SRC).getValue(),
                (byte[]) x.getField(MatchType.DL_DST).getValue()));
    }

    @Test
    public void testMatchSetGetNWAddr() throws UnknownHostException {
        Match x = new Match();
        String ip = "172.20.231.23";
        InetAddress address = InetAddress.getByName(ip);
        InetAddress mask = InetAddress.getByName("255.255.0.0");

        x.setField(MatchType.NW_SRC, address, mask);
        Assert.assertTrue(ip.equals(((InetAddress) x.getField(MatchType.NW_SRC).getValue()).getHostAddress()));
        Assert.assertTrue(x.getField(MatchType.NW_SRC).getMask().equals(mask));
    }

    @Test
    public void testMatchSetGetEtherType() throws UnknownHostException {
        Match x = new Match();

        x.setField(MatchType.DL_TYPE, EtherTypes.QINQ.shortValue(), (short) 0xffff);
        Assert.assertTrue(((Short) x.getField(MatchType.DL_TYPE).getValue()).equals(EtherTypes.QINQ.shortValue()));
        Assert.assertFalse(x.getField(MatchType.DL_TYPE).getValue() == EtherTypes.QINQ);
        Assert.assertFalse(x.getField(MatchType.DL_TYPE).getValue().equals(EtherTypes.QINQ));

        x.setField(MatchType.DL_TYPE, EtherTypes.LLDP.shortValue(), (short) 0xffff);
        Assert.assertTrue(((Short) x.getField(MatchType.DL_TYPE).getValue()).equals(EtherTypes.LLDP.shortValue()));
        Assert.assertFalse(x.getField(MatchType.DL_TYPE).equals(EtherTypes.LLDP.intValue()));
    }

    @Test
    public void testSetGetNwTos() {
        Match x = new Match();
        x.setField(MatchType.NW_TOS, (byte) 0xb, (byte) 0xf);

        Byte t = new Byte((byte) 0xb);

        Object o = x.getField(MatchType.NW_TOS).getValue();
        Assert.assertTrue(o.equals(t));
        Assert.assertTrue(o.equals((byte) 0xb));
    }

    @Test
    public void testSetGetNwProto() {
        Match x = new Match();
        byte proto = (byte) 199;
        x.setField(MatchType.NW_PROTO, proto, (byte) 0xff);

        Object o = x.getField(MatchType.NW_PROTO).getValue();
        Assert.assertTrue(o.equals(proto));
    }

    @Test
    public void testSetTpSrc() {
        // Minimum value validation.
        Match match = new Match();
        short tp_src = 0;
        match.setField(MatchType.TP_SRC, tp_src);

        Object o = match.getField(MatchType.TP_SRC).getValue();
        Assert.assertTrue(o.equals(tp_src));

        // Maximum value validation.
        match = new Match();
        tp_src = (short) 0xffff;
        match.setField(MatchType.TP_SRC, tp_src);

        o = match.getField(MatchType.TP_SRC).getValue();
        Assert.assertTrue(o.equals(tp_src));
    }

    @Test
    public void testSetTpDst() {
        // Minimum value validation.
        Match match = new Match();
        short tp_dst = 0;
        match.setField(MatchType.TP_DST, tp_dst);

        Object o = match.getField(MatchType.TP_DST).getValue();
        Assert.assertTrue(o.equals(tp_dst));

        // Maximum value validation.
        match = new Match();
        tp_dst = (short) 0xffff;
        match.setField(MatchType.TP_DST, tp_dst);

        o = match.getField(MatchType.TP_DST).getValue();
        Assert.assertTrue(o.equals(tp_dst));
    }

    @Test
    public void testMatchMask() {
        Match x = new Match();
        NodeConnector inPort = NodeConnectorCreator.createOFNodeConnector((short) 6, NodeCreator.createOFNode(3L));
        x.setField(MatchType.IN_PORT, inPort);
        x.setField(MatchType.DL_VLAN, (short) 28, (short) 0xfff);
        Assert.assertFalse(x.getMatches() == 0);
        Assert.assertTrue(x.getMatches() == (MatchType.IN_PORT.getIndex() | MatchType.DL_VLAN.getIndex()));
    }

    @Test
    public void testMatchBitMask() {
        byte mac[] = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 22, (byte) 12 };
        byte mask[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0 };
        NodeConnector inPort = NodeConnectorCreator.createOFNodeConnector((short) 4095, NodeCreator.createOFNode(7L));

        MatchField x = new MatchField(MatchType.IN_PORT, inPort);
        Assert.assertTrue((x.getMask()) == null);

        x = new MatchField(MatchType.DL_VLAN, (short) 255, (short) 0xff);
        Assert.assertTrue(x.getBitMask() == 0xff);

        x = new MatchField(MatchType.DL_SRC, mac, mask);
        Assert.assertTrue(x.getMask().equals(mask));
        Assert.assertTrue(x.getBitMask() == 0xffffffffff00L);
    }

    @Test
    public void testNullMask() {
        byte mac[] = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 22, (byte) 12 };
        NodeConnector inPort = NodeConnectorCreator.createOFNodeConnector((short) 2000, NodeCreator.createOFNode(7L));

        MatchField x = new MatchField(MatchType.IN_PORT, inPort);
        Assert.assertTrue(x.getBitMask() == 0);

        x = new MatchField(MatchType.NW_PROTO, (byte) 17);
        Assert.assertTrue(x.getBitMask() == 0xff);

        x = new MatchField(MatchType.DL_VLAN, (short) 255);
        Assert.assertTrue(x.getBitMask() == 0xfff);

        x = new MatchField(MatchType.DL_SRC, mac);
        Assert.assertTrue(x.getBitMask() == 0xffffffffffffL);
    }

    @Test
    public void testEquality() throws Exception {
        Node node = NodeCreator.createOFNode(7L);
        NodeConnector port = NodeConnectorCreator.createOFNodeConnector((short) 24, node);
        NodeConnector port2 = NodeConnectorCreator.createOFNodeConnector((short) 24, node);
        byte srcMac[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte dstMac[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        byte srcMac2[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte dstMac2[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        InetAddress srcIP = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        InetAddress dstIP = InetAddress.getByName("2001:420:281:1004:e123:e688:d655:a1b0");
        InetAddress ipMask = InetAddress.getByName("ffff:ffff:ffff:ffff:0:0:0:0");
        InetAddress ipMaskd = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:0");
        InetAddress srcIP2 = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        InetAddress dstIP2 = InetAddress.getByName("2001:420:281:1004:e123:e688:d655:a1b0");
        InetAddress ipMask2 = InetAddress.getByName("ffff:ffff:ffff:ffff:0:0:0:0");
        InetAddress ipMaskd2 = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:0");
        short ethertype = EtherTypes.IPv6.shortValue();
        short ethertype2 = EtherTypes.IPv6.shortValue();
        short vlan = (short) 27, vlan2 = (short) 27;
        byte vlanPr = (byte) 3, vlanPr2 = (byte) 3;
        Byte tos = 4, tos2 = 4;
        byte proto = IPProtocols.UDP.byteValue(), proto2 = IPProtocols.UDP.byteValue();
        short src = (short) 5500, src2 = (short) 5500;
        short dst = 80, dst2 = 80;

        /*
         * Create a SAL Flow aFlow
         */
        Match match1 = new Match();
        Match match2 = new Match();
        match1.setField(MatchType.IN_PORT, port);
        match1.setField(MatchType.DL_SRC, srcMac);
        match1.setField(MatchType.DL_DST, dstMac);
        match1.setField(MatchType.DL_TYPE, ethertype);
        match1.setField(MatchType.DL_VLAN, vlan);
        match1.setField(MatchType.DL_VLAN_PR, vlanPr);
        match1.setField(MatchType.NW_SRC, srcIP, ipMask);
        match1.setField(MatchType.NW_DST, dstIP, ipMaskd);
        match1.setField(MatchType.NW_TOS, tos);
        match1.setField(MatchType.NW_PROTO, proto);
        match1.setField(MatchType.TP_SRC, src);
        match1.setField(MatchType.TP_DST, dst);

        match2.setField(MatchType.IN_PORT, port2);
        match2.setField(MatchType.DL_SRC, srcMac2);
        match2.setField(MatchType.DL_DST, dstMac2);
        match2.setField(MatchType.DL_TYPE, ethertype2);
        match2.setField(MatchType.DL_VLAN, vlan2);
        match2.setField(MatchType.DL_VLAN_PR, vlanPr2);
        match2.setField(MatchType.NW_SRC, srcIP2, ipMask2);
        match2.setField(MatchType.NW_DST, dstIP2, ipMaskd2);
        match2.setField(MatchType.NW_TOS, tos2);
        match2.setField(MatchType.NW_PROTO, proto2);
        match2.setField(MatchType.TP_SRC, src2);
        match2.setField(MatchType.TP_DST, dst2);

        Assert.assertTrue(match1.equals(match2));

        // Make sure all values are equals
        for (MatchType type : MatchType.values()) {
            if (match1.isPresent(type)) {
                Assert.assertTrue(match1.getField(type).equals(match2.getField(type)));
            }
        }

        // Make none of the fields couples are pointing to the same reference
        MatchField a = null, b = null;
        for (MatchType type : MatchType.values()) {
            a = match1.getField(type);
            b = match2.getField(type);
            if (a != null && b != null) {
                Assert.assertFalse(a == b);
            }
        }
    }

    @Test
    public void testEqualityNetMask() throws Exception {

        InetAddress srcIP = InetAddress.getByName("1.1.1.1");
        InetAddress ipMask = InetAddress.getByName("255.255.255.255");
        InetAddress srcIP2 = InetAddress.getByName("1.1.1.1");
        InetAddress ipMask2 = null;
        short ethertype = EtherTypes.IPv4.shortValue();
        short ethertype2 = EtherTypes.IPv4.shortValue();

        /*
         * Create a SAL Flow aFlow
         */
        Match match1 = new Match();
        Match match2 = new Match();

        match1.setField(MatchType.DL_TYPE, ethertype);
        match1.setField(MatchType.NW_SRC, srcIP, ipMask);

        match2.setField(MatchType.DL_TYPE, ethertype2);
        match2.setField(MatchType.NW_SRC, srcIP2, ipMask2);

        Assert.assertTrue(match1.equals(match2));

        ipMask2 = InetAddress.getByName("255.255.255.255");
        match2.setField(MatchType.NW_SRC, srcIP2, ipMask2);

        srcIP = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        srcIP2 = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        ipMask = null;
        ipMask2 = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        ethertype = EtherTypes.IPv6.shortValue();
        ethertype2 = EtherTypes.IPv6.shortValue();

        match1.setField(MatchType.DL_TYPE, ethertype);
        match1.setField(MatchType.NW_SRC, srcIP, ipMask);

        match2.setField(MatchType.DL_TYPE, ethertype2);
        match2.setField(MatchType.NW_SRC, srcIP2, ipMask2);

        Assert.assertTrue(match1.equals(match2));
    }

    @Test
    public void testHashCodeWithReverseMatch() throws Exception {
        InetAddress srcIP1 = InetAddress.getByName("1.1.1.1");
        InetAddress ipMask1 = InetAddress.getByName("255.255.255.255");
        InetAddress srcIP2 = InetAddress.getByName("2.2.2.2");
        InetAddress ipMask2 = InetAddress.getByName("255.255.255.255");
        MatchField field1 = new MatchField(MatchType.NW_SRC, srcIP1, ipMask1);
        MatchField field2 = new MatchField(MatchType.NW_DST, srcIP2, ipMask2);
        Match match1 = new Match();
        match1.setField(field1);
        match1.setField(field2);
        Match match2 = match1.reverse();
        Assert.assertFalse(match1.hashCode() == match2.hashCode());
    }

    @Test
    public void testHashCode() throws Exception {
        byte srcMac1[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte srcMac2[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte dstMac1[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        byte dstMac2[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        short ethertype = EtherTypes.IPv4.shortValue();
        short ethertype2 = EtherTypes.IPv4.shortValue();
        InetAddress srcIP1 = InetAddress.getByName("1.1.1.1");
        InetAddress ipMask1 = InetAddress.getByName("255.255.255.255");
        InetAddress srcIP2 = InetAddress.getByName("1.1.1.1");
        InetAddress ipMask2 = InetAddress.getByName("255.255.255.255");

        Match match1 = new Match();
        Match match2 = new Match();

        MatchField field1 = new MatchField(MatchType.DL_SRC, srcMac1);
        MatchField field2 = new MatchField(MatchType.DL_SRC, srcMac2);
        Assert.assertTrue(field1.hashCode() == field2.hashCode());

        match1.setField(field1);
        match2.setField(field2);
        Assert.assertTrue(match1.hashCode() == match2.hashCode());

        MatchField field3 = new MatchField(MatchType.DL_DST, dstMac1);
        MatchField field4 = new MatchField(MatchType.DL_DST, dstMac2);
        Assert.assertTrue(field3.hashCode() == field4.hashCode());

        match1.setField(field3);
        match2.setField(field4);
        Assert.assertTrue(match1.hashCode() == match2.hashCode());

        MatchField field5 = new MatchField(MatchType.DL_TYPE, ethertype);
        MatchField field6 = new MatchField(MatchType.DL_TYPE, ethertype2);
        Assert.assertTrue(field5.hashCode() == field6.hashCode());

        match1.setField(field5);
        match2.setField(field6);
        Assert.assertTrue(match1.hashCode() == match2 .hashCode());

        MatchField field7 = new MatchField(MatchType.NW_SRC, srcIP1, ipMask1);
        MatchField field8 = new MatchField(MatchType.NW_SRC, srcIP2, ipMask2);
        Assert.assertTrue(field7.hashCode() == field8.hashCode());

        match1.setField(field7);
        match2.setField(field8);
        Assert.assertTrue(match1.hashCode() == match2.hashCode());

    }

    @Test
    public void testCloning() throws Exception {
        Node node = NodeCreator.createOFNode(7L);
        NodeConnector port = NodeConnectorCreator.createOFNodeConnector((short) 24, node);
        byte srcMac[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte dstMac[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        InetAddress srcIP = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        InetAddress dstIP = InetAddress.getByName("2001:420:281:1004:e123:e688:d655:a1b0");
        InetAddress ipMasks = InetAddress.getByName("ffff:ffff:ffff:ffff:0:0:0:0");
        InetAddress ipMaskd = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:0");
        short ethertype = EtherTypes.IPv6.shortValue();
        short vlan = (short) 27;
        byte vlanPr = (byte) 3;
        Byte tos = 4;
        byte proto = IPProtocols.UDP.byteValue();
        short src = (short) 5500;
        short dst = 80;

        /*
         * Create a SAL Flow aFlow
         */
        Match match = new Match();
        match.setField(MatchType.IN_PORT, port);
        match.setField(MatchType.DL_SRC, srcMac);
        match.setField(MatchType.DL_DST, dstMac);
        match.setField(MatchType.DL_TYPE, ethertype);
        match.setField(MatchType.DL_VLAN, vlan);
        match.setField(MatchType.DL_VLAN_PR, vlanPr);
        match.setField(MatchType.NW_SRC, srcIP, ipMasks);
        match.setField(MatchType.NW_DST, dstIP, ipMaskd);
        match.setField(MatchType.NW_TOS, tos);
        match.setField(MatchType.NW_PROTO, proto);
        match.setField(MatchType.TP_SRC, src);
        match.setField(MatchType.TP_DST, dst);

        Match cloned = match.clone();

        // Make sure all values are equals
        for (MatchType type : MatchType.values()) {
            if (match.isPresent(type)) {
                if (!match.getField(type).equals(cloned.getField(type))) {
                    Assert.assertTrue(match.getField(type).equals(cloned.getField(type)));
                }
            }
        }

        // Make sure none of the fields couples are pointing to the same
        // reference
        MatchField a = null, b = null;
        for (MatchType type : MatchType.values()) {
            a = match.getField(type);
            b = cloned.getField(type);
            if (a != null && b != null) {
                Assert.assertFalse(a == b);
            }
        }

        Assert.assertTrue(match.equals(cloned));

        Assert.assertFalse(match.getField(MatchType.DL_SRC) == cloned.getField(MatchType.DL_SRC));
        Assert.assertFalse(match.getField(MatchType.NW_DST) == cloned.getField(MatchType.NW_DST));
        Assert.assertTrue(match.getField(MatchType.NW_DST).getMask()
                .equals(cloned.getField(MatchType.NW_DST).getMask()));
        Assert.assertTrue(match.hashCode() == cloned.hashCode());
    }

    @Test
    public void testFlip() throws Exception {
        Node node = NodeCreator.createOFNode(7L);
        NodeConnector port = NodeConnectorCreator.createOFNodeConnector((short) 24, node);
        byte srcMac[] = { (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0x9a, (byte) 0xbc };
        byte dstMac[] = { (byte) 0x1a, (byte) 0x2b, (byte) 0x3c, (byte) 0x4d, (byte) 0x5e, (byte) 0x6f };
        InetAddress srcIP = InetAddress.getByName("2001:420:281:1004:407a:57f4:4d15:c355");
        InetAddress dstIP = InetAddress.getByName("2001:420:281:1004:e123:e688:d655:a1b0");
        InetAddress ipMasks = InetAddress.getByName("ffff:ffff:ffff:ffff:0:0:0:0");
        InetAddress ipMaskd = InetAddress.getByName("ffff:ffff:ffff:ffff:ffff:ffff:ffff:0");
        short ethertype = EtherTypes.IPv6.shortValue();
        short vlan = (short) 27;
        byte vlanPr = (byte) 3;
        Byte tos = 4;
        byte proto = IPProtocols.UDP.byteValue();
        short src = (short) 5500;
        short dst = 80;

        /*
         * Create a SAL Flow aFlow
         */
        Match match = new Match();
        match.setField(MatchType.IN_PORT, port);
        match.setField(MatchType.DL_SRC, srcMac);
        match.setField(MatchType.DL_DST, dstMac);
        match.setField(MatchType.DL_TYPE, ethertype);
        match.setField(MatchType.DL_VLAN, vlan);
        match.setField(MatchType.DL_VLAN_PR, vlanPr);
        match.setField(MatchType.NW_SRC, srcIP, ipMasks);
        match.setField(MatchType.NW_DST, dstIP, ipMaskd);
        match.setField(MatchType.NW_TOS, tos);
        match.setField(MatchType.NW_PROTO, proto);
        match.setField(MatchType.TP_SRC, src);
        match.setField(MatchType.TP_DST, dst);

        Match flipped = match.reverse();

        Assert.assertTrue(match.getField(MatchType.DL_TYPE).equals(flipped.getField(MatchType.DL_TYPE)));
        Assert.assertTrue(match.getField(MatchType.DL_VLAN).equals(flipped.getField(MatchType.DL_VLAN)));

        Assert.assertTrue(match.getField(MatchType.DL_DST).getValue()
                .equals(flipped.getField(MatchType.DL_SRC).getValue()));
        Assert.assertTrue(match.getField(MatchType.DL_DST).getMask() == flipped.getField(MatchType.DL_SRC).getMask());

        Assert.assertTrue(match.getField(MatchType.NW_DST).getValue()
                .equals(flipped.getField(MatchType.NW_SRC).getValue()));
        Assert.assertTrue(match.getField(MatchType.NW_DST).getMask() == flipped.getField(MatchType.NW_SRC).getMask());

        Assert.assertTrue(match.getField(MatchType.TP_DST).getValue()
                .equals(flipped.getField(MatchType.TP_SRC).getValue()));
        Assert.assertTrue(match.getField(MatchType.TP_DST).getMask() == flipped.getField(MatchType.TP_SRC).getMask());

        Match flipflip = flipped.reverse().reverse();
        Assert.assertTrue(flipflip.equals(flipped));

    }

    @Test
    public void testVlanNone() throws Exception {
        // The value 0 is used to indicate that no VLAN ID is set
        short vlan = (short) 0;
        MatchField field = new MatchField(MatchType.DL_VLAN, vlan);

        Assert.assertTrue(field != null);
        Assert.assertTrue(field.getValue().equals(new Short(vlan)));
        Assert.assertTrue(field.isValid());
    }

    @Test
    public void testIntersection() throws UnknownHostException {
        Short ethType = Short.valueOf((short)0x800);
        InetAddress ip1 = InetAddress.getByName("1.1.1.1");
        InetAddress ip2 = InetAddress.getByName("1.1.1.0");
        InetAddress ipm2 = InetAddress.getByName("255.255.255.0");
        InetAddress ip3 = InetAddress.getByName("1.3.0.0");
        InetAddress ipm3 = InetAddress.getByName("255.255.0.0");
        InetAddress ip4 = InetAddress.getByName("1.3.4.4");
        InetAddress ipm4 = InetAddress.getByName("255.255.255.0");

        Match m1 = new Match();
        m1.setField(MatchType.DL_TYPE, ethType);
        m1.setField(MatchType.NW_SRC, ip1);

        Match m2 = new Match();
        m2.setField(MatchType.DL_TYPE, ethType);
        m2.setField(MatchType.NW_SRC, ip2, ipm2);

        Match m3 = new Match();
        m3.setField(MatchType.DL_TYPE, ethType);
        m3.setField(MatchType.NW_SRC, ip3, ipm3);
        m3.setField(MatchType.NW_PROTO, IPProtocols.TCP.byteValue());

        Match m3r = m3.reverse();
        Assert.assertTrue(m3.intersetcs(m3r));

        Assert.assertTrue(m1.intersetcs(m2));
        Assert.assertTrue(m2.intersetcs(m1));
        Assert.assertFalse(m1.intersetcs(m3));
        Assert.assertTrue(m1.intersetcs(m3r));
        Assert.assertFalse(m3.intersetcs(m1));
        Assert.assertTrue(m3.intersetcs(m1.reverse()));
        Assert.assertFalse(m2.intersetcs(m3));
        Assert.assertFalse(m3.intersetcs(m2));
        Assert.assertTrue(m2.intersetcs(m3r));


        Match i = m1.getIntersection(m2);
        Assert.assertTrue(((Short)i.getField(MatchType.DL_TYPE).getValue()).equals(ethType));
        // Verify intersection of IP addresses is correct
        Assert.assertTrue(((InetAddress)i.getField(MatchType.NW_SRC).getValue()).equals(ip1));
        Assert.assertNull(i.getField(MatchType.NW_SRC).getMask());

        // Empty set
        i = m2.getIntersection(m3);
        Assert.assertNull(i);

        Match m4 = new Match();
        m4.setField(MatchType.DL_TYPE, ethType);
        m4.setField(MatchType.NW_PROTO, IPProtocols.TCP.byteValue());
        m3.setField(MatchType.NW_SRC, ip4, ipm4);
        Assert.assertTrue(m4.intersetcs(m3));

        // Verify intersection of IP and IP mask addresses is correct
        Match ii = m3.getIntersection(m4);
        Assert.assertTrue(((InetAddress)ii.getField(MatchType.NW_SRC).getValue()).equals(ip4));
        Assert.assertTrue(((InetAddress)ii.getField(MatchType.NW_SRC).getMask()).equals(ipm4));

        Match m5 = new Match();
        m5.setField(MatchType.DL_TYPE, ethType);
        m3.setField(MatchType.NW_SRC, ip3, ipm3);
        m5.setField(MatchType.NW_PROTO, IPProtocols.UDP.byteValue());
        Assert.assertFalse(m5.intersetcs(m3));
        Assert.assertFalse(m5.intersetcs(m4));
        Assert.assertTrue(m5.intersetcs(m5));
        Assert.assertFalse(m3.intersetcs(m5));
        Assert.assertFalse(m4.intersetcs(m5));


        Match i2 = m4.getIntersection(m3);
        Assert.assertFalse(i2.getMatches() == 0);
        Assert.assertFalse(i2.getMatchesList().isEmpty());
        Assert.assertTrue(((InetAddress)i2.getField(MatchType.NW_SRC).getValue()).equals(ip3));
        Assert.assertTrue(((InetAddress)i2.getField(MatchType.NW_SRC).getMask()).equals(ipm3));
        Assert.assertTrue(((Byte)i2.getField(MatchType.NW_PROTO).getValue()).equals(IPProtocols.TCP.byteValue()));

        byte src[] = {(byte)0, (byte)0xab,(byte)0xbc,(byte)0xcd,(byte)0xde,(byte)0xef};
        byte dst[] = {(byte)0x10, (byte)0x11,(byte)0x12,(byte)0x13,(byte)0x14,(byte)0x15};
        Short srcPort = (short)1024;
        Short dstPort = (short)80;

        // Check identity
        Match m6 = new Match();
        m6.setField(MatchType.DL_SRC, src);
        m6.setField(MatchType.DL_DST, dst);
        m6.setField(MatchType.NW_SRC, ip2, ipm2);
        m6.setField(MatchType.NW_DST, ip3, ipm3);
        m6.setField(MatchType.NW_PROTO, IPProtocols.UDP.byteValue());
        m6.setField(MatchType.TP_SRC, srcPort);
        m6.setField(MatchType.TP_DST, dstPort);
        Assert.assertTrue(m6.intersetcs(m6));
        Assert.assertTrue(m6.getIntersection(m6).equals(m6));

        // Empty match, represents the universal set (all packets)
        Match u = new Match();
        Assert.assertTrue(m6.getIntersection(u).equals(m6));
        Assert.assertTrue(u.getIntersection(m6).equals(m6));

        // No intersection with null match, empty set
        Assert.assertNull(m6.getIntersection(null));
    }

    @Test
    public void testMetadata() {
        Property tier1 = new Tier(1);
        Property tier2 = new Tier(2);
        Property table1 = new Tables((byte)0x7f);
        Match m1 = new Match();
        List<Property> resprops = null;
        resprops = m1.getMetadatas();
        // This should be null
        Assert.assertTrue(resprops.isEmpty());
        m1.setMetadata("tier1", tier1);
        m1.setMetadata("tier2", tier2);
        m1.setMetadata("table1", table1);
        resprops = m1.getMetadatas();
        // Check for the number of elements in it
        Assert.assertTrue(resprops.size() == 3);
        // Check if the elements are in it
        Assert.assertTrue(resprops.contains(tier1));
        Assert.assertTrue(resprops.contains(tier2));
        Assert.assertTrue(resprops.contains(table1));
        // Check for single elements retrieve
        Assert.assertTrue(m1.getMetadata("tier1").equals(tier1));
        Assert.assertTrue(m1.getMetadata("tier2").equals(tier2));
        Assert.assertTrue(m1.getMetadata("table1").equals(table1));
        // Now remove an element and make sure the remaining are
        // correct
        m1.removeMetadata("tier1");

        resprops = m1.getMetadatas();
        // Check for the number of elements in it
        Assert.assertTrue(resprops.size() == 2);
        // Check if the elements are in it
        Assert.assertFalse(resprops.contains(tier1));
        Assert.assertTrue(resprops.contains(tier2));
        Assert.assertTrue(resprops.contains(table1));
        // Check for single elements retrieve
        Assert.assertTrue(m1.getMetadata("table1").equals(table1));
        Assert.assertTrue(m1.getMetadata("tier2").equals(tier2));
        Assert.assertNull(m1.getMetadata("tier1"));

        // Check for an element never existed
        Assert.assertNull(m1.getMetadata("table100"));

        // Remove them all
        m1.removeMetadata("tier2");
        m1.removeMetadata("table1");

        // Remove also a non-existent one
        m1.removeMetadata("table100");

        resprops = m1.getMetadatas();
        // Check there are no elements left
        Assert.assertTrue(resprops.size() == 0);

        // Now check for exception on setting null values
        try {
            m1.setMetadata("foo", null);
            // The line below should never be reached
            Assert.assertTrue(false);
        } catch (NullPointerException nue) {
            // NPE should be raised for null value
            Assert.assertTrue(true);
        }

        // Now check on using null key
        try {
            m1.setMetadata(null, table1);
            // The line below should never be reached
            Assert.assertTrue(false);
        } catch (NullPointerException nue) {
            // NPE should be raised for null value
            Assert.assertTrue(true);
        }

        // Now check on using null key and null value
        try {
            m1.setMetadata(null, null);
            // The line below should never be reached
            Assert.assertTrue(false);
        } catch (NullPointerException nue) {
            // NPE should be raised for null value
            Assert.assertTrue(true);
        }
    }
}
