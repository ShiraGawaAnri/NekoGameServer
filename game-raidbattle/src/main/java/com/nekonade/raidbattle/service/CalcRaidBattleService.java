package com.nekonade.raidbattle.service;


import com.nekonade.common.basePojo.BaseRaidBattleCharacter;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.dto.*;
import com.nekonade.common.dto.raidbattle.RaidBattleCharacter;
import com.nekonade.common.dto.raidbattle.RaidBattlePlayer;
import com.nekonade.common.dto.raidbattle.vo.RaidBattleDamageVo;
import com.nekonade.common.error.exceptions.GameNotifyException;
import com.nekonade.dao.daos.db.SkillDBDao;
import com.nekonade.dao.daos.db.CharacterDBDao;
import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.dao.db.entity.data.CharacterDB;
import com.nekonade.raidbattle.manager.RaidBattleManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CalcRaidBattleService {

    @Autowired
    private GlobalConfigDao globalConfigDao;

    @Autowired
    private CharacterDBDao charactersDbDao;


    @Getter
    @Setter
    private class Damage {

        private int miss = 0;

        private boolean critical = false;

        private double totalDamage = 0L;

        public void addDamage(long damage) {
            this.totalDamage += damage;
        }

        public void addDamage(double damage) {
            this.totalDamage += damage;
        }

        public void addDamage(int damage) {
            this.totalDamage += damage;
        }
    }

    @Autowired
    private SkillDBDao skillsDbDao;

    public <T extends RaidBattleCharacter> RaidBattleDamageVo calcDamage(RaidBattleManager dataManager, RaidBattlePlayer actionPlayer, T actionSource, String cardId, int targetPos, List<Integer> selectCharaPos, long turn) {
        RaidBattleDamageVo raidBattleDamageVo = new RaidBattleDamageVo();
        String raidId = dataManager.getRaidBattle().getRaidId();
        raidBattleDamageVo.setRaidId(raidId);

        RaidBattleDamageVo.Contribution contribution = new RaidBattleDamageVo.Contribution();
        contribution.setAmount(actionPlayer.getContributePoint());

        raidBattleDamageVo.addScenario(contribution);

        boolean alive = actionSource.isAlive();

        EnumCollections.DataBaseMapper.CharacterProp prop = actionSource.getProp();

        boolean isPlayer = prop == EnumCollections.DataBaseMapper.CharacterProp.Player;

        boolean isEnemy = prop == EnumCollections.DataBaseMapper.CharacterProp.Enemy;

        //这里应该引入计算公式
        //测试随便算
        Damage damage = new Damage();

        long baseDamage = 0;

        Integer atk = actionSource.getAtk();
        baseDamage = atk;
        damage.addDamage(baseDamage);

        List<? extends BaseRaidBattleCharacter> livingTargets;
        if (isPlayer) {
            livingTargets = dataManager.getLivingEnemy(dataManager.getRaidBattle().getEnemies());
        } else {
            livingTargets = dataManager.getLivingCharacter(new ArrayList<>(actionPlayer.getParty().values()));
        }
        //生成演出脚本
        RaidBattleDamageVo.Attack attackScenario = new RaidBattleDamageVo.Attack();
        RaidBattleDamageVo.ModeChange modeChangeScenario = new RaidBattleDamageVo.ModeChange();
        RaidBattleDamageVo.BossGauge bossGaugeScenario = new RaidBattleDamageVo.BossGauge();
        raidBattleDamageVo.addScenario(attackScenario);
        raidBattleDamageVo.addScenario(modeChangeScenario);
        raidBattleDamageVo.addScenario(bossGaugeScenario);
        modeChangeScenario.setGauge(100);
        targetPos = Math.min(livingTargets.size() - 1, targetPos);
        if(targetPos == -1){
            throw GameNotifyException.newBuilder(EnumCollections.CodeMapper.GameErrorCode.RaidBattleHasBeenFinished).data(raidId).build();
        }
        modeChangeScenario.setPos(targetPos);
        bossGaugeScenario.setPos(targetPos);

        BaseRaidBattleCharacter target = livingTargets.get(targetPos);
        RaidBattleDamageVo.Damage damageScenario = new RaidBattleDamageVo.Damage();
        attackScenario.addDamage(damageScenario);
        damageScenario.setPos(targetPos);
        damageScenario.setMiss(damage.getMiss());
        attackScenario.setFrom(prop.getValue());

        //经过一轮计算后得出的atk值
        //测试随便写
        long vDamage = (long)(damage.getTotalDamage() - target.getDef());
        damage.setTotalDamage(vDamage);

        int skillRatio = 100;
        damage.setTotalDamage((damage.getTotalDamage() * skillRatio / 100d));

        long totalDamage = (long) damage.getTotalDamage();
        totalDamage = Math.max(10,totalDamage);
        target.takeDamage(totalDamage);
        bossGaugeScenario.setHp(target.getHp());
        bossGaugeScenario.setMaxHp(target.getMaxHp());
        damageScenario.setValue(totalDamage);

        contribution.addAmount((int) (totalDamage / 2));

        return raidBattleDamageVo;
    }


    public RaidBattleCharacter CalcRaidBattleInitCharacterStatus(CharacterVo source){
        RaidBattleCharacter character = new RaidBattleCharacter();
        CalcRaidBattleInitCharacterStatus(source,character);
        return character;
    }

    public void CalcRaidBattleInitCharacterStatus(CharacterVo source, RaidBattleCharacter target){
        String charaId = source.getCharacterId();
        CharacterDB db = charactersDbDao.findChara(charaId);
        Map<String, GlobalConfig.CharacterConfig.StatusDataBase> statusDataBase = globalConfigDao.getGlobalConfig().getCharacterConfig().getStatusDataBase();
        GlobalConfig.CharacterConfig.StatusDataBase dataBase = statusDataBase.get(db.getCharacterId());
        BeanUtils.copyProperties(source,target);

        //计算Hp
        //HP = Floor ( 100 + HP_JOB_A * BaseLv + HP_JOB_B * ( 1 + 2 + 3 ... + BaseLv ) ) * ( 1 + VIT / 100 )
        if(dataBase == null){
            dataBase = new GlobalConfig.CharacterConfig.StatusDataBase();
        }
        int level = source.getLevel();
        double hpFactor = dataBase.getHpFactor();
        int hp0 = 0;
        for(int i = 0; i < level;i++){
            hp0 += level;
        }
        //随便算算
        //后期可以缓存计算结果
        long hp = (long)((100d + level * hpFactor + Math.pow(hpFactor,2) * hp0) * (1 + level / 100d));
        target.setMaxHp(hp);
        target.setHp(target.getMaxHp());

        double atkFactor = dataBase.getAtkFactor();
        int atk = (int)(db.getBaseAtk() * (1 + Math.pow( atkFactor,2) + level / 100d));
        target.setAtk(atk);

        double defFactor = dataBase.getDefFactor();
        int def = (int)(db.getBaseDef() * (1 + Math.pow(defFactor,1.5) + level / 100d));
        target.setDef(def);
    }
}
