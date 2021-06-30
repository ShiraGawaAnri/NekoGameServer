package com.nekonade.common.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServerInfo {

    private int serviceId; //服务id，与GameMessageMetadata中的一致

    private int serverId;  //服务器id

    private String host;

    private int port;

}
