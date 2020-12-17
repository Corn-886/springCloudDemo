package netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端实现逻辑
 */
@Slf4j
public class clientHandle extends SimpleChannelInboundHandler<ByteBuf> {
    /**
     * 读取数据后进行处理
     *
     * @param channelHandlerContext
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        log.info("读到消息" + byteBuf.toString(CharsetUtil.UTF_8));
    }

    /**
     * channnel 活跃后做业务处理
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hollow netty", CharsetUtil.UTF_8));
    }
}
