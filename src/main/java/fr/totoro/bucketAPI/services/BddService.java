package fr.totoro.bucketAPI.services;

import com.mongodb.*;
import fr.totoro.bucketAPI.helpers.DBObjectHelper;
import org.springframework.stereotype.Repository;

import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BddService implements IBddService {

    MongoClient mongoClient;
    DB database;

    BddService() throws UnknownHostException {
        mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        database = mongoClient.getDB("BucketDatabase");
    }

    @Override
    public DBObject add(String collection, DBObject body) {
        DBObjectHelper.asNewDocument(body,collection);
        DBCollection dbCollection = database.getCollection(collection);
        dbCollection.insert(body);
        return body;
    }

    @Override
    public DBObject get(String collection, String id) {
        DBCollection dbCollection = database.getCollection(collection);
        DBObject query = DBObjectHelper.buildWithId(collection ,id);
        return (DBObject) dbCollection.findOne(query);
    }

    @Override
    public Long getLastUpdate(String collection, String id) {
        DBCollection dbCollection = database.getCollection(collection);
        DBObject query = DBObjectHelper.buildWithId(collection ,id);
        DBObject fields = new BasicDBObject().append(DBObjectHelper.TIME_STAMP, 1);
        DBObject res = dbCollection.findOne(query, fields);
        if(res == null) { return 0L;} else { return (Long) res.get(DBObjectHelper.TIME_STAMP);}
    }

    @Override
    public DBObject save(String collection, String id, DBObject body) {
        DBObject query = DBObjectHelper.buildWithId(collection ,id);
        DBObjectHelper.newTimeStamp(body);
        body.put(DBObjectHelper.ID, DBObjectHelper.toStringId(collection, id));
        DBCollection dbCollection = database.getCollection(collection);
        dbCollection.update(query, body);
        return body;
    }

    @Override
    public List<DBObject> getAll(String collection) {
        DBCollection dbCollection = database.getCollection(collection);
        return dbCollection.find().toArray().stream().map(obj -> (DBObject) obj).collect(Collectors.toList());
    }

    @Override
    public List<DBObject> getAll(String collection, DBObject query) {
        DBCollection dbCollection = database.getCollection(collection);
        return dbCollection.find(query).toArray().stream().map(obj -> (DBObject) obj).collect(Collectors.toList());
    }

    @Override
    public void delete(String collection, String id) {
        DBObject query = DBObjectHelper.buildWithId(collection ,id);
        DBCollection dbCollection = database.getCollection(collection);
        dbCollection.remove(query);
    }

    @Override
    public void removeLinks(DBObject query) {
        DBCollection dbCollection = database.getCollection(LINKS_TABLE);
        dbCollection.remove(query);
    }

    @Override
    public void addLink(DBObject link) {
        DBCollection dbCollection = database.getCollection(LINKS_TABLE);
        dbCollection.insert(link);
    }
}
