package com.nekonade.network.message.manager;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.dao.db.entity.Character;
import com.nekonade.dao.db.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class CharacterManager {//角色管理类

    private static final Logger logger = LoggerFactory.getLogger(CharacterManager.class);
    private final PlayerManager playerManager;
    private final ApplicationContext context;
    private final Map<String, Character> characters;//角色数据集合对象
    private final Player player;//角色对象，有些日志和事件记录需要这个对象。

    public CharacterManager(PlayerManager playerManager) {
        this.context = playerManager.getContext();
        this.playerManager = playerManager;
        this.player = playerManager.getPlayer();
        this.characters = player.getCharacters();
    }

    public boolean checkCharaExist(String charaId){
        return this.characters.get(charaId) != null;
    }

    public void addChara(Character character) {
        String charaId = character.getCharacterId();
        if(checkCharaExist(charaId)){
            throw GameErrorException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.CharacterExistedCanNotAdd).build();
        }
        this.characters.put(charaId, character);
    }

    public Map<String, Character> getCharacterMap(){
        return player.getCharacters();
    }

    public Character getChara(String charaId) {
        Character character = this.characters.get(charaId);
        return character;
    }

}
