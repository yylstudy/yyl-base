package com.cqt.client.websocket;

import com.cqt.client.config.nacos.CloudNettyProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author chw
 * @Date 20230615
 * @Description websocket通道初始化器
 **/
@Component
public class WebsocketChannelInitializer extends ChannelInitializer<SocketChannel> {
 
    @Autowired
    private WebSocketHandler webSocketHandler;

    @Autowired
    private ChannelAuthHandler channelAuthHandler;
    @Resource
    private CloudNettyProperties cloudNettyProperties;
 
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        String websocketUrl = cloudNettyProperties.getUrl();
        //获取pipeline通道
        ChannelPipeline pipeline = socketChannel.pipeline();
        //因为基于http协议，使用http的编码和解码器
        pipeline.addLast(new HttpServerCodec());
        //是以块方式写，添加ChunkedWriteHandler处理器
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new IdleStateHandler(cloudNettyProperties.getReadOutTime(), 0, 0));
        /*
          说明
          1. http数据在传输过程中是分段, HttpObjectAggregator ，就是可以将多个段聚合
          2. 这就就是为什么，当浏览器发送大量数据时，就会发出多次http请求
        */
        pipeline.addLast(new HttpObjectAggregator(8192));
        /* 说明
          1. 对应websocket ，它的数据是以 帧(frame) 形式传递
          2. 可以看到WebSocketFrame 下面有六个子类
          3. 浏览器请求时 ws://localhost:7000/msg 表示请求的uri
          4. WebSocketServerProtocolHandler 核心功能是将 http协议升级为 ws协议 , 保持长连接
          5. 是通过一个 状态码 101
        */
        pipeline.addLast(channelAuthHandler);
        pipeline.addLast(new WebSocketServerProtocolHandler(websocketUrl));
        //自定义的handler ，处理业务逻辑
        pipeline.addLast(webSocketHandler);
    }

}
