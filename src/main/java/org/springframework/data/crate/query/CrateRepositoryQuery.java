/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package org.springframework.data.crate.query;

import com.google.common.base.Optional;
import org.springframework.data.crate.core.CrateAction;
import org.springframework.data.crate.core.CrateActionResponseHandler;
import org.springframework.data.crate.core.CrateOperations;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;

import java.util.List;
import java.util.Locale;

import static org.springframework.util.Assert.isTrue;

public class CrateRepositoryQuery implements RepositoryQuery {

    private final CrateActionResponseHandler handler;
    private final CrateQueryMethod queryMethod;
    private final CrateOperations crateOperations;
    private final String query;

    private CrateRepositoryQuery(String query, CrateQueryMethod queryMethod, CrateOperations crateOperations) {
        this.queryMethod = queryMethod;
        this.crateOperations = crateOperations;
        this.query = query;
        this.handler = new SimpleQueryCrateHandler<>(queryMethod.getReturnedObjectType());
    }

    @Override
    public QueryMethod getQueryMethod() {
        return queryMethod;
    }

    @Override
    public Object execute(Object[] parameters) {
        CrateAction action = new SimpleQueryCrateAction(query);
        if (queryMethod.isCollectionQuery()) {
            return crateOperations.execute(action, handler);
        } else {
            return getSingleResult((List<?>) crateOperations.execute(action, handler));
        }
    }

    public String getSource() {
        return query;
    }

    private Object getSingleResult(List<?> results) {
        isTrue(results.size() <= 1, String.format(Locale.ENGLISH,
                "Select statement should return only one entity %d for query: %s", results.size(), query));
        return results.size() == 1 ? results.get(0) : null;
    }


    public static CrateRepositoryQuery buildFromAnnotation(CrateQueryMethod queryMethod, CrateOperations crateOperations) {
        if(queryMethod.getAnnotatedQuery().isPresent()) {
            return new CrateRepositoryQuery(queryMethod.getAnnotatedQuery().get(), queryMethod, crateOperations);
        }
        throw new IllegalArgumentException("cannot create annotated query if annotation doesn't contain a query");
    }

}