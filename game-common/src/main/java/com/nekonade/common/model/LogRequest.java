package com.nekonade.common.model;

import com.nekonade.common.gameMessage.IGameMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogRequest {

    private String operatorId;
    /**     类名  **/
    private String operateClassName;
    /**     方法名 **/
    private String operateMethodName;
    /**     操作类型    **/
    private String operateType;
    /**     操作说明    **/
    private String operateExplain;

    private String operateDate;

    private String operateResult;

    private String remark;

    private Boolean operateSuccessful = false;

    private IGameMessage gameMessage;
}
