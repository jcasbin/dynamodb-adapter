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

import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;

import java.util.*;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

/**
 * DynamoDBAdapter is the AWS DynamoDB adapter for jCasbin 
 */
public class DynamoDBAdapter implements Adapter
{
    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table table;

    public static void main(String[] args) {
        DynamoDBAdapter a = new DynamoDBAdapter("http://localhost:8000", "cn-north-1");
        a.createTable();
        a.dropTable();
    }

    public DynamoDBAdapter(String serviceEndpoint, String signingRegion) {
        try {
            this.client = AmazonDynamoDBClientBuilder
                            .standard()
                            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                            .build();
    
            this.dynamoDB = new DynamoDB(this.client);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try {
            System.out.println("Attempting to create table; please wait...");
            Table table = this.dynamoDB.createTable(
                "casbin_rule",
                Arrays.asList(new KeySchemaElement("ID", KeyType.HASH)),
                Arrays.asList(new AttributeDefinition("ID", ScalarAttributeType.S)),
                new ProvisionedThroughput(10L, 10L)
            );
            table.waitForActive();
            System.out.println("Table is created!");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dropTable() {
        Table table = this.dynamoDB.getTable("casbin_rule");
        try {
            System.out.println("Attempting to delete table; please wait...");
            table.delete();
            table.waitForDelete();
            System.out.print("Success.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * loadPolicy loads all policy rules from the storage.
     */
    @Override
    public void loadPolicy(Model model) {
        throw new Error("not implemented");
    }
    
    /**
     * svePolicy saves all policy rules to the storage.
     */
    @Override
    public void savePolicy(Model model) {
        throw new Error("not implemented");
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
