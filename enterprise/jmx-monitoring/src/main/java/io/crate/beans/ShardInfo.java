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

package io.crate.beans;

import com.carrotsearch.hppc.cursors.IntObjectCursor;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.routing.IndexRoutingTable;
import org.elasticsearch.cluster.routing.IndexShardRoutingTable;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.common.collect.ImmutableOpenIntMap;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.shard.IndexShard;
import org.elasticsearch.index.store.StoreStats;
import org.elasticsearch.indices.IndicesService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ShardInfo implements ShardInfoMXBean {

    public static final String NAME = "io.crate.monitoring:type=ShardInfo";

    private final Supplier<ClusterState> clusterState;
    private final Supplier<DiscoveryNode> localNode;
    private final IndicesService indicesService;

    public ShardInfo(Supplier<ClusterState> clusterState, Supplier<DiscoveryNode> localNode, IndicesService indicesService) {
        this.clusterState = clusterState;
        this.localNode = localNode;
        this.indicesService = indicesService;
    }

    @Override
    public int getNumberOfShards() {
        int numberOfShards = 0;
        var discoveryNode = localNode.get();
        var state = clusterState.get();
        RoutingTable routingTable = state.getRoutingTable();
        if (routingTable != null) {
            List<ShardRouting> shardRoutings = routingTable.allShards();
            for (ShardRouting shardRouting : shardRoutings) {
                String currentNodeId = shardRouting.currentNodeId();
                if (discoveryNode.getId().equals(currentNodeId)) {
                    numberOfShards++;
                }
            }
        }
        return numberOfShards;
    }

    @Override
    public int getNumberOfPrimaryShards() {
        int numberOfPrimaryShards = 0;
        var discoveryNode = localNode.get();
        var state = clusterState.get();
        RoutingTable routingTable = state.getRoutingTable();
        if (routingTable != null) {
            ImmutableOpenMap<String, IndexRoutingTable> indicesRouting = routingTable.getIndicesRouting();
            for (ObjectObjectCursor<String, IndexRoutingTable> indexRouting : indicesRouting) {
                IndexRoutingTable indexRoutingTable = indexRouting.value;
                ImmutableOpenIntMap<IndexShardRoutingTable> shards = indexRoutingTable.getShards();
                for (IntObjectCursor<IndexShardRoutingTable> shard : shards) {
                    IndexShardRoutingTable value = shard.value;
                    List<ShardRouting> shards1 = value.getShards();
                    for (ShardRouting shardRouting : shards1) {
                        if (discoveryNode.getId().equals(shardRouting.currentNodeId())) {
                            if (shardRouting.primary()) {
                                numberOfPrimaryShards++;
                            }
                        }
                    }
                }
            }
        }
        return numberOfPrimaryShards;
    }

    @Override
    public int getNumberOfReplicas() {
        int numberOfReplicas = 0;
        var discoveryNode = localNode.get();
        var state = clusterState.get();
        RoutingTable routingTable = state.getRoutingTable();
        if (routingTable != null) {
            ImmutableOpenMap<String, IndexRoutingTable> indicesRouting = routingTable.getIndicesRouting();
            for (ObjectObjectCursor<String, IndexRoutingTable> indexRouting : indicesRouting) {
                IndexRoutingTable indexRoutingTable = indexRouting.value;
                ImmutableOpenIntMap<IndexShardRoutingTable> shards = indexRoutingTable.getShards();
                for (IntObjectCursor<IndexShardRoutingTable> shard : shards) {
                    IndexShardRoutingTable value = shard.value;
                    List<ShardRouting> shards1 = value.getShards();
                    for (ShardRouting shardRouting : shards1) {
                        if (discoveryNode.getId().equals(shardRouting.currentNodeId())) {
                            if (!shardRouting.primary()) {
                                numberOfReplicas++;
                            }
                        }
                    }
                }
            }
        }
        return numberOfReplicas;
    }

    @Override
    public List<ShardStats> getShardStats() {
        var result = new ArrayList<ShardStats>();
        var discoveryNode = localNode.get();
        var state = clusterState.get();
        RoutingTable routingTable = state.getRoutingTable();
        if (routingTable != null) {
            ImmutableOpenMap<String, IndexRoutingTable> indicesRouting = routingTable.getIndicesRouting();
            for (ObjectObjectCursor<String, IndexRoutingTable> indexRouting : indicesRouting) {
                IndexRoutingTable indexRoutingTable = indexRouting.value;
                ImmutableOpenIntMap<IndexShardRoutingTable> shards = indexRoutingTable.getShards();
                for (IntObjectCursor<IndexShardRoutingTable> shard : shards) {
                    IndexShardRoutingTable value = shard.value;
                    List<ShardRouting> shards1 = value.getShards();
                    for (ShardRouting shardRouting : shards1) {
                        if (discoveryNode.getId().equals(shardRouting.currentNodeId())) {
                                var shardStats = new ShardStats(shardRouting.id(), getShardSize(shardRouting.id()), shardRouting.state().toString());
                                result.add(shardStats);
                        }
                    }
                }
            }
        }
        return result;
    }

    private long getShardSize(int shardId) {
        for (IndexService indexService : indicesService) {
            IndexShard shard = indexService.getShardOrNull(shardId);
            if (shard != null) {
                StoreStats storeStats = shard.storeStats();
                return storeStats.getSizeInBytes();
            }
        }
        return -1;
    }
}
