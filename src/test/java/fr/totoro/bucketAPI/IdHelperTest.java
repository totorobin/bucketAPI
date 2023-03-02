package fr.totoro.bucketAPI;

import fr.totoro.bucketAPI.helpers.DBObjectHelper;
import fr.totoro.bucketAPI.pojo.Set;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

public class IdHelperTest {


    @Test
    void test_toSetId() {
        Assert.notNull(DBObjectHelper.toSetId("/test/5f5bfc9c6aec282e20ff7f3a"), "should not be null");
        Set<String,String> res = DBObjectHelper.toSetId("/test/5f5bfc9c6aec282e20ff7f3a");
        Assert.isTrue(res.getKey().contentEquals("test"), "fail to parse key");
        Assert.isTrue(res.getValue().contentEquals("5f5bfc9c6aec282e20ff7f3a"), "fail to parse value");
        Assert.isNull(DBObjectHelper.toSetId("/test/5f5bfc9c6aec282e20ff7f3a/"), "should be null");
        Assert.isNull(DBObjectHelper.toSetId("/t/5f5bfc9c6ae/282e20ff7f3a"), "should also be null");
        Assert.isNull(DBObjectHelper.toSetId("/test/"), "should still be null");
        Assert.isNull(DBObjectHelper.toSetId("//5f5bfc9c6aec282e20ff7f3a"), "should always be null");
    }

    @Test
    void test_toStringId() {
        Assert.notNull(DBObjectHelper.toStringId("test","jjeo45h6t4h5fg4h"), "should not be null");
        Assert.isTrue(DBObjectHelper.toStringId("test","jjeo45h6t4h5fg4h").contentEquals("/test/jjeo45h6t4h5fg4h"), "fail to compute");
    }
}
