package org.casbin.adapter;

import org.junit.Test;

import org.junit.Assert;

public class DynamoDBAdapterTest 
{
    @Test
    public void testGetItem() {
        DynamoDBAdapter adapter = new DynamoDBAdapter("http://localhost:8000", "cn-north-1");
        Assert.assertEquals("2004", adapter.GetItem(2004, "Alexander"));
    }
}
