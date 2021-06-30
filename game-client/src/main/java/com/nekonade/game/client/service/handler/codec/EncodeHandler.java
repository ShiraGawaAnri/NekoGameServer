package com.nekonade.game.client.service.handler.codec;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.utils.AESUtils;
import com.nekonade.common.utils.CompressUtils;
import com.nekonade.common.utils.JacksonUtils;
import com.nekonade.game.client.service.GameClientConfig;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.common.gameMessage.HeaderAttribute;
import com.nekonade.common.gameMessage.IGameMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * @ClassName: EncodeHandler
 * @Description: 客户端编码类
 */
public class EncodeHandler extends MessageToByteEncoder<IGameMessage> {
    /**
     * 发送消息的包头总长度：
     * 消息总长度(4) +
     * 客户端消息序列号长度(4) +
     * 消息请求ID长度（4） +
     * 服务ID(2) +
     * 客户端发送时间长度(8) +
     * 协议版本长度(4) +
     * 包头长度(4) +
     * 包头内容
     * HmacMd5校验长度(2)
     * hmacMd5内容
     * 是否压缩长度(1) +
     * body内容
     */
    //
    private final static Logger logger = LoggerFactory.getLogger(EncodeHandler.class);

    //private static final int GAME_MESSAGE_HEADER_LEN = 27;
    private static final int GAME_MESSAGE_HEADER_LEN = 33;
    private final GameClientConfig gameClientConfig;
    @Setter
    private String aesSecretKey;//对称加密的密钥
    private int seqId;//消息序列号

    public EncodeHandler(GameClientConfig gameClientConfig, ApplicationContext context) {
        this.gameClientConfig = gameClientConfig;
    }

    private String createParam(IGameMessage msg,byte[] body){
        GameMessageHeader header = msg.getHeader();
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
    protected void encode(ChannelHandlerContext ctx, IGameMessage msg, ByteBuf out) throws Exception {
        int messageSize = GAME_MESSAGE_HEADER_LEN;// 标记数据包的总大小
        byte[] body = msg.body();
        byte[] originBody = msg.body();
        int compress = 0;//标记包体是否进行了压缩
        if (body != null) {
            if (body.length >= gameClientConfig.getMessageCompressSize()) { // 从配置中获取达到压缩的包体的最小大小。
                body = CompressUtils.compress(body);//包体大小达到压缩的最上值时，对包体进行压缩
                compress = 1;
            }
            if (this.aesSecretKey != null && msg.getHeader().getMessageId() != EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()) {
                //密钥不为空，对消息体加密
                body = AESUtils.encode(aesSecretKey, body);
            }
            messageSize += body.length;//加上包体的长度，得到数据包的总大小。
        }
        GameMessageHeader header = msg.getHeader();
        int messageId = header.getMessageId();
        header.setClientSeqId(++seqId);
        header.setVersion(gameClientConfig.getVersion());
        header.setClientSendTime(System.currentTimeMillis());


        HeaderAttribute attribute = msg.getHeader().getAttribute();
        //String attributeJson = JSON.toJSONString(attribute);
        attribute.addLog();
        String attributeJson = JacksonUtils.toJSONStringV2(attribute);
        byte[] headerAttBytes = attributeJson.getBytes();
        messageSize += headerAttBytes.length;



        byte[] verificationBytes = "".getBytes();
        if(this.aesSecretKey != null && messageId != EnumCollections.CodeMapper.GatewayMessageCode.ConnectConfirm.getMessageId()){
            /*long start = System.nanoTime();
            String param = createParam(msg,originBody);
            String result = HmacUtils.encryptHmacMD5(param.getBytes(), HmacUtils.getHmacMd5Key(aesSecretKey));
            verificationBytes = result.getBytes();
            long end = System.nanoTime();*/
        }
        messageSize += verificationBytes.length;

        out.writeInt(messageSize);// 依次写入包头数据。
        out.writeInt(header.getClientSeqId());
        out.writeInt(header.getMessageId());
        out.writeShort(header.getServiceId());
        out.writeLong(header.getClientSendTime());
        out.writeInt(header.getVersion());// 从配置中获取客户端版本
        out.writeInt(headerAttBytes.length);
        out.writeBytes(headerAttBytes);
        out.writeShort(verificationBytes.length);
        out.writeBytes(verificationBytes);
        out.writeByte(compress);
        if (body != null) {//如果包体不为空，写入包体数据
            out.writeBytes(body);
        }

        //ReferenceCountUtil.release(out);
    }

}
