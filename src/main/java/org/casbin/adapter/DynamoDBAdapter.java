package org.casbin.adapter;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

public class DynamoDBAdapter
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    private AmazonDynamoDB client;
    private DynamoDB dynamoDB;
    private Table table;

    public DynamoDBAdapter(String serviceEndpoint, String signingRegion) {
        this.client = AmazonDynamoDBClientBuilder
                        .standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(serviceEndpoint, signingRegion))
                        .build();

        this.dynamoDB = new DynamoDB(this.client);
        this.table = dynamoDB.getTable("Movies");
    }

    public String GetItem(int year, String title) {
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("year", year, "title", title);
        Item outcome = this.table.getItem(spec);
        return outcome.get("year").toString();
    }


}
