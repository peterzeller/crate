/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package org.elasticsearch.cluster;

import io.crate.integrationtests.SQLTransportIntegrationTest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.InternalSettingsPlugin;
import org.elasticsearch.test.transport.MockTransportService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;


@ESIntegTestCase.ClusterScope(scope= ESIntegTestCase.Scope.TEST, numDataNodes=0)
public class RetentionLeaseSyncTest extends SQLTransportIntegrationTest {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        var plugins = new ArrayList<>(super.nodePlugins());
        plugins.add(InternalSettingsPlugin.class);
        plugins.add(MockTransportService.TestPlugin.class);
        return plugins;
    }

    public void testRetentionLeasesEstablishedWhenRelocatingPrimary() throws Exception {
        List<String> nodesIds = internalCluster().startNodes(5);
        final int numberOfReplicas = 1;
        execute(
            "create table test (x int, value text) clustered into 1 shards " +
            "with (number_of_replicas = ?, \"soft_deletes.enabled\" = true)",
            new Object[]{numberOfReplicas}
        );

        String tableName = getFqn("test");
        ensureGreen(tableName);
        client().admin().indices().prepareUpdateSettings(tableName).setSettings(Settings.builder().put(
            "index.soft_deletes.retention_lease.sync_interval",
            "100ms")).execute().actionGet();


        for (int i = 0; i < 10; i++) {
            execute("insert into test(x, value) values(?,?)", new Object[]{i, Integer.toString(i)});
        }
        assertBusy(() -> assertEnsurePeerRecoveryRetentionLeasesRenewedAndSynced("test"), 1, TimeUnit.MINUTES);
    }

    void assertEnsurePeerRecoveryRetentionLeasesRenewedAndSynced(String table) {
        var response = execute("""
                                          select seq_no_stats['global_checkpoint'],
                                          seq_no_stats['local_checkpoint'],
                                          seq_no_stats['max_seq_no'],
                                          retention_leases['leases']['retaining_seq_no']
                                          from sys.shards
                                          where table_name=?
                                          """, new Object[]{table});

        Long globalCheckpoint = (Long) response.rows()[0][0];
        Long localCheckpoint = (Long) response.rows()[0][1];
        Long maxSeqNo = (Long) response.rows()[0][2];
        assertThat(globalCheckpoint, is(maxSeqNo));
        assertThat(localCheckpoint, is(maxSeqNo));
        List<Long> retainingSeqNos = (List<Long>) response.rows()[0][3];
        for (Long rSeq : retainingSeqNos) {
            assertThat(rSeq, is(globalCheckpoint + 1));
        }
    }
}
