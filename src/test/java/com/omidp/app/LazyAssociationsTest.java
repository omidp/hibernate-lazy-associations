/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.omidp.app;

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

/**
 * @author Omid
 */
public class LazyAssociationsTest implements RewriteTest {

	@Override
	public void defaults(RecipeSpec spec) {
		spec.recipe(new LazyAssociations())
			.parser(JavaParser.fromJavaVersion().classpath("jakarta.persistence-api"));
	}

	@Test
	void transformManyToOneTestNoFetchType() {
		rewriteRun(
			java(
				"""
					import jakarta.persistence.Entity;
					import jakarta.persistence.Id;
					import jakarta.persistence.JoinColumn;
					import jakarta.persistence.ManyToOne;
					import java.util.UUID;
					public class TestEntity {
					private UUID id;
					@ManyToOne
					private Object manyToOne;
					}
					""",
				"""
					import jakarta.persistence.*;
					
					import java.util.UUID;
					
					public class TestEntity {
					private UUID id;
					@ManyToOne(fetch = FetchType.LAZY)
					private Object manyToOne;
					}
					"""
			)
		);
	}

	@Test
	void transformManyToOneTestFetchTypeEager() {
		rewriteRun(
			java(
				"""
					import jakarta.persistence.Entity;
					import jakarta.persistence.Id;
					import jakarta.persistence.JoinColumn;
					import jakarta.persistence.ManyToOne;
					import java.util.UUID;
					public class TestEntity {
					private UUID id;
					@ManyToOne(fetch = FetchType.EAGER)
					private Object manyToOne;
					}
					""",
				"""
					import jakarta.persistence.*;
					
					import java.util.UUID;
					
					public class TestEntity {
					private UUID id;
					@ManyToOne(fetch = FetchType.LAZY)
					private Object manyToOne;
					}
					"""
			)
		);
	}

	@Test
	void transformManyToOneTestFetchTypeWithProps() {
		rewriteRun(
			java(
				"""
					import jakarta.persistence.Entity;
					import jakarta.persistence.Id;
					import jakarta.persistence.JoinColumn;
					import jakarta.persistence.ManyToOne;
					import java.util.UUID;
					public class TestEntity {
					private UUID id;
					@ManyToOne(cascade = CascadeType.ALL, optional = false)
					private Object manyToOne;
					}
					""",
				"""
					import jakarta.persistence.*;
					
					import java.util.UUID;
					
					public class TestEntity {
					private UUID id;
					@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
					private Object manyToOne;
					}
					"""
			)
		);
	}

	@Test
	void transformOneToOneTestFetchTypeWithProps() {
		rewriteRun(
			java(
				"""
					import jakarta.persistence.Entity;
					import jakarta.persistence.Id;
					import jakarta.persistence.JoinColumn;
					import jakarta.persistence.OneToOne;
					import java.util.UUID;
					public class TestEntity {
					private UUID id;
					@OneToOne(cascade = CascadeType.ALL, optional = false)
					private Object manyToOne;
					}
					""",
				"""
					import jakarta.persistence.*;
					
					import java.util.UUID;
					
					public class TestEntity {
					private UUID id;
					@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)
					private Object manyToOne;
					}
					"""
			)
		);
	}



}
