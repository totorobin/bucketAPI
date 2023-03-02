package fr.totoro.bucketAPI.helpers;

import fr.totoro.bucketAPI.BucketController;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Configuration
public class AOPEntryLogger {

    private Logger logger = LoggerFactory.getLogger(BucketController.class);

    @Before("execution(* fr.totoro.bucketAPI.BucketController.*(..))")
    public void before(JoinPoint joinPoint) {
        logger.info(joinPoint.getSignature().getName() + " /" +
                Arrays.stream(joinPoint.getArgs()).filter(arg -> arg instanceof String).map(arg -> (String)arg).collect(Collectors.joining("/")));
    }
}
