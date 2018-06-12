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

package io.r2dbc.examples;

import io.r2dbc.core.R2dbc;
import io.r2dbc.core.Update;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.PostgresqlServerErrorException;
import io.r2dbc.spi.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static io.r2dbc.spi.Mutability.READ_ONLY;

public final class CoreExamples {

    @ClassRule
    public static final PostgresqlServerResource SERVER = new PostgresqlServerResource();

    private final PostgresqlConnectionConfiguration configuration = PostgresqlConnectionConfiguration.builder()
        .database(SERVER.getDatabase())
        .host(SERVER.getHost())
        .port(SERVER.getPort())
        .password(SERVER.getPassword())
        .username(SERVER.getUsername())
        .build();

    private final R2dbc r2dbc = new R2dbc(new PostgresqlConnectionFactory(this.configuration));

    @Test
    public void batch() {
        SERVER.getJdbcOperations().execute("INSERT INTO test VALUES (100)");

        this.r2dbc
            .withHandle(handle -> handle

                .createBatch()
                .add("INSERT INTO test VALUES(200)")
                .add("SELECT value FROM test")
                .mapResult(Mono::just))

            .as(StepVerifier::create)
            .expectNextCount(3)  // TODO: Decrease by 1 when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    @Test
    public void compoundStatement() {
        SERVER.getJdbcOperations().execute("INSERT INTO test VALUES (100)");

        this.r2dbc
            .withHandle(handle -> handle

                .createQuery("SELECT value FROM test; SELECT value FROM test")
                .mapResult(CoreExamples::extractColumns))

            .as(StepVerifier::create)
            .expectNext(Collections.singletonList(100))
            .expectNext(Collections.singletonList(100))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    @Test
    public void connectionMutability() {
        this.r2dbc
            .useHandle(handle -> Mono.from(handle

                .setTransactionMutability(READ_ONLY))
                .thenMany(handle.execute("INSERT INTO test VALUES ($1)", 200)))

            .as(StepVerifier::create)
            .verifyError(PostgresqlServerErrorException.class);
    }

    @Before
    public void createTable() {
        SERVER.getJdbcOperations().execute("CREATE TABLE test ( value INTEGER )");
    }

    @After
    public void dropTable() {
        SERVER.getJdbcOperations().execute("DROP TABLE test");
    }

    @Test
    public void generatedKeys() {
        SERVER.getJdbcOperations().execute("CREATE TABLE test2 (id SERIAL PRIMARY KEY, value INTEGER)");

        this.r2dbc
            .withHandle(handle -> handle

                .createUpdate("INSERT INTO test2(value) VALUES ($1)")
                .bind("$1", 100)
                .add()
                .bind("$1", 200)
                .add()
                .executeReturningGeneratedKeys()
                .flatMap(resultBearing -> resultBearing
                    .mapResult(CoreExamples::extractIds)))

            .as(StepVerifier::create)
            .expectNext(Collections.singletonList(1))
            .expectNext(Collections.singletonList(2))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    @Test
    public void prepareStatement() {
        this.r2dbc
            .withHandle(handle -> {
                Update update = handle.createUpdate("INSERT INTO test VALUES($1)");

                IntStream.range(0, 10)
                    .forEach(i -> update
                        .bind("$1", i)
                        .add());

                return update.execute();
            })
            .as(StepVerifier::create)
            .expectNextCount(10)
            .verifyComplete();
    }

    @Test
    public void savePoint() {
        SERVER.getJdbcOperations().execute("INSERT INTO test VALUES (100)");

        this.r2dbc
            .withHandle(handle -> handle
                .inTransaction(h1 -> h1
                    .select("SELECT value FROM test")
                    .<Object>mapResult(CoreExamples::extractColumns)

                    .concatWith(h1.execute("INSERT INTO test VALUES ($1)", 200))
                    .concatWith(h1.select("SELECT value FROM test")
                        .mapResult(CoreExamples::extractColumns))

                    .concatWith(h1.createSavepoint("test_savepoint"))
                    .concatWith(h1.execute("INSERT INTO test VALUES ($1)", 300))
                    .concatWith(h1.select("SELECT value FROM test")
                        .mapResult(CoreExamples::extractColumns))

                    .concatWith(h1.rollbackTransactionToSavepoint("test_savepoint"))
                    .concatWith(h1.select("SELECT value FROM test")
                        .mapResult(CoreExamples::extractColumns))))

            .as(StepVerifier::create)
            .expectNext(Collections.singletonList(100))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(1)
            .expectNext(Arrays.asList(100, 200))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(1)
            .expectNext(Arrays.asList(100, 200, 300))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(Arrays.asList(100, 200))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    @Test
    public void transactionCommit() {
        SERVER.getJdbcOperations().execute("INSERT INTO test VALUES (100)");

        this.r2dbc
            .withHandle(handle -> handle
                .inTransaction(h1 -> h1
                    .select("SELECT value FROM test")
                    .<Object>mapResult(CoreExamples::extractColumns)

                    .concatWith(h1.execute("INSERT INTO test VALUES ($1)", 200))
                    .concatWith(h1.select("SELECT value FROM test")
                        .mapResult(CoreExamples::extractColumns)))

                .concatWith(handle.select("SELECT value FROM test")
                    .mapResult(CoreExamples::extractColumns)))

            .as(StepVerifier::create)
            .expectNext(Collections.singletonList(100))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(1)
            .expectNext(Arrays.asList(100, 200))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(Arrays.asList(100, 200))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    @Test
    public void transactionMutability() {
        this.r2dbc
            .inTransaction(handle -> Mono.from(handle

                .setTransactionMutability(READ_ONLY))
                .thenMany(handle.execute("INSERT INTO test VALUES ($1)", 200)))

            .as(StepVerifier::create)
            .verifyError(PostgresqlServerErrorException.class);
    }

    @Test
    public void transactionRollback() {
        SERVER.getJdbcOperations().execute("INSERT INTO test VALUES (100)");

        this.r2dbc
            .withHandle(handle -> handle
                .inTransaction(h1 -> h1
                    .select("SELECT value FROM test")
                    .<Object>mapResult(CoreExamples::extractColumns)

                    .concatWith(h1.execute("INSERT INTO test VALUES ($1)", 200))
                    .concatWith(h1.select("SELECT value FROM test")
                        .mapResult(CoreExamples::extractColumns))

                    .concatWith(Mono.error(new Exception())))

                .onErrorResume(t -> handle.select("SELECT value FROM test")
                    .mapResult(CoreExamples::extractColumns)))

            .as(StepVerifier::create)
            .expectNext(Collections.singletonList(100))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(1)
            .expectNext(Arrays.asList(100, 200))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .expectNext(Collections.singletonList(100))
            .expectNextCount(1)  // TODO: Remove when https://github.com/reactor/reactor-core/issues/1033
            .verifyComplete();
    }

    private static Mono<List<Integer>> extractColumns(Result result) {
        return Flux.from(result
            .map((row, rowMetadata) -> row.get("value", Integer.class)))
            .collectList();
    }

    private static Mono<List<Integer>> extractIds(Result result) {
        return Flux.from(result
            .map((row, rowMetadata) -> row.get("id", Integer.class)))
            .collectList();
    }

}
