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

package io.r2dbc.postgresql;

import io.r2dbc.postgresql.message.Format;
import io.r2dbc.postgresql.message.backend.RowDescription.Field;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class PostgresqlColumnMetadataTest {

    @Test
    public void constructorNoName() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlColumnMetadata(null, (short) 100, 200))
            .withMessage("name must not be null");
    }

    @Test
    public void constructorNoPrecision() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlColumnMetadata("test-name", null, 200))
            .withMessage("precision must not be null");
    }

    @Test
    public void constructorNoType() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlColumnMetadata("test-name", (short) 100, null))
            .withMessage("type must not be null");
    }

    @Test
    public void toColumnMetadata() {
        PostgresqlColumnMetadata columnMetadata = PostgresqlColumnMetadata.toColumnMetadata(new Field((short) 100, 200, 300, (short) 400, Format.TEXT, "test-name", 500));

        assertThat(columnMetadata.getName()).isEqualTo("test-name");
        assertThat(columnMetadata.getPrecision()).hasValue(400);
        assertThat(columnMetadata.getType()).isEqualTo(200);
    }

    @Test
    public void toColumnMetadataNoField() {
        assertThatNullPointerException().isThrownBy(() -> PostgresqlColumnMetadata.toColumnMetadata(null))
            .withMessage("field must not be null");
    }

}
