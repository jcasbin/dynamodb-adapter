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

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Assertion;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.Helper;

import java.util.*;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

class CasbinRule {
    String ptype;
    String v0;
    String v1;
    String v2;
    String v3;
    String v4;
    String v5;
}

/**
 * DynamoDBAdapter is the AWS DynamoDB adapter for jCasbin 
 */
public class DynamoDBAdapter implements Adapter
{
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table table;

    public static void main(String[] args) {
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        DynamoDBAdapter a = new DynamoDBAdapter("http://localhost:8000", "cn-north-1");
        
        a.savePolicy(e.getModel());
        a.loadPolicy(e.getModel());
    }

    public DynamoDBAdapter(String serviceEndpoint, String signingRegion) {
        this.client = AmazonDynamoDBClientBuilder
                        .standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                        .build();

        this.dynamoDB = new DynamoDB(this.client);
    }

    public void createTable() {
        try {
            this.table = this.dynamoDB.createTable(
                "casbin_rule",
                Arrays.asList(new KeySchemaElement("ID", KeyType.HASH)),
                Arrays.asList(new AttributeDefinition("ID", ScalarAttributeType.S)),
                new ProvisionedThroughput(10L, 10L)
            );
            this.table.waitForActive();
        }
        catch (Exception e) {
            throw new Error(e);
        }
        
    }

    public void dropTable() {
        this.table = this.dynamoDB.getTable("casbin_rule");
        try {
            this.table.delete();
            this.table.waitForDelete();
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    private List<CasbinRule> getAllItem() {
        List<CasbinRule> rules = new ArrayList<>();
        ScanSpec scanSpec = new ScanSpec();
        ItemCollection<ScanOutcome> items = this.table.scan(scanSpec);
        Iterator<Item> iter = items.iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            CasbinRule line = new CasbinRule();
            line.ptype = item.get("ptype").toString();
            line.v0 = item.get("v0") != null ? item.get("v0").toString() : "";
            line.v1 = item.get("v1") != null ? item.get("v1").toString() : "";
            line.v2 = item.get("v2") != null ? item.get("v2").toString() : "";
            line.v3 = item.get("v3") != null ? item.get("v3").toString() : "";
            line.v4 = item.get("v4") != null ? item.get("v4").toString() : "";
            line.v5 = item.get("v5") != null ? item.get("v5").toString() : "";
            rules.add(line);
        }
        return rules;
    }

    private void loadPolicyLine(CasbinRule line, Model model) {
        String lineText = line.ptype;
        if (!line.v0.equals("")) {
            lineText += ", " + line.v0;
        }
        if (!line.v1.equals("")) {
            lineText += ", " + line.v1;
        }
        if (!line.v2.equals("")) {
            lineText += ", " + line.v2;
        }
        if (!line.v3.equals("")) {
            lineText += ", " + line.v3;
        }
        if (!line.v4.equals("")) {
            lineText += ", " + line.v4;
        }
        if (!line.v5.equals("")) {
            lineText += ", " + line.v5;
        }

        Helper.loadPolicyLine(lineText, model);
    }

    /**
     * loadPolicy loads all policy rules from the storage.
     */
    @Override
    public void loadPolicy(Model model) {
        List<CasbinRule> rules = getAllItem();
        for (CasbinRule line : rules) {
            loadPolicyLine(line, model);
        }
    }

    private CasbinRule savePolicyLine(String ptype, List<String> rule) {
        CasbinRule line = new CasbinRule();

        line.ptype = ptype;
        if (rule.size() > 0) {
            line.v0 = rule.get(0);
        }
        if (rule.size() > 1) {
            line.v1 = rule.get(1);
        }
        if (rule.size() > 2) {
            line.v2 = rule.get(2);
        }
        if (rule.size() > 3) {
            line.v3 = rule.get(3);
        }
        if (rule.size() > 4) {
            line.v4 = rule.get(4);
        }
        if (rule.size() > 5) {
            line.v5 = rule.get(5);
        }

        return line;
    }
    
    private void putCasbinRuleItem(CasbinRule line) {
        Item item = new Item().withPrimaryKey("ID", UUID.randomUUID().toString())
                                .with("ptype", line.ptype)
                                .with("v0", line.v0)
                                .with("v1", line.v1)
                                .with("v2", line.v2)
                                .with("v3", line.v3)
                                .with("v4", line.v4)
                                .with("v5", line.v5);
        this.table.putItem(item);
    }


    /**
     * svePolicy saves all policy rules to the storage.
     */
    @Override
    public void savePolicy(Model model) {
        for (Map.Entry<String, Assertion> entry : model.model.get("p").entrySet()) {
                String ptype = entry.getKey();
                Assertion ast = entry.getValue();
                for (List<String> rule : ast.policy) {
                    CasbinRule line = savePolicyLine(ptype, rule);
                    putCasbinRuleItem(line);
                }
        }

        for (Map.Entry<String, Assertion> entry : model.model.get("g").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();
            for (List<String> rule : ast.policy) {
                CasbinRule line = savePolicyLine(ptype, rule);
                putCasbinRuleItem(line);
            }
        }
    }

    /**
     * addPolicy adds a policy rule to the storage.
     */
    @Override
    public void addPolicy(String sec, String ptype, List<String> rule) {
        throw new Error("not implemented");
    }

    /**
     * removePolicy removes a policy rule from the storage.
     */
    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        throw new Error("not implemented");
    }

    /**
     * removeFilteredPolicy removes policy rules that match the filter from the storage.
     */
    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        throw new Error("not implemented");
    }

}
