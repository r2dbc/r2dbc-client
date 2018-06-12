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

import java.util.Objects;

import static io.r2dbc.postgresql.message.Format.BINARY;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.FLOAT8;

final class DoubleCodec extends AbstractCodec<Double> {

    private final ByteBufAllocator byteBufAllocator;

    DoubleCodec(ByteBufAllocator byteBufAllocator) {
        super(Double.class);
        this.byteBufAllocator = Objects.requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");
    }

    @Override
    public Double decode(ByteBuf byteBuf, Format format, @Nullable Class<? extends Double> type) {
        Objects.requireNonNull(byteBuf, "byteBuf must not be null");
        Objects.requireNonNull(format, "format must not be null");

        if (BINARY == format) {
            return byteBuf.readDouble();
        } else {
            return Double.parseDouble(ByteBufUtils.decode(byteBuf));
        }
    }

    @Override
    public Parameter doEncode(Double value) {
        Objects.requireNonNull(value, "value must not be null");

        ByteBuf encoded = this.byteBufAllocator.buffer(8).writeDouble(value);
        return create(BINARY, FLOAT8, encoded);
    }

    @Override
    boolean doCanDecode(@Nullable Format format, PostgresqlObjectId type) {
        Objects.requireNonNull(type, "type must not be null");

        return FLOAT8 == type;
    }

}
