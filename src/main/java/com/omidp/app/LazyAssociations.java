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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

/**
 * The Hibernate recommendation is to statically mark all associations lazy and to use dynamic fetching strategies for eagerness.
 * <p>
 * This is unfortunately at odds with the Jakarta Persistence specification which defines that all one-to-one and many-to-one associations should be eagerly fetched by default.
 * <p>
 * Hibernate, as a Jakarta Persistence provider, honors that default.
 * @author Omid
 */
public class LazyAssociations extends Recipe {

	@Override
	public String getDisplayName() {
		return "mark all associations lazy";
	}

	@Override
	public String getDescription() {
		return "mark one-to-one and many-to-one associations lazy.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return Preconditions.check(
			Preconditions.or(
				new UsesType<>("jakarta.persistence.ManyToOne", false),
				new UsesType<>("jakarta.persistence.OneToOne", false)
			),
			new LazyAssociationsVisitor()
		);
	}

	public class LazyAssociationsVisitor extends JavaIsoVisitor<ExecutionContext> {

		@Override
		public J.VariableDeclarations visitVariableDeclarations(J.VariableDeclarations multiVariable, ExecutionContext ctx) {
			J.VariableDeclarations varDecls = super.visitVariableDeclarations(multiVariable, ctx);

			return varDecls;
		}

		@Override
		public J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext ctx) {
			J.Annotation a = super.visitAnnotation(annotation, ctx);
			JavaType annType = a.getType();
			if (!(
				TypeUtils.isOfClassType(annType, "jakarta.persistence.OneToOne") ||
					TypeUtils.isOfClassType(annType, "jakarta.persistence.ManyToOne")
			)) {
				// recipe does not apply
				return a;
			}
			maybeAddImport("jakarta.persistence.FetchType", false);
			StringBuilder members = new StringBuilder();
			if ("jakarta.persistence.OneToOne".equals(a.getType().toString())) {
				members.append("@OneToOne(fetch = FetchType.LAZY");
			} else {
				members.append("@ManyToOne(fetch = FetchType.LAZY");
			}
			if (a.getArguments() != null && a.getArguments().size() > 0) {
				members.append(", ");
				int i = 0;
				for (Expression argument : a.getArguments()) {
					if ("jakarta.persistence.FetchType".equals(argument.getType().toString())) {
						continue;
					}
					if (i > 0) {
						members.append(", ");
					}
					members.append(argument.print(getCursor()).trim());
					i++;
				}
			}
			members.append(")");
			return JavaTemplate.builder(members.toString())
				.doBeforeParseTemplate(System.out::println)
				.javaParser(JavaParser.fromJavaVersion().classpathFromResources(ctx, "jakarta.persistence-api"))
				.imports("jakarta.persistence.FetchType")
				.build()
				.apply(getCursor(), a.getCoordinates().replace());

		}
	}

}
