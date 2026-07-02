package com.innowise.paymentservice.changelog;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.bson.Document;

@ChangeUnit(id = "001_create_payments", order = "001", author = "alina")
public class PaymentChangeLog {
    @Execution
    public void changeSet(MongoDatabase mongoDatabase) {
        mongoDatabase.createCollection("payments");

        MongoCollection<Document> collection = mongoDatabase.getCollection("payments");
        collection.createIndex(Indexes.ascending("user_id"));
        collection.createIndex(Indexes.ascending("order_id"));
        collection.createIndex(Indexes.ascending("status"));
    }

    @RollbackExecution
    public void rollback(MongoDatabase mongoDatabase) {
        mongoDatabase.getCollection("payments").drop();
    }
}
