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

import io.r2dbc.postgresql.client.Parameter;
import org.junit.Test;

import java.time.ZonedDateTime;

import static io.r2dbc.postgresql.message.Format.BINARY;
import static io.r2dbc.postgresql.message.Format.TEXT;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.MONEY;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.TIMESTAMPTZ;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.VARCHAR;
import static io.r2dbc.postgresql.util.ByteBufUtils.encode;
import static io.r2dbc.postgresql.util.TestByteBufAllocator.TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class ZonedDateTimeCodecTest {

    @Test
    public void constructorNoByteBufAllocator() {
        assertThatNullPointerException().isThrownBy(() -> new ZonedDateTimeCodec(null))
            .withMessage("byteBufAllocator must not be null");
    }

    @Test
    public void decode() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        assertThat(new ZonedDateTimeCodec(TEST).decode(encode(TEST, zonedDateTime.toString()), TEXT, ZonedDateTime.class))
            .isEqualTo(zonedDateTime);
    }

    @Test
    public void decodeNoByteBuf() {
        assertThatNullPointerException().isThrownBy(() -> new ZonedDateTimeCodec(TEST).decode(null, TEXT, ZonedDateTime.class))
            .withMessage("byteBuf must not be null");
    }

    @Test
    public void doCanDecode() {
        ZonedDateTimeCodec codec = new ZonedDateTimeCodec(TEST);

        assertThat(codec.doCanDecode(BINARY, TIMESTAMPTZ)).isFalse();
        assertThat(codec.doCanDecode(TEXT, MONEY)).isFalse();
        assertThat(codec.doCanDecode(TEXT, TIMESTAMPTZ)).isTrue();
    }

    @Test
    public void doCanDecodeNoFormat() {
        assertThatNullPointerException().isThrownBy(() -> new ZonedDateTimeCodec(TEST).doCanDecode(null, VARCHAR))
            .withMessage("format must not be null");
    }

    @Test
    public void doCanDecodeNoType() {
        assertThatNullPointerException().isThrownBy(() -> new ZonedDateTimeCodec(TEST).doCanDecode(TEXT, null))
            .withMessage("type must not be null");
    }

    @Test
    public void doEncode() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        assertThat(new ZonedDateTimeCodec(TEST).doEncode(zonedDateTime))
            .isEqualTo(new Parameter(TEXT, TIMESTAMPTZ.getObjectId(), encode(TEST, zonedDateTime.toString())));
    }

    @Test
    public void doEncodeNoValue() {
        assertThatNullPointerException().isThrownBy(() -> new ZonedDateTimeCodec(TEST).doEncode(null))
            .withMessage("value must not be null");
    }

}
