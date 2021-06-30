package com.nekonade.network.param.game.messagedispatcher;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "game.server.log")
@Getter
@Setter
public class LogServerConfig {

    private String logGameMessageTopic = "";

    private String whoAmI = "";
}
