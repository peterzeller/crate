/*
 * This file is part of a module with proprietary Enterprise Features.
 *
 * Licensed to Crate.io Inc. ("Crate.io") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * To use this file, Crate.io must have given you permission to enable and
 * use such Enterprise Features and you must have a valid Enterprise or
 * Subscription Agreement with Crate.io.  If you enable or use the Enterprise
 * Features, you represent and warrant that you have a valid Enterprise or
 * Subscription Agreement with Crate.io.  Your use of the Enterprise Features
 * if governed by the terms and conditions of your Enterprise or Subscription
 * Agreement with Crate.io.
 */

package io.crate.auth.user;

import com.google.common.collect.Lists;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;

public class PrivilegesResponseTest extends ESTestCase {

    @Test
    public void testStreaming() throws Exception {
        List<String> unknownUsers = Lists.newArrayList("ford", "arthur");
        long affectedRows = 1L;
        PrivilegesResponse r1 = new PrivilegesResponse(true, affectedRows, unknownUsers);

        BytesStreamOutput out = new BytesStreamOutput();
        r1.writeTo(out);

        PrivilegesResponse r2 = new PrivilegesResponse(out.bytes().streamInput());

        assertThat(r2.isAcknowledged(), is(true));
        assertThat(r2.affectedRows(), is(1L));
        assertThat(r2.unknownUserNames(), is(unknownUsers));
    }
}

