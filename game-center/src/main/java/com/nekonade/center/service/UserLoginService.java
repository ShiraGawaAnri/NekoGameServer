package com.nekonade.center.service;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.nekonade.common.constcollections.EnumCollections;
import com.nekonade.common.error.exceptions.GameErrorException;
import com.nekonade.common.error.IServerError;
import com.nekonade.common.redis.EnumRedisKey;
import com.nekonade.common.utils.CommonField;
import com.nekonade.dao.daos.UserAccountDao;
import com.nekonade.dao.db.entity.UserAccount;
import com.nekonade.network.param.http.request.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class UserLoginService {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Interner<String> pool = Interners.newWeakInterner();

    public IServerError verfiyLoginParam(LoginParam loginParam) {

        return null;
    }

    public IServerError verfiySdkToken(String openId, String token) {
        // 这里调用sdk服务端验证接口

        return null;
    }

    public UserAccount login(LoginParam loginParam) {

//        String openId = loginParam.getOpenId();
//        openId = openId.intern();//放openId放入到常量池
//        synchronized (openId) {// 对openId加锁，防止用户点击多次注册多次
//            Optional<UserAccount> op = userAccountDao.findById(openId);
//            UserAccount userAccount = null;
//            if (!op.isPresent()) {
//                // 用户不存在，自动注册
//                userAccount = this.register(loginParam);
//            } else {
//                userAccount = op.get();
//            }
//            return userAccount;
//        }
        String username = loginParam.getUsername();
        synchronized (pool.intern(username)) {// 对openId加锁，防止用户点击多次注册多次
            Optional<UserAccount> op = this.getUserAccountByUsername(username);
            UserAccount userAccount = null;
            if (!op.isPresent()) {
                // 用户不存在，自动注册
                userAccount = this.register(loginParam);
            } else {
                userAccount = op.get();
                if (userAccount.getPassword().equals(loginParam.getPassword())) {
                    return userAccount;
                }
                throw GameErrorException.newBuilder(EnumCollections.CodeMapper.GameCenterError.LOGIN_PASSWORD_ERROR).build();
            }
            return userAccount;
        }
    }

    private UserAccount register(LoginParam loginParam) {

        long userId = userAccountDao.getNextUserId();// 使用redis自增保证userId全局唯一
        UserAccount userAccount = new UserAccount();
        userAccount.setOpenId(DigestUtils.md5Hex(loginParam.getOpenId()));
        userAccount.setCreateTime(System.currentTimeMillis());
        userAccount.setUserId(userId);
        userAccount.setUsername(loginParam.getUsername());
        userAccount.setPassword(loginParam.getPassword());
        this.updateUserAccount(userAccount);
        logger.debug("user {} 注册成功", userAccount);
        return userAccount;

    }

    public void updateUserAccount(UserAccount userAccount) {
        this.userAccountDao.saveOrUpdate(userAccount, userAccount.getUserId());
    }

    public Optional<UserAccount> getUserAccountByUserId(long userId) {
        return this.userAccountDao.findById(userId);
    }

    //除非以openId为主键
//    public Optional<UserAccount> getUserAccountByOpenId(String openId) {
//        return this.userAccountDao.findById(openId);
//    }

    public Optional<UserAccount> getUserAccountByUsername(String username) {
        long userId = this.getUserIdByUserName(username);
        return this.getUserAccountByUserId(userId);
    }

    private long getUserIdByUserName(String username) {
        String key = EnumRedisKey.USER_NAME_REGISTER.getKey(username);
        String userId = redisTemplate.opsForValue().get(key);
        if (userId == null) {
            Optional<UserAccount> op = userAccountDao.findByUsername(username);
            if (op.isPresent()) {
                UserAccount userAccount = op.get();
                userId = String.valueOf(userAccount.getUserId());
                redisTemplate.opsForValue().set(key, userId, EnumRedisKey.USER_NAME_REGISTER.getTimeout());
            }
        }
        return userId == null ? -255 : Long.valueOf(userId);
    }


    public long getUserIdFromHeader(HttpServletRequest request) {
        String value = request.getHeader(CommonField.USER_ID);
        long userId = 0;
        if (!StringUtils.isEmpty(value)) {
            userId = Long.parseLong(value);
        }
        return userId;

    }

    public String getOpenIdFromHeader(HttpServletRequest request) {
        return request.getHeader(CommonField.OPEN_ID);
    }

    public String getUsernameFromHeader(HttpServletRequest request) {
        return request.getHeader(CommonField.USERNAME);
    }
}
