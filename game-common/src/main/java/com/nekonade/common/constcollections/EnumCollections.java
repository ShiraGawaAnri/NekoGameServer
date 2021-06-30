package com.nekonade.common.constcollections;

import com.nekonade.common.error.IServerError;
import lombok.Getter;


public class EnumCollections {

    public static class DataBaseMapper {

        @Getter
        public enum EnumString{

            RaidBattle_Effect_TargetTo_Character("character"),
            RaidBattle_Effect_TargetTo_Enemy("enemy"),
            ;

            private final String value;

            EnumString(String value) {
                this.value = value;
            }
        }

        @Getter
        public enum CharacterProp{
            Player(0,"友军单位"),
            Enemy(1,"敌方单位"),
            //角色类型
            ;

            private final int value;
            private final String summary;

            CharacterProp(int value) {
                this(value,"");
            }

            CharacterProp(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }


        @Getter
        public enum CharacterType{
            Character(0,"普通(玩家)角色"),
            Monster(1,"怪物角色"),
            //角色类型
            ;

            private final int value;
            private final String summary;

            CharacterType(int value) {
                this(value,"");
            }

            CharacterType(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }


        @Getter
        public enum SkillProp{
            Active(0,"主动技能"),
            Passive(1,"被动技能"),
            ;

            private final int value;
            private final String summary;

            SkillProp(int value) {
                this(value,"");
            }

            SkillProp(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }

        @Getter
        public enum SkillType{
            Attack(0,"攻击技能"),
            Support(1,"辅助技能"),
            Defence(2,"防御技能"),
            Special(3,"特殊技能/复合技能"),
            ;

            private final int value;
            private final String summary;

            SkillType(int value) {
                this(value,"");
            }

            SkillType(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }

        @Getter
        public enum SkillStatusProp{
            Support(0,"一般辅助状态"),
            Dot(1,"每回合增/减血"),
            Special(2,"特殊状态"),
            ;
            private final int value;
            private final String summary;

            SkillStatusProp(int value) {
                this(value,"");
            }

            SkillStatusProp(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }

        @Getter
        public enum SkillStatusType{
            Buff(0,"增益"),
            Debuff(1,"减益"),
            Field(2,"场地"),
            ;
            private final int value;
            private final String summary;

            SkillStatusType(int value) {
                this(value,"");
            }

            SkillStatusType(int value,String summary) {
                this.value = value;
                this.summary = summary;
            }
        }


        @Getter
        public enum EnumNumber{
            No_Refresh(-1),
            Tomorrow_05_Refresh(0),
            Week_Monday(1),
            Week_Tuesday(2),
            Week_Wednesday(3),
            Week_Thursday(4),
            Week_Friday(5),
            Week_Saturday(6),
            Week_Sunday(7),
            RaidBattle_Create_LimitCounterRefreshType_None(0),
            RaidBattle_Effect_Prop_Buff(0),
            RaidBattle_Effect_Prop_Debuff(1),
            RaidBattle_Effect_Prop_Field(2),
            RaidBattle_Effect_Type_Dot(1),
            RaidBattle_Effect_Type_Support(2),
            RaidBattle_Effect_Type_Heal(3),
            RaidBattle_Effect_Type_Revive(4),
            RaidBattle_Effect_Type_Special(5),
            RaidBattle_EffectGroups_MaxValue(0),
            RaidBattle_EffectGroups_Overlapping(1),
            RaidBattle_CardSkill_Type_Attack(1),
            RaidBattle_CardSkill_Type_Support(2),
            RaidBattle_CardSkill_Type_Heal(3),
            RaidBattle_CardSkill_Type_Revive(4),
            RaidBattle_CardSkill_Type_Special(5),
            RaidBattle_In_State_None(0),
            RaidBattle_In_State_Water(1),
            RaidBattle_In_State_Underground(2),
            RaidBattle_In_State_Air(3),
            RaidBattle_In_State_Space(4),
            ActiveSkill_Type_Near_Physical(0),
            ActiveSkill_Type_Long_Physical(1),
            ActiveSkill_Type_Magic(2),
            ActiveSkill_Type_Misc(3),
            ActiveSkill_TargetType_Enemy(1),//敌对单位
            ActiveSkill_TargetType_Place(2),//地面系
            ActiveSkill_TargetType_Self(3),//自身
            ActiveSkill_TargetType_Team(4),//队伍所有成员啊
            ActiveSkill_TargetType_Trap(5),//触发系 定时系
            ActiveSkill_TargetType_AllFriend(6),//所有友军
            Item_Type_Healing(0),
            Item_Type_Usable(1),
            Item_Type_Etc(2),
            Item_Type_Healing_Dialog(100),//通常只需在客户端实现
            Item_Type_Usable_Dialog(101),//通常只需在客户端实现
            Item_Category_Common(0),
            Item_Delay_Type_Unable(-1),//没有使用间隔
            Item_Delay_Type_Specific_Duration(0),//指定时间
            ;

            private final int value;

            EnumNumber(int value) {
                this.value = value;
            }

        }
    }


    public static class CodeMapper{

        public enum GameErrorCode implements IServerError {
            StaminaNoEntity(100005, "疲劳值错误"),
            StaminaNotEnough(100006, "疲劳值不足"),
            CharacterExistedCanNotAdd(100010,"角色已拥有,无法新增"),
            CharacterNotExist(100030, "角色不存在"),
            WeaponNotExist(100031, "武器不存在"),
            CharacterLevelNotEnough(100032, "角色等级不足"),
            WeaponUnable(100033, "武器不可用"),
            CharacterHasEquippedWeapon(100034, "此角色已装备武器"),
            DiamondReachMax(100020,"钻石已到达上限,无法添加"),
            StageDbNotFound(100100, "不存在的关卡"),
            StageDbClosed(100101, "关卡未开放"),
            StageReachLimit(100105, "已达到上限次数"),
            StageCostItemNotEnough(100108, "需要消耗的道具数量不足"),
            SingleRaidBattleSameTimeOnlyOne(100200,"同时拥有的单人战斗不能超过1个"),
            MultiRaidBattleSameTimeReachLimit(100201,"同时拥有的战斗不能超过5个"),
            MultiRaidBattlePlayersReachMax(100301,"加入的战斗已满人"),
            MultiRaidBattlePlayersJoinedIn(100302,"已经加入到此战斗中了"),
            MultiRaidBattlePlayerNotJoinedIn(100304,"未加入到此战斗中"),
            RaidBattleHasGone(100305,"此战斗不存在"),
            RaidBattleHasExpired(100306,"此战斗已超时"),
            RaidBattleHasBeenFinished(100307,"此战斗已经结束"),
            RaidBattleJoinWithEmptyParty(100307,"必须组成有效的队伍才可加入战斗"),
            SingleRaidNotAcceptOtherPlayer(100311,"无法加入单人战斗"),
            RaidBattleAttackInvalidParam(100320,"无效的攻击"),
            RaidBattleAttackUndefinedSkill(100321,"未定义的卡片技能"),
            LogicError(100500, "请求在逻辑服务器内部处理有错误"),
            RaidBattleLogicError(100501, "请求在RaidBattle服务器内部处理有错误"),
            CoolDownDoReceiveMailBox(100510, "获取邮件道具操作过快"),
            CoolDownDoClaimRaidBattleReward(100511, "获取战斗报酬奖励操作过快"),
            GachaPoolsNotActive(200001,"抽奖池未开放"),
            GachaPoolsDiamondNotEnough(200003,"钻石不足"),
            GachaPoolsNotExist(200404,"抽奖池不存在"),
            GachaPoolsLogicError(200500,"抽奖出现错误"),
            ;
            private final int errorCode;
            private final String desc;

            GameErrorCode(int errorCode, String desc) {
                this.errorCode = errorCode;
                this.desc = desc;
            }

            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorDesc() {
                return desc;
            }

        }

        public enum GameCenterError implements IServerError {
            UNKNOW(-1, "用户中心服务未知异常"),
            SDK_VERIFY_ERROR(1100, "sdk验证错误"),
            OPENID_IS_EMPTY(1102, "openId为空"),
            OPENID_LEN_ERROR(1103, "openId长度不对"),
            SDK_TOKEN_ERROR(1104, "SDK token错误"),
            SDK_TOKEN_LEN_ERROR(1105, "sdk token 长度不对"),
            ZONE_ID_IS_EMPTY(1106, "zoneId为空"),
            NICKNAME_EXIST(1107, "昵称已存在"),
            NICKNAME_IS_EMPTY(1108, "昵称为空"),
            NICKNAME_LEN_ERROR(1109, "昵称长度不对"),
            TOKEN_FAILED(1110, "token错误"),
            NO_GAME_GATEWAY_INFO(1111, "没有网关信息，无法连接游戏"),
            USERNAME_IS_EMPTY(1112, "用户名为空"),
            PASSWORD_IS_EMPTY(1113, "密码为空"),
            ILLEGAL_LOGIN_TYPE(1114, "非法的登陆方式"),
            LOGIN_PASSWORD_ERROR(1115, "登陆密码或用户名不正确"),
            DUPLICATE_CREATEPLAYER_ERROR(1116, "已经创建过角色"),
            NOT_CREATEPLAYER_ERROR(1117, "请先创建角色"),
            USER_ACCOUNT_NOT_FOUND(1118, "无法找到有效的账号"),
            ;
            private final int errorCode;
            private final String errorDesc;


            GameCenterError(int errorCode, String errorDesc) {
                this.errorCode = errorCode;
                this.errorDesc = errorDesc;
            }

            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorDesc() {
                return errorDesc;
            }

            @Override
            public String toString() {
                StringBuilder msg = new StringBuilder();
                msg.append("errorCode:").append(this.errorCode).append("; errorMsg:").append(this.errorDesc);
                return msg.toString();
            }

        }

        public enum GameGatewayError implements IServerError {
            GAME_GATEWAY_ERROR(-2, "网关逻辑错误,请稍后再试"),
            SERVER_LOGIC_UNAVAILABLE(101, "逻辑服务器不可用,请稍后再试"),
            SERVER_RAIDBATTLE_UNAVAILABLE(102,"战斗服务器不可用,请稍后再试"),
            SERVER_IM_UNAVAILABLE(103, "聊天服务器不可用,请稍后再试"),
            TOKEN_ILLEGAL(1304, "TOKEN非法"),
            TOKEN_EXPIRE(1305, "TOKEN已过期"),
            REPEATED_CONNECT(1306, "重复连接，可能异地登陆了"),
            RateLimiterWaitLines(1307,"限流登陆中,请排队等候"),
            RequestRefuse(1308,"请求已拒绝"),
            RequestFunctionMaintenance(1309,"请求的功能正在维护中"),
            RequestTooFastUser(1310,"请求过快,请稍后再试[7]"),
            RequestTooFastGlobal(1311,"请求过快,请稍后再试[8]"),
            ;
            private final int errorCode;
            private final String errorDesc;


            GameGatewayError(int errorCode, String errorDesc) {
                this.errorCode = errorCode;
                this.errorDesc = errorDesc;
            }

            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorDesc() {
                return errorDesc;
            }

            @Override
            public String toString() {
                StringBuilder msg = new StringBuilder();
                msg.append("errorCode:").append(this.errorCode).append("; errorMsg:").append(this.errorDesc);
                return msg.toString();
            }
        }

        public enum GameRPCError implements IServerError {
            NOT_FIND_SERVICE_INSTANCE(1401, "没有找到服务实例"),
            TIME_OUT(1402, "RPC接收超时，没有消息返回"),
            ;
            private final int errorCode;
            private final String errorDesc;


            GameRPCError(int errorCode, String errorDesc) {
                this.errorCode = errorCode;
                this.errorDesc = errorDesc;
            }

            @Override
            public int getErrorCode() {
                return errorCode;
            }

            @Override
            public String getErrorDesc() {
                return errorDesc;
            }

            @Override
            public String toString() {
                StringBuilder msg = new StringBuilder();
                msg.append("errorCode:").append(this.errorCode).append("; errorMsg:").append(this.errorDesc);
                return msg.toString();
            }
        }

        public enum GatewayMessageCode implements IServerError {

            ConnectConfirm(1, "连接认证"),
            Heartbeat(2, "心跳消息"),
            PassConnectionStatusMsgRequest(3,"连接状态"),
            GameGatewayErrorMsgResponse(4, "游戏网关错误"),
            GameErrorMsgResponse(5, "游戏内部错误"),
            GameNotificationMsgResponse(6, "弹框提醒"),
            EnterGame(201,"进入游戏"),
            TriggerPlayerLevelUpMsgResponse(205,"进入游戏"),
            RaidBattleBoardCastMsgResponse(1010,"战斗广播消息"),
            ;
            private final int messageId;
            private final String desc;

            GatewayMessageCode(int messageId, String desc) {
                this.messageId = messageId;
                this.desc = desc;
            }

            public int getMessageId() {
                return messageId;
            }

            public String getDesc() {
                return desc;
            }


            @Override
            public int getErrorCode() {
                return messageId;
            }

            @Override
            public String getErrorDesc() {
                return desc;
            }
        }

        public enum WebGatewayError implements IServerError {
            UNKNOWN(500, "网关服务器预料外异常"),
            TOO_MANY_GLOBAL_REQUEST(509, "Web网关全局请求过多"),
            TOO_MANY_USER_REQUEST(503, "Web网关个体用户请求过多"),
            TOKEN_EMPTY(403, "必须携带TOKEN"),
            NOT_FOUND(404, "请求资源不存在");
            ;
            private final int errorCode;
            private final String errorDesc;


            WebGatewayError(int errorCode, String errorDesc) {
                this.errorCode = errorCode;
                this.errorDesc = errorDesc;
            }

            @Override
            public int getErrorCode() {
                return this.errorCode;
            }

            @Override
            public String getErrorDesc() {
                return this.errorDesc;
            }

            @Override
            public String toString() {
                return "errorCode:" + errorCode + "; errorMsg:" + this.errorDesc;
            }
        }
    }
}
