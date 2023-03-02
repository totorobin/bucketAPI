package fr.totoro.bucketAPI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import fr.totoro.bucketAPI.helpers.BucketAdaptator;
import fr.totoro.bucketAPI.helpers.DBObjectHelper;
import fr.totoro.bucketAPI.pojo.Set;
import fr.totoro.bucketAPI.services.IBddService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static fr.totoro.bucketAPI.helpers.BucketAdaptator.toDBObject;
import static fr.totoro.bucketAPI.helpers.BucketAdaptator.toJsonNode;

@RestController()
@RequestMapping("api")
@CrossOrigin
public class BucketController {

    private final IBddService bddService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    BucketController(IBddService bddService) {
        this.bddService = bddService;
    }

    @PostMapping("/{collection}")
    public ResponseEntity<JsonNode> create(@PathVariable String collection , @RequestBody JsonNode body) throws JsonProcessingException {
        DBObject result = bddService.add(collection, toDBObject(body));
        return new ResponseEntity(toJsonNode(result),HttpStatus.CREATED);

    }

    @PutMapping("/{collection}/{id}")
    public ResponseEntity<JsonNode> update(@PathVariable String collection , @PathVariable String id , @RequestBody JsonNode body) throws JsonProcessingException {
        //before update, check if data exist and is not more recent
        final DBObject previous =  bddService.get(collection,id);
        if(previous == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else if(body.get(DBObjectHelper.TIME_STAMP) == null || body.get(DBObjectHelper.TIME_STAMP).asLong() < DBObjectHelper.getTimeStamp(previous)) {
            return new ResponseEntity(HttpStatus.NOT_MODIFIED);
        }

        DBObject result =  bddService.save(collection, id, toDBObject(body));
        //MyHandler.broadcastUpdate(toJsonNode(result));
        return new ResponseEntity(toJsonNode(result),HttpStatus.OK);
    }

    @PatchMapping("/{collection}/{id}")
    public ResponseEntity<JsonNode> patch(@PathVariable String collection , @PathVariable String id , @RequestBody JsonNode body) throws JsonProcessingException {
        final DBObject document =  bddService.get(collection,id);
        if(document == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        document.putAll(toDBObject(body).toMap());
        DBObject result =  bddService.save(collection, id, document);
        //MyHandler.broadcastUpdate(toJsonNode(result));
        return new ResponseEntity(toJsonNode(result),HttpStatus.OK);
    }

    @DeleteMapping("/{collection}/{id}")
    public ResponseEntity<JsonNode> delete(@PathVariable String collection , @PathVariable String id) throws JsonProcessingException {
        //On supprime l'element de sa collection
        bddService.delete(collection,id);
        String bddId = DBObjectHelper.toStringId(collection,id);

        // on supprime tous les liens dont il est parent
        DBObject query = new BasicDBObject().append("parent" , bddId);
        bddService.removeLinks(query);

        // on recherche tous les liens ou il est enfant pour mettre a jour le parent
        query = new BasicDBObject().append("child" , bddId);
        List<DBObject> liens = bddService.getAll(IBddService.LINKS_TABLE, query);
        for(DBObject lien : liens) {
            Set<String,String> parent = DBObjectHelper.toSetId(lien.get("parent").toString());
            removeChildId(parent.getKey(), parent.getKey(), lien.get("var").toString(), collection, id);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }


    @GetMapping("/{collection}/{id}")
    public ResponseEntity<JsonNode> get(@PathVariable String collection ,@PathVariable String id) throws JsonProcessingException {
        DBObject result =  bddService.get(collection,id);
        if(result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(toJsonNode(result),HttpStatus.OK);
    }


    @GetMapping("/{collection}")
    public ResponseEntity<List<JsonNode>> getAll(@PathVariable String collection ) throws JsonProcessingException {
        List<JsonNode> json = new ArrayList<>();
        List<DBObject> result =  bddService.getAll(collection);
        if(result == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        for(DBObject document : result) {
            json.add(BucketAdaptator.toJsonNode(document));
        }
        return new ResponseEntity(json,HttpStatus.OK);
    }

    @PostMapping("/{collection}/{id}/{variable}/{childCollection}")
    public ResponseEntity<List<JsonNode>> addChild(@PathVariable String collection , @PathVariable String id ,
                                             @PathVariable String variable , @PathVariable String childCollection ,
                                             @RequestBody JsonNode childBody) throws JsonProcessingException {

        final DBObject parent =  bddService.get(collection,id);
        if(parent == null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        final DBObject result = bddService.add(childCollection, toDBObject(childBody));

        addLink(result, parent, variable);

        //MyHandler.broadcastUpdate(toJsonNode(result));
        //MyHandler.broadcastUpdate(toJsonNode(parent));
        return new ResponseEntity(Arrays.asList(toJsonNode(parent),toJsonNode(result)),HttpStatus.OK);
    }

    @PutMapping("/{collection}/{id}/{variable}/{childCollection}/{childId}")
    public ResponseEntity<JsonNode> addChildId(@PathVariable String collection , @PathVariable String id ,
                                             @PathVariable String variable , @PathVariable String childCollection , @PathVariable String childId ) throws JsonProcessingException {
        final  DBObject child =  bddService.get(childCollection,childId);
        if(child == null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        final DBObject parent =  bddService.get(collection,id);
        if(parent == null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        addLink(child, parent, variable);

       // MyHandler.broadcastUpdate(toJsonNode(parent));
        return new ResponseEntity(toJsonNode(parent),HttpStatus.OK);
    }

    @DeleteMapping("/{collection}/{id}/{variable}/{childCollection}/{childId}")
    public ResponseEntity<JsonNode> removeChildId(@PathVariable String collection , @PathVariable String id ,
                                               @PathVariable String variable , @PathVariable String childCollection , @PathVariable String childId ) throws JsonProcessingException {
        final DBObject parent =  bddService.get(collection,id);
        if(parent == null){
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        final String childStringId = DBObjectHelper.toStringId(childCollection ,childId);
        if(parent.containsField(variable)) {
            if(parent.get(variable) instanceof List) {
                List list = (List<Object>) parent.get(variable);
                parent.put(variable, list.stream().filter(v -> !v.equals(childStringId)).collect(Collectors.toList()));
            } else if(parent.get(variable).equals(childStringId)){
                parent.removeField(variable);
            } else {
                return new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        bddService.save(collection, id, parent);

        DBObject query = new BasicDBObject()
                .append("child", childStringId)
                .append("parent", DBObjectHelper.getId(parent))
                .append("var", variable);
        bddService.removeLinks(query);

        //MyHandler.broadcastUpdate(toJsonNode(parent));
        return new ResponseEntity(HttpStatus.OK);
    }




    private void addLink(final DBObject child, final DBObject parent, String variable) {
        if(parent.containsField(variable)) {
            List<Object> list = new ArrayList<>();
            if(parent.get(variable) instanceof List) {
                list = (List<Object>) parent.get(variable);
            } else {
                list.add(parent.get(variable));
            }
            list.add(DBObjectHelper.getId(child));
            parent.put(variable, list);
        }  else {
            parent.put(variable, DBObjectHelper.getId(child));
        }
        bddService.save(DBObjectHelper.getCollection(parent), DBObjectHelper.getUUID(parent), parent);

        DBObject link = (DBObject) new BasicDBObject()
                .append("child", DBObjectHelper.getId(child))
                .append("parent", DBObjectHelper.getId(parent))
                .append("var", variable);

        bddService.addLink(link);
    }




}
