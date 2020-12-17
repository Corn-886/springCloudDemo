package rabbit;

import com.example.util.RabbitUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.util.HashMap;
import java.util.Map;

public class Producter {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitUtils.getConnection();
        Channel channel = connection.createChannel();
        // 声明一个接收被删除的消息的交换机和队列
        String EXCHANGE_DEAD_NAME = "exchange.dead";
        String QUEUE_DEAD_NAME = "queue_dead";
        channel.exchangeDeclare(EXCHANGE_DEAD_NAME, BuiltinExchangeType.DIRECT);
        // 声明一个队列,主题名，是否队列持久化，是否排外（只能本次连接访问），自动删除，
        channel.queueDeclare(QUEUE_DEAD_NAME, false, false, false, null);
        channel.queueBind(QUEUE_DEAD_NAME, EXCHANGE_DEAD_NAME, "routingkey.dead");
        String EXCHANGE_NAME = "exchange.direct";
        String QUEUE_NAME = "queue_name";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        Map<String, Object> arguments = new HashMap<String, Object>();
        // 统一设置队列中的所有消息的过期时间
        arguments.put("x-message-ttl", 30000);
        // 设置超过多少毫秒没有消费者来访问队列，就删除队列的时间
        arguments.put("x-expires", 20000000);
        // 设置队列的最新的 N 条消息，如果超过 N 条，前面的消息将从队列中移除掉
        arguments.put("x-max-length", 4);
        // 设置队列的内容的最大空间，超过该阈值就删除之前的消息
        arguments.put("x-max-length-bytes", 1024);
        // 将删除的消息推送到指定的交换机，一般 x-dead-letter-exchange 和 x-dead-letter-routing- key 需要同时设置
        arguments.put("x-dead-letter-exchange", "exchange.dead");
        // 将删除的消息推送到指定的交换机对应的路由键
        arguments.put("x-dead-letter-routing-key", "routingkey.dead");
        // 设置消息的优先级，优先级大的优先被消费
        arguments.put("x-max-priority", 10);
        channel.queueDeclare(QUEUE_NAME, false, false, false, arguments);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
        String message = "Hello RabbitMQ: ";
        for (int i = 1; i <= 10; i++) {
            // expiration: 设置单条消息的过期时间
            AMQP.BasicProperties.Builder properties = new AMQP.BasicProperties().builder().priority(i).expiration(i * 1000000 + "").deliveryMode(2);
            channel.basicPublish(EXCHANGE_NAME, "", properties.build(), (message + i).getBytes("UTF-8"));
        } channel.close();
        connection.close();

    }
}
