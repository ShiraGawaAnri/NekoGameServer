package com.nekonade.common.utils;

import com.nekonade.common.error.exceptions.TokenException;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class JWTUtil {
    private final static String TOKEN_SECRET = "game_token#$%Abc";
    // TOKEN有效期 七天
    private final static long TOKEN_EXPIRE = DateUtils.MILLIS_PER_DAY * 7;

    @Deprecated
    public static String getUserToken(String openId, long userId, String username) {
        return getUserToken(openId, userId, 0, "-1", username);
    }

    @Deprecated
    public static String getUserToken(String openId, long userId, long playerId, String zoneId, String username, String... params) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;//使用对称加密算法生成签名
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        TokenBody tokenBody = new TokenBody();
        tokenBody.setOpenId(openId);
        tokenBody.setPlayerId(playerId);
        tokenBody.setUserId(userId);
        tokenBody.setServerId(zoneId);
        tokenBody.setUsername(username);
        tokenBody.setParam(params);
        String subject = JacksonUtils.toJSONString(tokenBody);
        JwtBuilder builder = Jwts.builder().setId(String.valueOf(nowMillis)).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm, TOKEN_SECRET);
        long expMillis = nowMillis + TOKEN_EXPIRE;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
        return builder.compact();
    }

    @Deprecated
    public static TokenBody getTokenBody(String token) throws TokenException {
        try {
            Claims claims = Jwts.parser().setSigningKey(TOKEN_SECRET).parseClaimsJws(token).getBody();
            String subject = claims.getSubject();
            TokenBody tokenBody = JacksonUtils.parseObject(subject, TokenBody.class);
            return tokenBody;
        } catch (Throwable e) {
            TokenException exp = new TokenException("token解析失败", e);
            if (e instanceof ExpiredJwtException) {
                exp.setExpire(true);
            }
            throw exp;
        }
    }


    public static String getUserTokenV2(String openId, long userId, String username) {
        return getUserTokenV2(openId, userId, 0, "-1", username);
    }

    @SneakyThrows
    public static String getUserTokenV2(String openId, long userId, long playerId, String zoneId, String username, String... params) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;//使用对称加密算法生成签名
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        TokenBody tokenBody = new TokenBody();
        tokenBody.setOpenId(openId);
        tokenBody.setPlayerId(playerId);
        tokenBody.setUserId(userId);
        tokenBody.setServerId(zoneId);
        tokenBody.setUsername(username);
        tokenBody.setParam(params);
        String subject = JacksonUtils.toJSONStringV2(tokenBody);
        JwtBuilder builder = Jwts.builder().setId(String.valueOf(nowMillis)).setIssuedAt(now).setSubject(subject).signWith(signatureAlgorithm, TOKEN_SECRET);
        long expMillis = nowMillis + TOKEN_EXPIRE;
        Date exp = new Date(expMillis);
        builder.setExpiration(exp);
        return builder.compact();
    }



    public static TokenBody getTokenBodyV2(String token) throws TokenException {
        try {
            Claims claims = Jwts.parser().setSigningKey(TOKEN_SECRET).parseClaimsJws(token).getBody();
            String subject = claims.getSubject();
            TokenBody tokenBody = JacksonUtils.parseObjectV2(subject, TokenBody.class);
            return tokenBody;
        } catch (Throwable e) {
            TokenException exp = new TokenException("token解析失败", e);
            if (e instanceof ExpiredJwtException) {
                exp.setExpire(true);
            }
            throw exp;
        }
    }

    @Getter
    @Setter
    public static class TokenBody {
        private String openId;
        private long userId;
        private long playerId;
        private String serverId = "1";
        private String username;
        private String[] param;//其它的额外参数


    }


}
