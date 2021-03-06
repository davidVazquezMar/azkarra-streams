/*
 * Copyright 2019 StreamThoughts.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.streamthoughts.azkarra.api.query.internal;

import io.streamthoughts.azkarra.api.model.KV;
import io.streamthoughts.azkarra.api.monad.Try;
import io.streamthoughts.azkarra.api.query.LocalStoreAccessor;
import io.streamthoughts.azkarra.api.streams.KafkaStreamsContainer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KeyValueGetQueryTest {

    public static final String STORE_NAME = "storeName";

    @Test
    public void shouldGetGivenKey() {
        KeyValueGetQuery<String, String> query = new KeyValueGetQuery<>(STORE_NAME, "key", Serdes.String().serializer());
        KafkaStreamsContainer mkContainer = Mockito.mock(KafkaStreamsContainer.class);

        ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(store.get("key")).thenReturn("value");
        when(mkContainer.getLocalKeyValueStore(matches(STORE_NAME))).thenReturn(new LocalStoreAccessor<>(() -> store));

        Try<List<KV<String, String>>> result = query.execute(mkContainer);
        Assertions.assertEquals(1, result.get().size());
        Assertions.assertEquals(KV.of("key", "value"), result.get().get(0));
    }
}