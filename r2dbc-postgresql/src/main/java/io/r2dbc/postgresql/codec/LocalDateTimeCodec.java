/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.r2dbc.postgresql.codec;

import io.r2dbc.core.nullability.Nullable;
import io.r2dbc.postgresql.client.Parameter;
import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.type.PostgresqlObjectId;
import io.r2dbc.postgresql.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.time.LocalDateTime;
import java.util.Objects;

import static io.r2dbc.postgresql.message.Format.TEXT;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.TIMESTAMP;

final class LocalDateTimeCodec extends AbstractCodec<LocalDateTime> {

    private final ByteBufAllocator byteBufAllocator;

    LocalDateTimeCodec(ByteBufAllocator byteBufAllocator) {
        super(LocalDateTime.class);
        this.byteBufAllocator = Objects.requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");
    }

    @Override
    public LocalDateTime decode(ByteBuf byteBuf, @Nullable Format format, @Nullable Class<? extends LocalDateTime> type) {
        Objects.requireNonNull(byteBuf, "byteBuf must not be null");

        return LocalDateTime.parse(ByteBufUtils.decode(byteBuf));
    }

    @Override
    public Parameter doEncode(LocalDateTime value) {
        Objects.requireNonNull(value, "value must not be null");

        ByteBuf encoded = ByteBufUtils.encode(this.byteBufAllocator, value.toString());
        return create(TEXT, TIMESTAMP, encoded);
    }

    @Override
    boolean doCanDecode(Format format, PostgresqlObjectId type) {
        Objects.requireNonNull(format, "format must not be null");
        Objects.requireNonNull(type, "type must not be null");

        return TEXT == format && TIMESTAMP == type;
    }

}
