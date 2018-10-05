/*
 * Copyright 2018 the original author or authors.
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
package io.r2dbc.client;

import static io.r2dbc.spi.Mutability.*;

import javax.sql.DataSource;

import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.Mutability;
import io.r2dbc.spi.R2dbcException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import com.zaxxer.hikari.HikariDataSource;

/**
 * @author Greg Turnquist
 */
final class H2Example implements Example<String> {

	private static final String DATABASE_NAME = "mem:r2dbc-client-examples";
	private static final String JDBC_CONNECTION_URL = "jdbc:h2:" + DATABASE_NAME;

	@RegisterExtension
	static final H2ServerExtension SERVER = new H2ServerExtension(JDBC_CONNECTION_URL);

	private final H2ConnectionFactory connectionFactory = new H2ConnectionFactory(Mono.defer(() -> Mono.just(DATABASE_NAME)));

	private final R2dbc r2dbc = new R2dbc(connectionFactory);

	@Override
	public String getIdentifier(int index) {
		return getPlaceholder(index);
	}

	@Override
	public JdbcOperations getJdbcOperations() {
		return SERVER.getJdbcOperations();
	}

	@Override
	public String getPlaceholder(int index) {
		return String.format("$%d", index + 1);
	}

	@Override
	public R2dbc getR2dbc() {
		return this.r2dbc;
	}

	@Test
	@Disabled("connectionMutability not yet merged to master branch in r2dbc-h2")
	@Override
	public void connectionMutability() {

		getR2dbc()
			.useHandle(handle -> Mono.from(handle

				.setTransactionMutability(READ_ONLY))
				.thenMany(handle.execute(String.format("INSERT INTO test VALUES (%s)", getPlaceholder(0)), 200)))

			.as(StepVerifier::create)
			.verifyError(R2dbcException.class);

		// Switch to READ_WRITE to support test cleanup.

		this.connectionFactory.create()
			.flatMapMany(connection -> Mono.from(connection.setTransactionMutability(Mutability.READ_WRITE)))
			.as(StepVerifier::create)
			.verifyComplete();
	}

	@Test
	@Disabled("transactionMutability not yet merged to master branch in r2dbc-h2")
	@Override
	public void transactionMutability() {

		getR2dbc()
			.inTransaction(handle -> Mono.from(handle

				.setTransactionMutability(READ_ONLY))
				.thenMany(handle.execute(String.format("INSERT INTO test VALUES (%s)", getPlaceholder(0)), 200)))

			.as(StepVerifier::create)
			.verifyError(R2dbcException.class);

		// Switch to READ_WRITE to support test cleanup.

		this.connectionFactory.create()
			.flatMapMany(connection -> Mono.from(connection.setTransactionMutability(Mutability.READ_WRITE)))
			.as(StepVerifier::create)
			.verifyComplete();
	}

	private static final class H2ServerExtension implements BeforeAllCallback, AfterAllCallback {

		private static final Logger log = LoggerFactory.getLogger(H2ServerExtension.class);

		private final String connectionUrl;

		private JdbcOperations jdbcOperations;

		public H2ServerExtension(String connectionUrl) {
			this.connectionUrl = connectionUrl;
		}

		@Override
		public void beforeAll(ExtensionContext context) {

			log.info("H2 database starting...");

			DataSource anotherDataSource = DataSourceBuilder.create()
				.type(HikariDataSource.class)
				.driverClassName("org.h2.Driver").url(this.connectionUrl)
				.username("sa").password("")
				.build();

			this.jdbcOperations = new JdbcTemplate(anotherDataSource);
		}

		@Override
		public void afterAll(ExtensionContext context) {
			log.info("H2 database stopping...");
		}

		JdbcOperations getJdbcOperations() {
			return jdbcOperations;
		}
	}
}
