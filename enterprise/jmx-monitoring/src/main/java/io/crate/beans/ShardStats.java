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

import java.beans.ConstructorProperties;
import java.util.Objects;

public class ShardStats {

    final int shardId;
    final long size;
    final String state;

    @ConstructorProperties({"shardId", "size", "state"})
    public ShardStats(int shardId, long size, String state) {
        this.shardId = shardId;
        this.size = size;
        this.state = state;
    }

    public int getShardId() {
        return shardId;
    }

    public long getSize() {
        return size;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShardStats that = (ShardStats) o;
        return shardId == that.shardId &&
               size == that.size &&
               Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shardId, size, state);
    }

    @Override
    public String toString() {
        return "ShardStats{" +
               "shardId=" + shardId +
               ", size=" + size +
               ", state='" + state + '\'' +
               '}';
    }
}
