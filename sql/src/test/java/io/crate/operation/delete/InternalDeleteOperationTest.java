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

package io.crate.operation.delete;


import io.crate.analyze.WhereClause;
import io.crate.integrationtests.SQLTransportIntegrationTest;
import io.crate.operation.operator.EqOperator;
import io.crate.planner.symbol.Literal;
import io.crate.test.integration.CrateIntegrationTest;
import io.crate.testing.TestingHelpers;
import io.crate.types.DataTypes;
import org.junit.Test;

import static org.hamcrest.Matchers.is;

@CrateIntegrationTest.ClusterScope(scope = CrateIntegrationTest.Scope.SUITE, numNodes = 1)
public class InternalDeleteOperationTest extends SQLTransportIntegrationTest {


    @Test
    public void testDeleteOperationWithQuery() throws Exception {
        execute("create table t (name string) clustered into 1 shards with (number_of_replicas = 0)");
        ensureYellow();

        execute("insert into t (name) values ('Marvin'), ('Arthur')");
        execute("refresh table t");

        WhereClause whereClause = TestingHelpers.whereClause(
                EqOperator.NAME,
                TestingHelpers.createReference("name", DataTypes.STRING),
                Literal.newLiteral("Marvin"));

        DeleteOperation deleteOperation = cluster().getInstance(DeleteOperation.class);
        long deleted = deleteOperation.delete("t", 0, whereClause);
        assertThat(deleted, is(1L));
    }
}