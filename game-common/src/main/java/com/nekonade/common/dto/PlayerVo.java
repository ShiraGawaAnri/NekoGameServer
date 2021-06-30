package com.nekonade.common.dto;

import com.nekonade.common.basePojo.BasePlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;


/**
 * @ClassName: PlayerViewDTO
 * @Author: Lily
 * @Description: 玩家数据展示类,非战斗状态下
 * @Date: 2021/6/28
 * @Version: 1.0
 */

@Getter
@Setter
public class PlayerVo extends BasePlayer implements Cloneable{

    private Map<String, CharacterVo> characters = new HashMap<>();

}
