package fr.totoro.bucketAPI.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.totoro.bucketAPI.pojo.Set;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBObjectHelper {

    public static final String ID = "_id";
    public static final String TIME_STAMP = "_timestamp";
    private static final Pattern pattern = Pattern.compile("/([^/]+)/([0-9a-f]{24})");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toStringId(String collection, String id) {
        return "/" +collection + "/" + id;
    }

    public static Set<String,String> toSetId(String id) {
        Matcher matcher = pattern.matcher(id);
        if(matcher.matches()) {
          return new Set<String,String>(matcher.group(1),matcher.group(2));
        }
        return null;
    }


    public static BasicDBObject buildWithId(String collection , String uuid) {
        return new BasicDBObject()
                .append(ID, toStringId(collection ,uuid));
    }

    public static DBObject asNewDocument(final DBObject dbObject, String collection) {
        dbObject.put(ID, DBObjectHelper.toStringId(collection , ObjectId.get().toHexString()));
        dbObject.put(TIME_STAMP, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        return dbObject;
    }

    public static Long getTimeStamp(DBObject dbObject) {
        return dbObject.get(TIME_STAMP) instanceof Long ? (Long) dbObject.get(TIME_STAMP) : 0;
    }

    public static String getId(DBObject dbObject) {
        return (String) dbObject.get(ID);
    }

     public static String getCollection(DBObject dbObject) {
        Matcher matcher = pattern.matcher(getId(dbObject));
        if(matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getUUID(DBObject dbObject) {
        Matcher matcher = pattern.matcher(getId(dbObject));
        if(matcher.matches()) {
            return matcher.group(2);
        }
        return null;
    }

    public static DBObject newTimeStamp(final DBObject body) {
        body.put(TIME_STAMP, LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
        return body;
    }
}
