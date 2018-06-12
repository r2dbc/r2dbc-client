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

package io.r2dbc.spi;

import java.util.Optional;

/**
 * Represents the metadata for a column of the results returned from a query.
 */
public interface ColumnMetadata {

    /**
     * Returns the name of the column.
     *
     * @return the name of the column
     */
    String getName();

    /**
     * Returns the precisions of the column.
     *
     * @return the precision of the column
     */
    Optional<Integer> getPrecision();

    /**
     * Returns the type of the column.
     *
     * @return the type of the column
     */
    Integer getType();

}
