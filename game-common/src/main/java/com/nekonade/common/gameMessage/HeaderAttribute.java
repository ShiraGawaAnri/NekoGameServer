package com.nekonade.common.gameMessage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Transient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: HeaderAttribute
 * @Author: Lily
 * @Description: 做为包头的扩展类，因为网关是转发消息的，有些客户端的数据也需要一起转发出去，为了扩展时，不修改协议的编码和解码，都放在这个类里面
 * 这个类在序列化为会使用json序列化。
 * @Date: 2021/6/28
 * @Version: 1.0
 */
@Getter
@Setter
@ToString
@Slf4j
public class HeaderAttribute {

    private String clientIp;//客户端ip

    private String raidId;

    private List<Long> broadIds;

    //记录追踪用
    @Transient
    private Map<String,Long> logMap = new LinkedHashMap<>();

    public void addLog(){
        this.addLog("");
    }

    public void addLog(String afterfix){
        long logTime = System.currentTimeMillis();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        /*for (StackTraceElement e : stackTrace) {
            log.info("{}\t{}\t{}\t{}",
                    e.getClassName(),
                    e.getMethodName(),
                    e.getLineNumber(),
                    logTime);
        }*/
        StackTraceElement logTag = stackTrace[1];
        String tag = null;
        for (int i = 1; i < stackTrace.length; i++) {
            StackTraceElement e = stackTrace[i];
            if (!e.getClassName().equals(logTag.getClassName())) {
                tag = e.getClassName() + "." + e.getMethodName();
                break;
            }
        }
        if (tag == null) {
            tag = logTag.getClassName() + "." + logTag.getMethodName();

        }
        if(StringUtils.isNotEmpty(afterfix)){
            tag += "_" + afterfix;
        }
        //log.info("{}\t{}",tag,logTime);
        this.logMap.put(tag, logTime);
    }

    public void setLogMap(Map<String, Long> logMap) {
        this.logMap = logMap;

    }
    public void showLog(GameMessageHeader header){
        this.showLog(header,false);
    }

    public void showLog(GameMessageHeader header,Boolean forceShow)
    {
        Long time = Math.abs(System.currentTimeMillis() - header.getClientSendTime());
        Long longTime = 3000L;
        if(!forceShow){
            if(time == 0 || time >= 1600000000000L){
                //log.debug("MessageId:{} [ServerSend] Player:{} RaidId:{}",header.getMessageId(),header.getPlayerId(),this.getRaidId());
            }else if(time >= longTime){
                log.warn("MessageId:{} Time:{} Player:{} RaidId:{} 处理有较大延迟(>={})",header.getMessageId(),time,header.getPlayerId(),this.getRaidId(),longTime);
                log.info("Player:{} LogMap -> \t {}",header.getPlayerId(),logMap.toString());
            }else{
                log.debug("MessageId:{} Time:{} Player:{} RaidId:{}",header.getMessageId(),time,header.getPlayerId(),this.getRaidId());
            }
        }else{
            if(time == 0 || time >= 1600000000000L){
                //log.debug("MessageId:{} [ServerSend] Player:{} RaidId:{}",header.getMessageId(),header.getPlayerId(),this.getRaidId());
            }else if(time >= longTime){
                log.info("LagMessageId:{} Time:{} Player:{} RaidId:{} 处理有较大延迟(>={})",header.getMessageId(),time,header.getPlayerId(),this.getRaidId(),longTime);
                log.info("Player:{} LogMap -> \t {}",header.getPlayerId(),logMap.toString());
            }else{
                log.info("MessageId:{} Time:{} Player:{} RaidId:{}",header.getMessageId(),time,header.getPlayerId(),this.getRaidId());
            }
        }


    }
}
