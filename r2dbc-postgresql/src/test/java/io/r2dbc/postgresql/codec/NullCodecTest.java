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

import static io.r2dbc.postgresql.message.Format.BINARY;
import static io.r2dbc.postgresql.type.PostgresqlObjectId.UNSPECIFIED;
import static org.assertj.core.api.Assertions.assertThat;

public final class NullCodecTest {

    @Test
    public void canEncode() {
        assertThat(new NullCodec().canEncode(null)).isTrue();
        assertThat(new NullCodec().canEncode(new Object())).isFalse();
    }

    @Test
    public void encode() {
        assertThat(new NullCodec().encode(null)).isEqualTo(new Parameter(BINARY, UNSPECIFIED.getObjectId(), null));
    }

}
