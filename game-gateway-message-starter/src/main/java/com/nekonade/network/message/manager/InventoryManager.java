package com.nekonade.network.message.manager;


import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.ItemDTO;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.dao.daos.db.ItemDBDao;
import com.nekonade.dao.db.entity.Inventory;
import com.nekonade.dao.db.entity.Item;
import com.nekonade.dao.db.entity.data.ItemDB;
import com.nekonade.network.message.event.function.ItemAddEvent;
import com.nekonade.network.message.event.function.ItemSubEvent;
import com.nekonade.network.message.event.function.TriggerSystemSendMailEvent;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InventoryManager {

    private final PlayerManager playerManager;

    private final ApplicationContext context;

    private final ItemDBDao itemsDbDao;

    @Getter
    private final Inventory inventory;

    public InventoryManager(PlayerManager playerManager) {
        this.context = playerManager.getContext();
        this.playerManager = playerManager;
        this.inventory = playerManager.getPlayer().getInventory();
        this.itemsDbDao = context.getBean(ItemDBDao.class);
    }

    public ConcurrentHashMap<String, Item> getItemMap() {
        return inventory.getItemMap();
    }


    public Item getItem(String itemId) {
        return inventory.getItemMap().get(itemId);
    }

    public ItemDB getItemDb(String itemId){
        return itemsDbDao.findItemDb(itemId);
    }

    public boolean checkItemExist(String itemId){
        return getItemDb(itemId) != null;
    }

    public void checkItemEnough(String itemId, int needCount) {
        Map<String, Integer> costMap = new HashMap<>();
        costMap.put(itemId, needCount);
        this.checkItemEnough(costMap);
    }

    public boolean checkItemEnough(Map<String, Integer> costMap) {
        Set<Map<String, Integer>> collect = costMap.keySet().stream().map(itemId -> {
            Integer needCount = costMap.get(itemId);
            Item item = this.getItem(itemId);
            if (item == null || item.getAmount() == null || item.getAmount() < needCount) {
                Map<String, Integer> map = new HashMap<>();
                map.put(itemId, (item == null ? needCount : (item.getAmount() == null ? needCount : needCount - item.getAmount())));
                return map;
            }
            return null;
        }).collect(Collectors.toSet());
        collect.removeAll(Collections.singletonList(null));
        if (collect.size() > 0) {
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.StageCostItemNotEnough).data(collect).build();
        }
        return true;
    }

    public boolean checkOverFlow(String itemId,int addValue){

        ItemDB itemDb = getItemDb(itemId);
        if(itemDb == null){
            return true;
        }

        Item item = this.getItem(itemId);
        if(item == null){
            return false;
        }
        int count = item.getAmount() == null ? 0 : item.getAmount();

        ItemDB.Stack stack = itemDb.getStack();

        return (count + addValue) > stack.getMaxAmount();
    }

    /*public boolean produceItem(String itemId, int amount) {
        if(checkOverFlow(itemId,amount)) return false;
        ItemAddEvent itemAddEvent = new ItemAddEvent(this, playerManager, itemId, amount);
        context.publishEvent(itemAddEvent);
        return true;
    }*/

    public Item produceItem(String itemId, int amount) {
        if(checkOverFlow(itemId,amount)){
            Item item = new Item();
            item.setItemId(itemId);
            item.setAmount(amount);
            return item;
        }
        ItemAddEvent itemAddEvent = new ItemAddEvent(this, playerManager, itemId, amount);
        context.publishEvent(itemAddEvent);
        return null;
    }

    public void produceItemWithOverFlowProcess(String itemId, int amount) {
        if(checkOverFlow(itemId,amount)){
            ItemDTO item = new ItemDTO();
            item.setItemId(itemId);
            item.setAmount(amount);
            List<ItemDTO> list  = new ArrayList<>();
            list.add(item);
            TriggerSystemSendMailEvent triggerEvent = new TriggerSystemSendMailEvent(this, playerManager, list);
            context.publishEvent(triggerEvent);
            return;
        }
        ItemAddEvent itemAddEvent = new ItemAddEvent(this, playerManager, itemId, amount);
        context.publishEvent(itemAddEvent);
    }

    public void consumeItem(Map<String, Integer> costMap) {
        costMap.keySet().forEach(each -> {
            this.consumeItem(each, costMap.get(each));
        });
    }

    public void consumeItem(String itemId, int count) {
        ItemSubEvent itemSubEvent = new ItemSubEvent(this, playerManager, itemId, count);
        context.publishEvent(itemSubEvent);
    }
}
