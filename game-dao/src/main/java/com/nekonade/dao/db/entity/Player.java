package com.nekonade.dao.db.entity;

import com.nekonade.common.basePojo.BasePlayer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString
@Document(collection = "Player")
public class Player extends BasePlayer {

    @Id
    private long playerId;

    private Stamina stamina = new Stamina();

    private Diamond diamond = new Diamond();

    private Experience experience = new Experience();

    private Long lastLoginTime;

    private Long createTime = System.currentTimeMillis();

    private Map<String, Character> characters = new ConcurrentHashMap<>();

    private Map<String, Integer> map = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String,Task> tasks = new ConcurrentHashMap<>();

    private String zoneId;
    //背包
    private Inventory inventory = new Inventory();
    //疲劳值,耐久力

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return new EqualsBuilder()
                .append(playerId, player.playerId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(playerId)
                .toHashCode();
    }
}
