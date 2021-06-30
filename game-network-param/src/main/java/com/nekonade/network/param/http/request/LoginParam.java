package com.nekonade.network.param.http.request;

import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.utils.CommonField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class LoginParam extends AbstractHttpRequestParam {

    private String username;
    private String password;
    private String openId;
    private String token;

    private String ip;

    private int loginType = 1;

    @Override
    protected void haveError() {
        // 验证登陆参数
        if (this.loginType == 1) {
            if (StringUtils.isEmpty(username)) {
                this.error = EnumCollections.CodeMapper.GameCenterError.USERNAME_IS_EMPTY;
            } else if (StringUtils.isEmpty(password)) {
                this.error = EnumCollections.CodeMapper.GameCenterError.PASSWORD_IS_EMPTY;
            }
        } else if (this.loginType == 2) {
            if (StringUtils.isEmpty(openId)) {
                this.error = EnumCollections.CodeMapper.GameCenterError.OPENID_IS_EMPTY;
            } else if (openId.length() > CommonField.OPEN_ID_LENGTH) {
                this.error = EnumCollections.CodeMapper.GameCenterError.OPENID_LEN_ERROR;
            } else if (StringUtils.isEmpty(token)) {
                this.error = EnumCollections.CodeMapper.GameCenterError.SDK_TOKEN_ERROR;
            } else if (token.length() > 128) {
                this.error = EnumCollections.CodeMapper.GameCenterError.SDK_TOKEN_LEN_ERROR;
            }
        } else {
            this.error = EnumCollections.CodeMapper.GameCenterError.ILLEGAL_LOGIN_TYPE;
        }
    }


}
