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

import java.net.URI;
import java.util.*;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

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
    private DynamoDbClient client;
    private static final String TABLE_NAME = "casbin_rule";

    public static void main(String[] args) {
        Enforcer e = new Enforcer("examples/rbac_model.conf", "examples/rbac_policy.csv");
        DynamoDBAdapter a = new DynamoDBAdapter("http://localhost:8000", "cn-north-1");
        
        a.savePolicy(e.getModel());
        a.loadPolicy(e.getModel());
    }

    public DynamoDBAdapter(String serviceEndpoint, String signingRegion) {
        this.client = DynamoDbClient.builder()
                .endpointOverride(URI.create(serviceEndpoint))
                .region(software.amazon.awssdk.regions.Region.of(signingRegion))
                .build();
    }

    public DynamoDBAdapter(DynamoDbClient client) {
        this.client = client;
    }

    public void createTable() {
        try {
            CreateTableRequest request = CreateTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("ID")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("ID")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(10L)
                            .writeCapacityUnits(10L)
                            .build())
                    .build();
            
            client.createTable(request);
            
            // Wait for table to be active
            DynamoDbWaiter waiter = client.waiter();
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            WaiterResponse<DescribeTableResponse> waiterResponse = 
                    waiter.waitUntilTableExists(describeRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void dropTable() {
        try {
            DeleteTableRequest request = DeleteTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            client.deleteTable(request);
            
            // Wait for table to be deleted
            DynamoDbWaiter waiter = client.waiter();
            DescribeTableRequest describeRequest = DescribeTableRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            WaiterResponse<DescribeTableResponse> waiterResponse = 
                    waiter.waitUntilTableNotExists(describeRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    private List<CasbinRule> getAllItem() {
        List<CasbinRule> rules = new ArrayList<>();
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build();
            
            ScanResponse response = client.scan(scanRequest);
            
            for (Map<String, AttributeValue> item : response.items()) {
                CasbinRule line = new CasbinRule();
                line.ptype = getAttributeValue(item, "ptype");
                line.v0 = getAttributeValue(item, "v0");
                line.v1 = getAttributeValue(item, "v1");
                line.v2 = getAttributeValue(item, "v2");
                line.v3 = getAttributeValue(item, "v3");
                line.v4 = getAttributeValue(item, "v4");
                line.v5 = getAttributeValue(item, "v5");
                rules.add(line);
            }
        } catch (Exception e) {
            throw new Error(e);
        }
        return rules;
    }
    
    private String getAttributeValue(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        if (value != null && value.s() != null) {
            return value.s();
        }
        return "";
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
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ID", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("ptype", AttributeValue.builder().s(line.ptype != null ? line.ptype : "").build());
        item.put("v0", AttributeValue.builder().s(line.v0 != null ? line.v0 : "").build());
        item.put("v1", AttributeValue.builder().s(line.v1 != null ? line.v1 : "").build());
        item.put("v2", AttributeValue.builder().s(line.v2 != null ? line.v2 : "").build());
        item.put("v3", AttributeValue.builder().s(line.v3 != null ? line.v3 : "").build());
        item.put("v4", AttributeValue.builder().s(line.v4 != null ? line.v4 : "").build());
        item.put("v5", AttributeValue.builder().s(line.v5 != null ? line.v5 : "").build());
        
        PutItemRequest request = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        
        client.putItem(request);
    }

    private Map<String, AttributeValue> buildItemFromCasbinRule(CasbinRule line) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ID", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("ptype", AttributeValue.builder().s(line.ptype != null ? line.ptype : "").build());
        item.put("v0", AttributeValue.builder().s(line.v0 != null ? line.v0 : "").build());
        item.put("v1", AttributeValue.builder().s(line.v1 != null ? line.v1 : "").build());
        item.put("v2", AttributeValue.builder().s(line.v2 != null ? line.v2 : "").build());
        item.put("v3", AttributeValue.builder().s(line.v3 != null ? line.v3 : "").build());
        item.put("v4", AttributeValue.builder().s(line.v4 != null ? line.v4 : "").build());
        item.put("v5", AttributeValue.builder().s(line.v5 != null ? line.v5 : "").build());
        return item;
    }

    private void writeBatchWithRetry(List<WriteRequest> writeRequests) {
        List<WriteRequest> unprocessedItems = new ArrayList<>(writeRequests);
        int retryCount = 0;
        int maxRetries = 5;
        long initialBackoffMs = 50;
        
        while (!unprocessedItems.isEmpty() && retryCount < maxRetries) {
            Map<String, List<WriteRequest>> requestItems = new HashMap<>();
            requestItems.put(TABLE_NAME, unprocessedItems);
            
            BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                    .requestItems(requestItems)
                    .build();
            
            BatchWriteItemResponse response = client.batchWriteItem(batchRequest);
            
            // Get any unprocessed items for retry
            Map<String, List<WriteRequest>> unprocessed = response.unprocessedItems();
            if (unprocessed != null && unprocessed.containsKey(TABLE_NAME)) {
                unprocessedItems = new ArrayList<>(unprocessed.get(TABLE_NAME));
                retryCount++;
                
                if (!unprocessedItems.isEmpty()) {
                    // Exponential backoff
                    long backoffMs = initialBackoffMs * (long) Math.pow(2, retryCount - 1);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new Error("Interrupted during backoff", e);
                    }
                }
            } else {
                // All items processed successfully
                unprocessedItems.clear();
            }
        }
        
        if (!unprocessedItems.isEmpty()) {
            throw new Error("Failed to write all items after " + maxRetries + " retries. " +
                    unprocessedItems.size() + " items remaining.");
        }
    }

    private void batchSaveRows(List<CasbinRule> rules) {
        if (rules.isEmpty()) {
            return;
        }
        
        int batchSize = 25; // DynamoDB batch write limit
        List<WriteRequest> batch = new ArrayList<>();
        
        for (CasbinRule rule : rules) {
            Map<String, AttributeValue> item = buildItemFromCasbinRule(rule);
            WriteRequest writeRequest = WriteRequest.builder()
                    .putRequest(PutRequest.builder().item(item).build())
                    .build();
            batch.add(writeRequest);
            
            if (batch.size() >= batchSize) {
                writeBatchWithRetry(batch);
                batch.clear();
            }
        }
        
        // Write any remaining items
        if (!batch.isEmpty()) {
            writeBatchWithRetry(batch);
        }
    }


    /**
     * svePolicy saves all policy rules to the storage.
     */
    @Override
    public void savePolicy(Model model) {
        List<CasbinRule> allRules = new ArrayList<>();
        
        for (Map.Entry<String, Assertion> entry : model.model.get("p").entrySet()) {
                String ptype = entry.getKey();
                Assertion ast = entry.getValue();
                for (List<String> rule : ast.policy) {
                    CasbinRule line = savePolicyLine(ptype, rule);
                    allRules.add(line);
                }
        }

        for (Map.Entry<String, Assertion> entry : model.model.get("g").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();
            for (List<String> rule : ast.policy) {
                CasbinRule line = savePolicyLine(ptype, rule);
                allRules.add(line);
            }
        }
        
        batchSaveRows(allRules);
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
