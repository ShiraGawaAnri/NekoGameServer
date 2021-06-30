package com.nekonade.center.service;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.JWTUtil;
import com.nekonade.dao.daos.db.GlobalConfigDao;
import com.nekonade.dao.daos.PlayerDao;
import com.nekonade.dao.db.entity.Player;
import com.nekonade.dao.db.entity.Stamina;
import com.nekonade.dao.db.entity.config.GlobalConfig;
import com.nekonade.network.param.http.request.SelectGameGatewayParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GlobalConfigDao globalConfigDao;
    @Autowired
    private PlayerDao playerDao;

    private boolean saveNickNameIfAbsent(String zoneId, String nickName) {
        String key = this.getNickNameRedisKey(zoneId, nickName);
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "0", EnumRedisKey.PLAYER_NICKNAME.getTimeout());// value先使用一个默认值
        if (result == null) {
            return false;
        }
        Player entity = playerDao.findByNickName(nickName);
        return entity == null;
    }

    private String getNickNameRedisKey(String zoneId, String nickName) {
        return EnumRedisKey.PLAYER_NICKNAME.getKey(zoneId + "_" + nickName);
    }

    private void updatePlayerIdForNickName(String zoneId, String nickName, long playerId) {
        String key = this.getNickNameRedisKey(zoneId, nickName);
        this.redisTemplate.opsForValue().set(key, String.valueOf(playerId));
    }

    public Player createPlayer(String zoneId, String nickName) {
        boolean saveNickName = this.saveNickNameIfAbsent(zoneId, nickName);
        if (!saveNickName) {// 如果存储失败，抛出错误异常
            throw new GameErrorException.Builder(EnumCollections.CodeMapper.GameCenterError.NICKNAME_EXIST).message(nickName).build();
        }
        long playerId = this.nextPlayerId(zoneId);//获取一个全局playerId。
        long now = System.currentTimeMillis();
        //TODO: 创建角色时,如客户端已经链接上了Socket，会使得此处被创建的角色数据被覆盖
        // 因此,需要考虑是否断开链接,或者请求GameGateway进行操作
        Player player = new Player();
        player.setPlayerId(playerId);
        player.setZoneId(zoneId);
        player.setNickName(nickName);
        player.setLastLoginTime(now);
        player.setCreateTime(player.getLastLoginTime());
        GlobalConfig globalConfig = globalConfigDao.getGlobalConfig();
        Stamina stamina = player.getStamina();
        stamina.setValue(globalConfig.getStaminaConfig().getDefaultStarterValue());
        stamina.setPreQueryTime(now);
        stamina.setNextRecoverTime(globalConfig.getStaminaConfig().getRecoverTime());
        stamina.setNextRecoverTimestamp(globalConfig.getStaminaConfig().getRecoverTime() + now);
        this.updatePlayerIdForNickName(zoneId, nickName, playerId);// 再次更新一下nickName对应的playerId
        playerDao.saveOrUpdate(player, playerId);
        logger.info("创建角色成功,{}", player);
        return player;
    }


    private long nextPlayerId(String zoneId) {
        String key = EnumRedisKey.PLAYER_ID_INCR.getKey();
        redisTemplate.opsForValue().setIfAbsent(key, "50000000");// value先使用一个默认值
        return redisTemplate.opsForValue().increment(key);
    }

    public String createToken(SelectGameGatewayParam param, String gatewayIp, String publicKey) {
        String username = param.getUsername();
        String openId = param.getOpenId();
        String zoneId = param.getZoneId();
        long userId = param.getUserId();
        long playerId = param.getPlayerId();

        String token = JWTUtil.getUserTokenV2(openId, userId, playerId, zoneId, username, gatewayIp, publicKey);
        return token;
    }

}
