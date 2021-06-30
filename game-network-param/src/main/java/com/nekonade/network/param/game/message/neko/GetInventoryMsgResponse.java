package com.nekonade.network.param.game.message.neko;


import com.nekonade.common.dto.ItemDTO;
import com.nekonade.common.dto.WeaponDTO;
import com.nekonade.common.gameMessage.AbstractJsonGameMessage;
import com.nekonade.common.gameMessage.EnumMessageType;
import com.nekonade.common.gameMessage.GameMessageMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ConcurrentHashMap;

@GameMessageMetadata(messageId = 203, messageType = EnumMessageType.RESPONSE, serviceId = 101)
public class GetInventoryMsgResponse extends AbstractJsonGameMessage<GetInventoryMsgResponse.Inventory> {

    @Override
    protected Class<Inventory> getBodyObjClass() {
        return Inventory.class;
    }

    @Getter
    @Setter
    public static class Inventory {
        //武器包
        private ConcurrentHashMap<String, WeaponDTO> weaponMap;
        //道具包
        private ConcurrentHashMap<String, ItemDTO> itemMap;
    }
}
