package com.example.util;

import com.example.dao.ProdMapper;
import com.example.module.OrderBean;
import com.example.module.Product;
import com.example.service.KillProdService;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
@Component
public class KillQueueUtil {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    KillProdService killProdService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    ProdMapper prodMapper;

    @Autowired
    @Qualifier("redissonClient")
    private RedissonClient redissonClient;
    //使用无界线程队列，，保证线程安全
    private Queue<OrderBean> queue = new ConcurrentLinkedQueue<>();
    //新建线程池，可缓存，但是无界队列
    private ExecutorService executorService = Executors.newCachedThreadPool();
    //可以等到下个任务执行完的结果
    private ScheduledExecutorService sec = Executors.newScheduledThreadPool(4);

    /**
     * 执行延时
     *
     */
    private void excute() {
        sec.scheduleWithFixedDelay(() -> {
            OrderBean bean = queue.poll();
            if (bean != null) {
                log.info("-----------开始处理队列");
                sock(bean.getProdId(), bean.getUserId());
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    public void addBean(OrderBean bean) {
        queue.offer(bean);
    }

    /**
     * 实例化方法，spring 初始化会管理，启动时调用
     */
    public KillQueueUtil() {
        log.info("---------启动队列------");
        excute();
    }

    public void sock(int prodId, String userId) {
        final String killGoodCount = KillProdService.KILL_GOOD_COUNT + prodId;
        //初始化库存
        long sock_result = killProdService.sockProdByLUA(killGoodCount, 1, KillProdService.STOCK_LUA);
        // 初始化库存
        if (sock_result == KillProdService.UNINITIALIZED_STOCK) {
            RLock rLock = redissonClient.getLock("store_lock_cn_order");
            try {
                //获取锁两秒后过期
                rLock.lock(2, TimeUnit.SECONDS);
                //双重验证，避免并发时回源到数据库
                long int_rsult = killProdService.sockProdByLUA(killGoodCount, 1, KillProdService.STOCK_LUA);
                //再次初始化库存
                if (sock_result == KillProdService.UNINITIALIZED_STOCK) {
                    //获取初始化库存
                    Product product = prodMapper.queryAllProdById(prodId);
                    //将库存设置到Redis
                    redisTemplate.opsForValue().set(killGoodCount, product.getStock(), 60 * 60, TimeUnit.SECONDS);
                    sock_result = killProdService.sockProdByLUA(killGoodCount, 1, KillProdService.STOCK_LUA);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                rLock.unlock();
            }
        }
        if (sock_result > 0) {
            log.info("------秒杀成功--------");
        } else {
            log.info("------秒杀失败--------");
        }
    }

}
