package com.nekonade.gamegateway.server.handler.codec;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.utils.AESUtils;
import com.nekonade.common.utils.CompressUtils;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.GameMessagePackage;
import com.nekonade.common.gameMessage.HeaderAttribute;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class DecodeHandler extends ChannelInboundHandlerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(DecodeHandler.class);

    private final ApplicationContext context;


    @Setter
    private String aesSecretKey;//对称加密密钥

    public DecodeHandler(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    private String createParam(GameMessageHeader header, byte[] body){
        HeaderAttribute attribute = header.getAttribute();
        String clientIp = attribute.getClientIp();
        String raidId = attribute.getRaidId();
        int clientSeqId = header.getClientSeqId();
        long clientSendTime = header.getClientSendTime();
        int messageId = header.getMessageId();
        long playerId = header.getPlayerId();
        int version = header.getVersion();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer
                .append(clientIp).append("_")
                .append(raidId).append("_")
                .append(clientSeqId).append("_")
                .append(clientSendTime).append("_")
                .append(messageId).append("_")
                .append(playerId).append("_")
                .append(version).append("_")
                .append(Arrays.toString(body));
        return stringBuffer.toString();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            int messageSize = byteBuf.readInt();
            int clientSeqId = byteBuf.readInt();
            int messageId = byteBuf.readInt();
            int serviceId = byteBuf.readShort();
            long clientSendTime = byteBuf.readLong();
            int version = byteBuf.readInt();
            int headerAttrLength = byteBuf.readInt();

            HeaderAttribute headerAttr = null;
            if (headerAttrLength > 0) {//读取包头属性
                byte[] headerAttrBytes = new byte[headerAttrLength];
                byteBuf.readBytes(headerAttrBytes);
                String headerAttrJson = new String(headerAttrBytes);
                //headerAttr = JSON.parseObject(headerAttrJson, HeaderAttribute.class);
                headerAttr = JacksonUtils.parseObjectV2(headerAttrJson,HeaderAttribute.class);
            }

            int verificationLength = byteBuf.readShort();
            String verification = "";
            if(verificationLength > 0){
                byte[] verificationBytes = new byte[verificationLength];
                byteBuf.readBytes(verificationBytes);
                verification = new String(verificationBytes);
            }

            int compress = byteBuf.readByte();
            byte[] body = null;
            if (byteBuf.readableBytes() > 0) {
                body = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(body);
                if (this.aesSecretKey != null && messageId != EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()) {//如果密钥不为空，且不是认证消息，对消息体解密
                    body = AESUtils.decode(aesSecretKey, body);
                }
                if (compress == 1) {
                    body = CompressUtils.decompress(body);
                }
            }
            GameMessageHeader header = new GameMessageHeader();
            headerAttr.addLog();
            header.setAttribute(headerAttr);
            header.setClientSendTime(clientSendTime);
            header.setClientSeqId(clientSeqId);
            header.setMessageId(messageId);
            header.setServiceId(serviceId);
            header.setMessageSize(messageSize);
            header.setVersion(version);
            header.getAttribute().addLog();
            GameMessagePackage gameMessagePackage = new GameMessagePackage();
            gameMessagePackage.setHeader(header);
            gameMessagePackage.setBody(body);
            if(this.aesSecretKey != null && messageId != EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()){
                /*long start = System.nanoTime();
                String param = createParam(header, body);
                String result = HmacUtils.encryptHmacMD5(param.getBytes(), HmacUtils.getHmacMd5Key(aesSecretKey));
                long end = System.nanoTime();
                if(!result.equals(verification)){
                    return;
                }*/
            }
            ctx.fireChannelRead(gameMessagePackage);
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }
}
