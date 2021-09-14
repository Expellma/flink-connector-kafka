/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.connector.kafka.source;

import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.connector.kafka.source.reader.deserializer.KafkaRecordDeserializationSchema;
import org.apache.flink.util.TestLogger;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Tests for {@link KafkaSourceBuilder}. */
public class KafkaSourceBuilderTest extends TestLogger {

    @Test
    public void testBuildSourceWithGroupId() {
        final KafkaSource<String> kafkaSource = getBasicBuilder().setGroupId("groupId").build();
        // Commit on checkpoint should be enabled by default
        Assertions.assertTrue(
                kafkaSource
                        .getConfiguration()
                        .get(KafkaSourceOptions.COMMIT_OFFSETS_ON_CHECKPOINT));
        // Auto commit should be disabled by default
        Assertions.assertFalse(
                kafkaSource
                        .getConfiguration()
                        .get(
                                ConfigOptions.key(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
                                        .booleanType()
                                        .noDefaultValue()));
    }

    @Test
    public void testBuildSourceWithoutGroupId() {
        final KafkaSource<String> kafkaSource = getBasicBuilder().build();
        // Commit on checkpoint and auto commit should be disabled because group.id is not specified
        Assertions.assertFalse(
                kafkaSource
                        .getConfiguration()
                        .get(KafkaSourceOptions.COMMIT_OFFSETS_ON_CHECKPOINT));
        Assertions.assertFalse(
                kafkaSource
                        .getConfiguration()
                        .get(
                                ConfigOptions.key(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)
                                        .booleanType()
                                        .noDefaultValue()));
    }

    @Test
    public void testEnableCommitOnCheckpointWithoutGroupId() {
        final IllegalStateException exception =
                Assert.assertThrows(
                        IllegalStateException.class,
                        () ->
                                getBasicBuilder()
                                        .setProperty(
                                                KafkaSourceOptions.COMMIT_OFFSETS_ON_CHECKPOINT
                                                        .key(),
                                                "true")
                                        .build());
        MatcherAssert.assertThat(
                exception.getMessage(),
                CoreMatchers.containsString(
                        "Property group.id is required when offset commit is enabled"));
    }

    @Test
    public void testEnableAutoCommitWithoutGroupId() {
        final IllegalStateException exception =
                Assert.assertThrows(
                        IllegalStateException.class,
                        () ->
                                getBasicBuilder()
                                        .setProperty(
                                                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
                                        .build());
        MatcherAssert.assertThat(
                exception.getMessage(),
                CoreMatchers.containsString(
                        "Property group.id is required when offset commit is enabled"));
    }

    @Test
    public void testDisableOffsetCommitWithoutGroupId() {
        getBasicBuilder()
                .setProperty(KafkaSourceOptions.COMMIT_OFFSETS_ON_CHECKPOINT.key(), "false")
                .build();
        getBasicBuilder().setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false").build();
    }

    private KafkaSourceBuilder<String> getBasicBuilder() {
        return new KafkaSourceBuilder<String>()
                .setBootstrapServers("testServer")
                .setTopics("topic")
                .setDeserializer(
                        KafkaRecordDeserializationSchema.valueOnly(StringDeserializer.class));
    }
}
