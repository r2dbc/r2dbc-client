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
import io.netty.buffer.ByteBuf;

import static io.r2dbc.postgresql.message.Format.BINARY;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.UNSPECIFIED;

final class NullCodec implements Codec<Object> {

    @Override
    public boolean canDecode(@Nullable ByteBuf byteBuf, int dataType, @Nullable Format format, @Nullable Class<?> type) {
        return byteBuf == null;
    }

    @Override
    public boolean canEncode(@Nullable Object value) {
        return value == null;
    }

    @Nullable
    @Override
    public Object decode(@Nullable ByteBuf byteBuf, @Nullable Format format, @Nullable Class<?> type) {
        return null;
    }

    @Override
    public Parameter encode(@Nullable Object value) {
        return new Parameter(BINARY, UNSPECIFIED.getObjectId(), null);
    }

}
