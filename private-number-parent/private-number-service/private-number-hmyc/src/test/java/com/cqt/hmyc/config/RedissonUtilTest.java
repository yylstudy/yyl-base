package com.cqt.hmyc.config;

import com.cqt.hmyc.PrivateNumberHideApplication;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author linshiqiang
 * @date 2021/7/12 14:26
 */
@SpringBootTest(classes = PrivateNumberHideApplication.class)
public class RedissonUtilTest {

    @Resource
    public RedissonClient redissonClient;

//    @Test
//    public void test3() {
//
//        RBatch batch = redissonClient.createBatch();
//        batch.getBucket("test1112343").setAsync(IdUtil.fastUUID(), 0, TimeUnit.SECONDS);
//        batch.execute();
////        RMap<Object, Object> map = redissonClient.getMap("aaaaa1234111");
////        map.put("id", 111);
////        map.put("age", "");
////        map.put("age2", 133);
//    }

//    @Test
//    public void test2() {
//        List<String> numList = new ArrayList<>();
//        for (int i = 0; i < 300; i++) {
//            numList.add("1301234567" + i);
//        }
//        redissonClient.getSet("{test}:0591").addAll(numList);
//        redissonClient.getSet("{test}:110");
//        redissonClient.getSet("{test}:119");
//
//
//        RScript script = redissonClient.getScript();
//
//        String lua ="local pool = redis.call('sdiff', KEYS[1],KEYS[2],KEYS[3]);\n" +
//                "if next(pool) ~= nil  then\n" +
//                "    if #pool < #KEYS then\n" +
//                "        return nil;\n" +
//                "    end;\n" +
//                "    local r = {};\n" +
//                "    for i=1, #(KEYS) do\n" +
//                "        local index = math.random(1, #pool);\n" +
//                "        local x = pool[index];\n" +
//                "        table.remove(pool, index);\n" +
//                "        table.insert(r, x);\n" +
//                "        redis.call('sadd', KEYS[2], x);\n" +
//                "        redis.call('sadd', KEYS[3], x);\n" +
//                "    end\n" +
//                "\treturn r;\n" +
//                "end;\n" +
//                "return nil;";
//        List<Object> keys =new ArrayList<>();
//        keys.add("{test}:0591");
//        keys.add("{test}:110");
//        keys.add("{test}:119");
//        long start = System.currentTimeMillis();
//        Object eval = script.eval(RScript.Mode.READ_WRITE, lua, RScript.ReturnType.MULTI, keys);
//        System.out.println(eval);
//        System.out.println(System.currentTimeMillis() - start);
//    }
}
