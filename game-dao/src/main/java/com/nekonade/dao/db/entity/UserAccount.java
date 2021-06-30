package com.nekonade.dao.db.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Document(collection = "UserAccount")
@Getter
@Setter
public class UserAccount {
    @Id
    private long userId;

    @Indexed(name = "openId", unique = true, sparse = true)
    private String openId;

    @Indexed(name = "username", unique = true, sparse = true)
    private String username;

    private String password;

    private long createTime;

    private String registIp;

    private String lastLoginIp;
    // 记录已创建角色的基本信息
    private Map<String, ZonePlayerInfo> zonePlayerInfo = new ConcurrentHashMap<>();

    private long group;


    @Override
    public String toString() {
        return "UserAccount [openId=" + openId + ", userId=" + userId + ", createTime=" + createTime + ", registIp=" + registIp + ", lastLoginIp=" + lastLoginIp + ", zonePlayerInfo=" + zonePlayerInfo + "]";
    }


    public static class ZonePlayerInfo {
        private long playerId;//此区内的角色Id
        private long lastEnterTime;//最近一次进入此区的时间

        public ZonePlayerInfo() {
        }

        public ZonePlayerInfo(long playerId, long lastEnterTime) {
            super();
            this.playerId = playerId;
            this.lastEnterTime = lastEnterTime;
        }

        public long getPlayerId() {
            return playerId;
        }

        public void setPlayerId(long playerId) {
            this.playerId = playerId;
        }

        public long getLastEnterTime() {
            return lastEnterTime;
        }

        public void setLastEnterTime(long lastEnterTime) {
            this.lastEnterTime = lastEnterTime;
        }

        @Override
        public String toString() {
            return "ZoneInfo [playerId=" + playerId + ", lastEnterTime=" + lastEnterTime + "]";
        }


    }


}
