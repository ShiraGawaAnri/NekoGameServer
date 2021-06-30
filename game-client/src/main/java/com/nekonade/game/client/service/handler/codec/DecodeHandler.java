package com.nekonade.game.client.service.handler.codec;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.gameMessage.HeaderAttribute;
import com.nekonade.common.utils.AESUtils;
import com.nekonade.common.utils.CompressUtils;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.utils.JacksonUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @ClassName: DecodeHandler
 * @Description: 客户端解码类
 */
public class DecodeHandler extends ChannelInboundHandlerAdapter {
    
    private static final Logger logger = LoggerFactory.getLogger(DecodeHandler.class);
    
    private final ApplicationContext context;


    @Setter
    private String aesSecretKey;// 对称加密的密钥

    public DecodeHandler(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        try {
            int messageSize = buf.readInt(); // 根据协议，依次读取包头的信息
            int clientSeqId = buf.readInt();
            int messageId = buf.readInt();
            long serverSendTime = buf.readLong();
            int version = buf.readInt();
            int errorCode = buf.readInt();
            long playerId = buf.readLong();
            int headerAttrLength = buf.readInt();
            HeaderAttribute headerAttr = null;
            if (headerAttrLength > 0) {//读取包头属性
                byte[] headerAttrBytes = new byte[headerAttrLength];
                buf.readBytes(headerAttrBytes);
                String headerAttrJson = new String(headerAttrBytes);
                headerAttr = JacksonUtils.parseObjectV2(headerAttrJson,HeaderAttribute.class);
            }
            int compress = buf.readByte();
            byte[] body = null;
            if (errorCode == 0 && buf.readableBytes() > 0) {// 读取包体数据
                body = new byte[buf.readableBytes()];// 剩下的字节都是body数据
                buf.readBytes(body);
                if (this.aesSecretKey != null && messageId != EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()) {// 如果对称加密 密钥不为null，对消息解密
                    body = AESUtils.decode(aesSecretKey, body);
                }
                if (compress == 1) {// 如果包体压缩了，接收时需要解压

                    body = CompressUtils.decompress(body);
                }
            }
            GameMessageHeader header = new GameMessageHeader();
            header.setAttribute(headerAttr);
            header.setMessageSize(messageSize);
            header.setMessageId(messageId);
            header.setClientSeqId(clientSeqId);
            header.setServerSendTime(serverSendTime);
            header.setVersion(version);
            header.setErrorCode(errorCode);
            header.setPlayerId(playerId);
            GameMessagePackage gameMessagePackage = new GameMessagePackage();// 构造数据包
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(body);
            logger.trace("接收服务器消息,大小：{}:<-{}", messageSize, header);
            header.getAttribute().addLog();
            header.getAttribute().showLog(header);
            ctx.fireChannelRead(gameMessagePackage);// 将解码出来的消息发送到后面的Handler。
        } finally {// 这里做了判断，如果buf不是从堆内存分配，还是从直接内存中分配的，需要手动释放，否则，会造成内存泄露。
            ReferenceCountUtil.release(buf);
        }
    }

}
