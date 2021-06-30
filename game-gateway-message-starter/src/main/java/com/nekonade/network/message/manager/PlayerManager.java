package com.nekonade.network.message.manager;


import com.nekonade.common.gameMessage.DataManager;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.network.message.channel.GameChannel;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

@Getter
public class PlayerManager extends DataManager {

    private final ApplicationContext context;

    private final GameChannel gameChannel;

    private final Player player;//声明数据对象
    private final CharacterManager characterManager; //角色管理类
    private final TaskManager taskManager;
    private final InventoryManager inventoryManager;
    private final StaminaManager staminaManager;
    private final ExperienceManager experienceManager;
    private final DiamondManager diamondManager;

    //声明其它的管理类....
    public PlayerManager(Player player, ApplicationContext applicationContext, GameChannel gameChannel) {//初始化所的管理类
        this.context = applicationContext;
        this.gameChannel = gameChannel;
        this.player = player;
        this.characterManager = new CharacterManager(this);
        this.taskManager = new TaskManager(this);
        this.inventoryManager = new InventoryManager(this);
        this.staminaManager = new StaminaManager(this);
        this.experienceManager = new ExperienceManager(this);
        this.diamondManager = new DiamondManager(this);
    }


}
