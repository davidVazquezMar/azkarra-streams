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
import io.streamthoughts.azkarra.api.monad.Reader;
import io.streamthoughts.azkarra.api.monad.Try;
import io.streamthoughts.azkarra.api.query.LocalStoreAccessor;
import io.streamthoughts.azkarra.api.query.LocalStoreQuery;
import io.streamthoughts.azkarra.api.query.StoreOperation;
import io.streamthoughts.azkarra.api.query.StoreType;
import io.streamthoughts.azkarra.api.streams.KafkaStreamsContainer;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import java.time.Instant;
import java.util.List;

public class TimestampedWindowFetchKeyRangeQuery<K, V> implements LocalStoreQuery<Windowed<K>, V> {

    private final String store;
    private final K keyFrom;
    private final K keyTo;
    private final Instant timeFrom;
    private final Instant timeTo;

    /**
     * Creates a new {@link TimestampedWindowFetchKeyRangeQuery} instance.
     *
     * @param storeName     the name of the store.
     * @param keyFrom       the query param key from.
     * @param keyTo         the query param key to.
     * @param timeFrom      the query param time from.
     * @param timeTo        the query param time to.
     */
    TimestampedWindowFetchKeyRangeQuery(final String storeName,
                                        final K keyFrom,
                                        final K keyTo,
                                        final Instant timeFrom,
                                        final Instant timeTo) {
        this.store = storeName;
        this.keyFrom = keyFrom;
        this.keyTo = keyTo;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreType storeType() {
        return StoreType.TIMESTAMPED_WINDOW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StoreOperation operationType() {
        return StoreOperation.FETCH_KEY_RANGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Try<List<KV<Windowed<K>, V>>> execute(final KafkaStreamsContainer container, final long limit) {

        final LocalStoreAccessor<ReadOnlyWindowStore<K, ValueAndTimestamp<V>>> accessor =
                container.getLocalTimestampedWindowStore(store);

        final Reader<ReadOnlyWindowStore<K, ValueAndTimestamp<V>>, List<KV<Windowed<K>, V>>> reader =
            reader(keyFrom, keyTo, timeFrom, timeTo)
            .map(iterator -> LocalStoreQuery.toKeyValueAndTimestampListAndClose(iterator, limit));

        return new LocalStoreQueryExecutor<>(accessor).execute(reader);
    }

    private Reader<ReadOnlyWindowStore<K, ValueAndTimestamp<V>>, KeyValueIterator<Windowed<K>, ValueAndTimestamp<V>>>
        reader(final K keyFrom, final K keyTo, final Instant timeFrom, final Instant timeTo) {
        return Reader.of(store -> store.fetch(keyFrom, keyTo, timeFrom, timeTo));
    }
}
