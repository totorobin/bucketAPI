package fr.totoro.bucketAPI.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;
import fr.totoro.bucketAPI.helpers.BucketAdaptator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class BddAOPNotification {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @AfterReturning(value = "execution(* fr.totoro.bucketAPI.services.IBddService+.save(..))", returning = "updated")
    public void afterUpdate(JoinPoint joinPoint, DBObject updated) {
        logger.info(joinPoint.toString());
        try {
            MyHandler.broadcastUpdate(BucketAdaptator.toJsonNode(updated));
        } catch (JsonProcessingException e) {
            logger.error("failed to parse updated DBOject");
        }
    }
}
