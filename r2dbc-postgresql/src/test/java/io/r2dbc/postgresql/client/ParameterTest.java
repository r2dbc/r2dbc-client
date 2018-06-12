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

package io.r2dbc.postgresql.client;

import org.junit.Test;

import static io.r2dbc.postgresql.message.Format.TEXT;
import static io.r2dbc.postgresql.util.TestByteBufAllocator.TEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class ParameterTest {

    @Test
    public void constructorNoFormat() {
        assertThatNullPointerException().isThrownBy(() -> new Parameter(null, 100, null))
            .withMessage("format must not be null");
    }

    @Test
    public void constructorNoType() {
        assertThatNullPointerException().isThrownBy(() -> new Parameter(TEXT, null, null))
            .withMessage("type must not be null");
    }

    @Test
    public void getters() {
        Parameter parameter = new Parameter(TEXT, 100, TEST.buffer(4).writeInt(200));

        assertThat(parameter.getFormat()).isEqualTo(TEXT);
        assertThat(parameter.getType()).isEqualTo(100);
        assertThat(parameter.getValue()).isEqualTo(TEST.buffer(4).writeInt(200));
    }

}
