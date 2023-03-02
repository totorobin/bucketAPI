package fr.totoro.bucketAPI.services;

import com.mongodb.DBObject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBddService {

    String ID = "_id";
    String LINKS_TABLE = "_links";

    DBObject add(String collection, DBObject body);

    DBObject get(String collection, String id);

    Long getLastUpdate(String collection, String id);

    DBObject save(String collection, String id, DBObject body);

    List<DBObject> getAll(String collection);

    List<DBObject> getAll(String collection, DBObject query);

    void delete(String collection, String id);

    void removeLinks(DBObject query);

    void addLink(DBObject link);
}
