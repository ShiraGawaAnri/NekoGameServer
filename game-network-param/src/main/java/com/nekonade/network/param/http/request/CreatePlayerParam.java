package com.nekonade.network.param.http.request;

import com.nekonade.common.constcollections.EnumCollections;
import org.springframework.util.StringUtils;

public class CreatePlayerParam extends AbstractHttpRequestParam {

    private String zoneId;// 如果是分区游戏，需要传区id
    private String nickName;

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    @Override
    protected void haveError() {
        if (StringUtils.isEmpty(zoneId)) {
            this.error = EnumCollections.CodeMapper.GameCenterError.ZONE_ID_IS_EMPTY;
        } else if (StringUtils.isEmpty(nickName)) {
            this.error = EnumCollections.CodeMapper.GameCenterError.NICKNAME_IS_EMPTY;
        } else {
            int len = nickName.length();
            if (len < 2 || len > 10) {
                this.error = EnumCollections.CodeMapper.GameCenterError.NICKNAME_LEN_ERROR;
            }
        }
    }


}
