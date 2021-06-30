package com.nekonade.network.param.log;

import com.nekonade.common.gameMessage.IGameMessage;
import com.nekonade.common.utils.JacksonUtils;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogTable {

    private Integer id;

    private String operatorId;
    /**     类名  **/
    private String operateClassName;
    /**     方法名 **/
    private String operateMethodName;
    /**     操作类型    **/
    private String operateType;
    /**     操作说明    **/
    private String operateExplain;

    @Deprecated
    private String operateDate;

    private Long operateTimestamp;

    private Long operateFinishTimestamp;

    private String operateResult;

    private String remark;

    private Boolean operateSuccessful = false;

    private byte[] gameMessage;

    public static LogTable readBody(byte[] body){
        if(body == null) return null;
        String str = new String(body);
        return JacksonUtils.parseObjectV2(str, LogTable.class);
    }

}