// Copyright 2018 The casbin Authors. All Rights Reserved.
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
import org.junit.Assert;

public class DynamoDBAdapterTest 
{
    @Test
    public void testAdapter() {
        // Load the policy from the file adapter (.csv) first
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");

        DynamoDBAdapter a = new DynamoDBAdapter("http://localhost:8000", "cn-north-1");

        // Save the current policy to DB
        a.savePolicy(e.getModel());
    }
}
