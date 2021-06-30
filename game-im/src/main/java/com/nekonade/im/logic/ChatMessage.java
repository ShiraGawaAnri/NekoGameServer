package com.nekonade.im.logic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@Setter
@ToString
public class ChatMessage {

    private long seqId;

    private long playerId;

    private String nickName;

    private String chatMessage;

    private int clientSeqId;

    private long clientSendTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChatMessage that = (ChatMessage) o;

        return new EqualsBuilder()
                .append(seqId, that.seqId)
                .append(playerId, that.playerId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(seqId)
                .append(playerId)
                .toHashCode();
    }
}
