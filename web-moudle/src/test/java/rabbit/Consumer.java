package rabbit;

import com.example.util.RabbitUtils;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Consumer {
    public static void main(String[] args) throws Exception {
        Connection connection = RabbitUtils.getConnection();
        final Channel channel = connection.createChannel();
        Map<String, Object> arguments = new HashMap<String, Object>();
        // 统一设置队列中的所有消息的过期时间
        arguments.put("x-message-ttl", 30000);
        // 设置超过多少毫秒没有消费者来访问队列，就删除队列的时间
        arguments.put("x-expires", 20000000);
        // 设置队列的最新的N条消息，如果超过N条，前面的消息将从队列中移除掉
        arguments.put("x-max-length", 4);
        // 设置队列的内容的最大空间，超过该阈值就删除之前的消息
        arguments.put("x-max-length-bytes", 1024);
        // 将删除的消息推送到指定的交换机，一般x-dead-letter-exchange和x-dead-letter-routing-key需要同时设置
        arguments.put("x-dead-letter-exchange", "exchange.dead");
        // 将删除的消息推送到指定的交换机对应的路由键
        arguments.put("x-dead-letter-routing-key", "routingkey.dead");
        // 设置消息的优先级，优先级大的优先被消费
        arguments.put("x-max-priority", 10);
        channel.queueDeclare("queue_name", false, false, false, arguments);
        //queueBind用于将队列与交换机绑定
        //参数1：队列名 参数2：交互机名  参数三：路由key（暂时用不到)
//        channel.queueBind(RabbitConstant.QUEUE, RabbitConstant.EXCHANGE_WEATHER, "");
        channel.basicQos(1);
        channel.basicConsume("queue_name", false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
               log.info("接收到的消息：" + new String(body));
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        });
    }
}

