package com.example.service;

import com.example.dao.ProdMapper;
import com.example.module.OrderBean;
import com.example.module.Product;
import com.example.util.KillQueueUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class KillProdService {

    private Object lockObect = new Object();
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    ProdMapper prodMapper;
    @Autowired
    KillQueueUtil killQueueUtil;

    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;

    public final static String SECKILL_KEY = "SECKILLKEY";
    public final static String HAVE_DEAL = "DEAL_CUST_";
    public final static String KILL_GOOD_COUNT = "KILL_GOOD_COUNT_";
    /**
     * 库存没有初始化，库存key在redis里面不存在
     */
    public static final long UNINITIALIZED_STOCK = -3L;
    public static String STOCK_LUA_1 = "";
    public static String STOCK_LUA = "";

    static {
        /**
         *
         * @desc 扣减库存Lua脚本
         * 库存（stock）-1：表示不限库存
         * 库存（stock）0：表示没有库存
         * 库存（stock）大于0：表示剩余库存
         *
         * @params 库存key
         * @return
         * 		-3:库存未初始化
         * 		-2:库存不足
         * 		-1:不限库存
         * 		大于等于0:剩余库存（扣减之后剩余的库存）
         * 	    redis缓存的库存(value)是-1表示不限库存，直接返回1
         */
        StringBuilder sb = new StringBuilder();
        sb.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb.append("    local stock = tonumber(redis.call('get', KEYS[1]));");
        sb.append("    local num = tonumber(ARGV[1]);");
        sb.append("    if (stock == -1) then");
        sb.append("        return -1;");
        sb.append("    end;");
        sb.append("    if (stock >= num) then");
        sb.append("        return redis.call('incrby', KEYS[1], 0 - num);");
        sb.append("    end;");
        sb.append("    return -2;");
        sb.append("end;");
        sb.append("return -3;");
        STOCK_LUA = sb.toString();

        StringBuilder sb1 = new StringBuilder();
        sb1.append("if (redis.call('exists', KEYS[1]) == 1) then");
        sb1.append("    local stock = tonumber(redis.call('get', KEYS[1]));\n");
        sb1.append("    local num = tonumber(ARGV[1]);");
        sb1.append("         if (stock >= num) then");
        sb1.append("             return redis.call('incrby', KEYS[1], 0 - num);");
        sb1.append("    end;");
        sb1.append("        return -2;");
        sb1.append("    end;");
        sb1.append("    return -3;");
        STOCK_LUA_1 = sb1.toString();
    }

    /**
     * 查询秒杀商品详情
     * 1,理论上只要有一个用户查询成功，就会缓存到本地
     * <p>
     * 2，先从本地查，查不到再到redis查，查到再存到本地缓存和redis中（为啥还要存一份到redis中）
     * <p>
     * 3，数据库查询加锁？怎样增加健壮性
     * <p>
     * 4，缓存雪崩和击穿。1，雪崩,缓存同时大量失效，查询到数据库中（a:通过设置不同的过期时间，b:提前预热数据库）
     * 2，击穿，查询大量不存在的值到redis中，查询数据库，造成击穿（a:把key 也存到数据库中，但是时间要短 ;
     * b:使用布隆过滤器）
     */
    public Product syncProd(Integer id) {
        Product product = null;
        String prod_cache_key = SECKILL_KEY + id;
        //先从本地缓存里拿
        Cache cache = cacheManager.getCache("killgoodDetail");
        if (cache.isKeyInCache(prod_cache_key)) {
            log.info(Thread.currentThread().getName() + "-------拿到本地缓存======");
            return (Product) cache.get(prod_cache_key).getObjectValue();
        }
        //再从Redis中拿
        Object killproduct = redisTemplate.opsForValue().get(prod_cache_key);
        if (null != killproduct) {
            log.info(Thread.currentThread().getName() + "-------从Redis中拿到缓存======");
            return (Product) killproduct;
        }
        //不存在则从数据库拿，这里加锁，防止大量请求到数据库中
        synchronized (lockObect) {
            //这里，当第一个线程拿了锁并且成功返回，第二个线程就等待后直接从缓存拿，所以要再查一次
            //先从本地缓存里拿

            if (cache.isKeyInCache(prod_cache_key)) {
                log.info(Thread.currentThread().getName() + "-------拿到本地缓存======");
                return (Product) cache.get(prod_cache_key).getObjectValue();
            }
            //再从Redis中拿
            Object killproduct2 = redisTemplate.opsForValue().get(prod_cache_key);
            if (null != killproduct2) {
                log.info(Thread.currentThread().getName() + "-------从Redis中拿到缓存======");
                return (Product) killproduct2;
            }

            product = prodMapper.queryAllProdById(id);
            if (null != product) {
                cache.putIfAbsent(new Element(prod_cache_key, product));
                redisTemplate.opsForValue().set(prod_cache_key, product, 2, TimeUnit.DAYS);
            } else {
                redisTemplate.opsForValue().set(prod_cache_key, null, 2, TimeUnit.MINUTES);
            }
        }

        return product;
    }

    /**
     * 秒杀接口，功能需求
     * 1，多个用户线程进行秒杀，容易造成多扣库存（分布式锁，lua脚本）
     * 2，并发量大，容易阻塞（秒杀成功订单通过本地队列缓存）
     * 3，锁过期问题，锁过期时间小于业务执行时间，需要对锁进行续命，防止脏数据
     *
     * @param killId
     * @param userId
     * @return
     */
    public boolean secKillByLock(int killId, String userId) {
        Boolean isDeal = redisTemplate.opsForSet().isMember(HAVE_DEAL + killId, userId);
        if (isDeal) {
            log.info("这个扑街已经参加过秒杀，滚~");
            return false;
        }
        final String killGoodCount = KILL_GOOD_COUNT + killId;
        //初始化库存
        long sock_result = this.sockProdByLUA(killGoodCount, 1, STOCK_LUA_1);
        // 初始化库存
        if (sock_result == UNINITIALIZED_STOCK) {
            RLock rLock = redissonClient.getLock("store_lock_cn_order");
            try {
                //获取锁两秒后过期
                rLock.lock(2, TimeUnit.SECONDS);
                //双重验证，避免并发时回源到数据库
                long int_rsult = this.sockProdByLUA(killGoodCount, 1, STOCK_LUA_1);
                //再次初始化库存
                if (sock_result == UNINITIALIZED_STOCK) {
                    //获取初始化库存
                    Product product = prodMapper.queryAllProdById(killId);
                    //将库存设置到Redis 这个地方有问题
                    redisTemplate.opsForValue().set(killGoodCount, product.getStock().intValue(), 60 * 60, TimeUnit.SECONDS);
                    sock_result = this.sockProdByLUA(killGoodCount, 1, STOCK_LUA_1);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                rLock.unlock();
            }
        }
        boolean flag = sock_result >= 0;
        if (flag) {
            redisTemplate.opsForSet().add(HAVE_DEAL + killId, userId);
        }
        return flag;
    }

    /**
     * 有bug 需要修复
     *
     * @param prodKey
     * @param count
     * @param luaScript
     * @return
     */
    public Long sockProdByLUA(String prodKey, int count, String luaScript) {
        //脚本里的key参数
        List<String> keys = new ArrayList<>();
        keys.add(prodKey);
        //脚本里的argv参数
        List<String> argvs = new ArrayList<>();
        argvs.add(Integer.toString(count));
        Long result = (long) redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                Object nativeConnection = connection.getNativeConnection();
                // 集群模式和单机模式虽然执行脚本的方法一样，但是没有共同的接口，所以只能分开执行
                // 集群模式
                if (nativeConnection instanceof JedisCluster) {
                    return (Long) ((JedisCluster) nativeConnection).eval(luaScript, keys, argvs);
                }

                // 单机模式
                else if (nativeConnection instanceof Jedis) {
                    return (Long) ((Jedis) nativeConnection).eval(luaScript, keys, argvs);
                }
                /*else if (nativeConnection instanceof Redisson) {
                    Redisson redisson = (Redisson)nativeConnection;
                    return redisson.getScript().eval(RScript.Mode.READ_WRITE,STOCK_LUA,RScript.ReturnType.INTEGER, Collections.singletonList(keys), new List[]{args});
                }*/
                return UNINITIALIZED_STOCK;
            }
        });
        return result;
    }


    public boolean sockByQueen(int prodId, String userId) {
        boolean result = false;
        killQueueUtil.addBean(new OrderBean(prodId, userId));
        return result;
    }

}
