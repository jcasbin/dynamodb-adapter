// Copyright 2020 The casbin Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.casbin.adapter;

import org.junit.Test;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.util.Util;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

public class DynamoDBAdapterTest 
{

    private static void testGetPolicy(Enforcer e, List<List<String>> res) {
        List<List<String>> myRes = e.getPolicy();
        Util.logPrint("Policy: " + myRes);

        // The input order cannot be preserved in DynamoDB
        // Therefore, Array2DEquals cannot be used as the evaluation functionality here.
        if (!TestUtils.setOfArrayEquals(res, myRes)) {
            fail("Policy: " + myRes + ", supposed to be " + res);
        }
    }

    @Test
    public void testAdapter() {
        // Load the policy from the file adapter (.csv) first
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

        String endpoint = "http://localhost:8000";
        String region = "cn-north-1";

        DynamoDBAdapter a = new DynamoDBAdapter(endpoint, region);

        a.createTable();

        // Save the current policy to DB
        a.savePolicy(e.getModel());

        // Clear the current policy.
        e.clearPolicy();
        
        // Load the policy from DB
        a.loadPolicy(e.getModel());
        testGetPolicy(e, asList(
            asList("alice", "data1", "read"),
            asList("bob", "data2", "write"),
            asList("data2_admin", "data2", "read"),
            asList("data2_admin", "data2", "write")));
        a.dropTable();
    }
}
