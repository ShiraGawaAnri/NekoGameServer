package com.nekonade.neko.service;

import com.nekonade.dao.daos.db.ItemDBDao;
import com.nekonade.dao.db.entity.Inventory;
import com.nekonade.dao.db.entity.Item;
import com.nekonade.dao.db.entity.data.ItemDB;
import com.nekonade.network.message.event.function.ItemAddEvent;
import com.nekonade.network.message.event.function.ItemSubEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ItemDBDao itemsDbDao;

    private ItemDB preInventoryItem(Inventory inventory, String itemId) {
        ItemDB itemsDB = itemsDbDao.findItemDb(itemId);
        if (itemsDB == null) return null;
        inventory.getItemMap().computeIfAbsent(itemId, iid -> {
            Item item = new Item();
            BeanUtils.copyProperties(itemsDB, item);
            item.setAmount(0);
            return item;
        });
        inventory.getItemMap().computeIfPresent(itemId, (iid, it) -> {
            if(it.getAmount() == null) it.setAmount(0);
            return it;
        });
        return itemsDB;
    }

    @EventListener
    public void itemAddEvent(ItemAddEvent event) {
        Inventory inventory = event.getPlayerManager().getInventoryManager().getInventory();
        String itemId = event.getItemId();
        int count = event.getAmount();
        ItemDB itemsDB = this.preInventoryItem(inventory, itemId);
        if (itemsDB == null) return;
        inventory.getItemMap().computeIfPresent(itemId, (iid, it) -> {
            it.setAmount((int) Math.min(itemsDB.getStack().getMaxAmount(), (it.getAmount() + count)));
            return it;
        });
    }

    @EventListener
    public void itemSubEvent(ItemSubEvent event) {
        Inventory inventory = event.getPlayerManager().getInventoryManager().getInventory();
        String itemId = event.getItemId();
        int count = event.getCount();
        ItemDB itemsDB = this.preInventoryItem(inventory, itemId);
        if (itemsDB == null) return;
        inventory.getItemMap().computeIfPresent(itemId, (iid, it) -> {
            it.setAmount(Math.max(0, (it.getAmount() - count)));
            return it;
        });
    }
}
