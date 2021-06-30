package com.nekonade.game.client.command;

import com.nekonade.common.dto.raidbattle.vo.RaidBattleVo;
import com.nekonade.common.gameMessage.GameMessageHeader;
import com.nekonade.game.client.common.PlayerInfo;
import com.nekonade.game.client.common.RaidBattleInfo;
import com.nekonade.game.client.service.GameClientBoot;
import com.nekonade.game.client.service.GameClientConfig;
import com.nekonade.network.param.game.message.DoConfirmMsgRequest;
import com.nekonade.network.param.game.message.battle.JoinRaidBattleMsgRequest;
import com.nekonade.network.param.game.message.battle.RaidBattleCardAttackMsgRequest;
import com.nekonade.network.param.game.message.neko.*;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;


/**
 * @ClassName: GameClientCommand
 * @Author: Lily
 * @Description: 客户端输入指令
 * @Date: 2021/6/30
 * @Version: 1.0
 */
@ShellComponent
public class GameClientCommand {

    private static final Logger logger = LoggerFactory.getLogger(GameClientCommand.class);

    @Autowired
    private GameClientBoot gameClientBoot;
    @Autowired
    private GameClientConfig gameClientConfig;

    @Autowired
    private PlayerInfo playerInfo;

    @Autowired
    private RaidBattleInfo raidBattleVo;


    @ShellMethod("连接服务器，格式：cs [port]")//连接服务器命令，
    public void cs() {
        connectServer("127.0.0.1", 16002);
    }

    @ShellMethod("连接服务器，格式：cs [host] [port]")//连接服务器命令，
    public void connectServer(@ShellOption(defaultValue = "127.0.0.1") String host, @ShellOption(defaultValue = "0") int port) {
        if (!host.isEmpty()) {//如果默认的host不为空，说明是连接指定的host，如果没有指定host，使用配置中的默认host和端口
            if (port == 0) {
                logger.error("请输入服务器端口号");
                return;
            }
            gameClientConfig.setDefaultGameGatewayHost(host);
            gameClientConfig.setDefaultGameGatewayPort(port);
        }
        gameClientBoot.launch();// 启动客户端并连接游戏网关
    }

    @ShellMethod("关闭连接")
    public void close() {
        gameClientBoot.getChannel().close();
    }

    @ShellMethod("发送测试消息，格式：msg 消息号 附加值")
    public void msg(int messageId){
        msg2(messageId, (Object[]) null);
    }

    @ShellMethod("发送测试消息，格式：msg2 消息号")
    public void msg2(int messageId,Object ...values) {

        if (messageId == 1) {//发送认证请求
            DoConfirmMsgRequest request = new DoConfirmMsgRequest();
            GameMessageHeader header = request.getHeader();
            header.setClientSendTime(System.currentTimeMillis());
            request.getBodyObj().setToken(gameClientConfig.getGatewayToken());
            //            GameMessageHeader header = new GameMessageHeader();
            //            GateRequestMessage gateRequestMessage = new GateRequestMessage(header,Unpooled.wrappedBuffer(request.write()),"");
            gameClientBoot.getChannel().writeAndFlush(request);
        }
//        if (messageId == 10001) {
//            // 向服务器发送一条消息
//            FirstMsgRequest request = new FirstMsgRequest();
//            request.setValue("Hello,server !!");
//            request.getHeader().setClientSendTime(System.currentTimeMillis());
//            gameClientBoot.getChannel().writeAndFlush(request);
//        }
//        if (messageId == 10002) {
//            SecondMsgRequest request = new SecondMsgRequest();
//            request.getBodyObj().setValue1("你好，这是测试请求");
//            request.getBodyObj().setValue2(System.currentTimeMillis());
//            gameClientBoot.getChannel().writeAndFlush(request);
//        }
//        if (messageId == 10003) {
//            ThirdMsgRequest request = new ThirdMsgRequest();
//            ThirdMsgBody.ThirdMsgRequestBody requestBody = ThirdMsgBody.ThirdMsgRequestBody.newBuilder().setValue1("我是Protocol Buffer序列化的").setValue2(System.currentTimeMillis()).build();
//            request.setRequestBody(requestBody);
//            gameClientBoot.getChannel().writeAndFlush(request);
//        }
        if (messageId == 201) {//进入游戏请求
            DoEnterGameMsgRequest request = new DoEnterGameMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 202) {//获取自身简单信息
            GetPlayerSelfMsgRequest request = new GetPlayerSelfMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 203) {//获取仓库
            GetInventoryMsgRequest request = new GetInventoryMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 204) {//获取体力/疲劳
            GetStaminaMsgRequest request = new GetStaminaMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 206) {//获取邮件
            GetMailBoxMsgRequest request = new GetMailBoxMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 207) {//历史战斗列表
            GetRaidBattleListMsgRequest request = new GetRaidBattleListMsgRequest();
            request.getBodyObj().setFinish(true);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 2071) {//当前战斗列表
            GetRaidBattleListMsgRequest request = new GetRaidBattleListMsgRequest();
            request.getBodyObj().setFinish(false);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 208) {//战斗报酬列表
            GetRaidBattleRewardListMsgRequest request = new GetRaidBattleRewardListMsgRequest();
            request.getBodyObj().setClaimed(0);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 2081) {//战斗报酬历史列表
            GetRaidBattleRewardListMsgRequest request = new GetRaidBattleRewardListMsgRequest();
            request.getBodyObj().setClaimed(1);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 209){//查询所拥有的角色
            GetPlayerCharacterListMsgRequest request = new GetPlayerCharacterListMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }

        if (messageId == 302) {//获取特定id的角色数据
            GetPlayerByIdMsgRequest request = new GetPlayerByIdMsgRequest();
            request.getBodyObj().setPlayerId(50000001);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 303) {
            GetArenaPlayerListMsgRequest request = new GetArenaPlayerListMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 304) {//购买竞技场挑战次数
            DoBuyArenaChallengeTimesMsgRequest request = new DoBuyArenaChallengeTimesMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 306){//领取第一页邮件
            DoReceiveMailMsgRequest request = new DoReceiveMailMsgRequest();
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 3061){//领取全部邮件
            DoReceiveMailMsgRequest request = new DoReceiveMailMsgRequest();
            request.getBodyObj().setAllPages(true);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 308){//领取战斗报酬
            DoClaimRaidBattleRewardMsgRequest request = new DoClaimRaidBattleRewardMsgRequest();
            request.getBodyObj().setRaidId((String)values[0]);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 401) {//创建战斗
            DoCreateBattleMsgRequest request = new DoCreateBattleMsgRequest();
            request.getBodyObj().setArea(1);
            request.getBodyObj().setEpisode(1);
            request.getBodyObj().setChapter(1);
            request.getBodyObj().setStage(2);
            request.getBodyObj().setDifficulty(1);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if (messageId == 4011) {//创建随机战斗
            DoCreateBattleMsgRequest request = new DoCreateBattleMsgRequest();
            request.getBodyObj().setArea(1);
            request.getBodyObj().setEpisode(1);
            request.getBodyObj().setChapter(1);
            request.getBodyObj().setStage(RandomUtils.nextInt(1,4));
            request.getBodyObj().setDifficulty(1);
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 501){ //抽奖
            //默认10连 - 给11个结果
            DoDiamondGachaMsgRequest request = new DoDiamondGachaMsgRequest();
            request.getBodyObj().setGachaPoolsId("GachaPoolAlpha0001");
            gameClientBoot.getChannel().writeAndFlush(request);
        }

        if(messageId == 5011){ //抽奖 - 指定Pool ID
            //默认10连 - 给11个结果
            DoDiamondGachaMsgRequest request = new DoDiamondGachaMsgRequest();
            request.getBodyObj().setGachaPoolsId((String)values[0]);
            gameClientBoot.getChannel().writeAndFlush(request);
        }

        if(messageId == 5012){ //抽奖 - 指定Pool ID - 指定次数 1 | 10
            //默认10连 - 给11个结果
            DoDiamondGachaMsgRequest request = new DoDiamondGachaMsgRequest();
            request.getBodyObj().setGachaPoolsId((String)values[0]);
            request.getBodyObj().setType((int)values[1]);
            gameClientBoot.getChannel().writeAndFlush(request);
        }

        if(messageId == 1000){//进入战斗
            if(StringUtils.isEmpty(raidBattleVo.getRaidId())){
                logger.warn("请先获取RaidId");
                return;
            }
            JoinRaidBattleMsgRequest request = new JoinRaidBattleMsgRequest();
            request.getHeader().getAttribute().setRaidId(raidBattleVo.getRaidId());
            request.getBodyObj().setRaidId(raidBattleVo.getRaidId());
            gameClientBoot.getChannel().writeAndFlush(request);
        }

        if(messageId == 1001){//卡片攻击
            if(StringUtils.isEmpty(raidBattleVo.getRaidId())){
                logger.warn("请先获取RaidId");
                return;
            }
            RaidBattleCardAttackMsgRequest request = new RaidBattleCardAttackMsgRequest();
            request.getBodyObj().setCharaId("TEST_CHARA_0004");
            request.getHeader().getAttribute().setRaidId(raidBattleVo.getRaidId());
            gameClientBoot.getChannel().writeAndFlush(request);
        }
        if(messageId == 10001){
            if(StringUtils.isEmpty(raidBattleVo.getRaidId())){
                logger.warn("请先获取RaidId");
                return;
            }
            RaidBattleCardAttackMsgRequest request = new RaidBattleCardAttackMsgRequest();
            request.getHeader().getAttribute().setRaidId(raidBattleVo.getRaidId());
            request.getBodyObj().setCharaId("TEST_CHARA_0004");
            int i = 0;
            while (i < 10000){
                i++;
                gameClientBoot.getChannel().writeAndFlush(request);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
