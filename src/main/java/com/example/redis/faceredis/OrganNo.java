package com.example.redis.faceredis;

import org.apache.commons.lang.RandomStringUtils;

/**
 * Description:
 * User: zhangll
 * Date: 2020-05-07
 * Time: 15:29
 */
public class OrganNo {

    /**
     * 组织认证数据
     * @return
     */
    /*public String organAuthenNo() {

        Long authenNo = null;
        if(redisOperations.exists(AUTHENNO)) {
            authenNo  =  redisOperations.incr(AUTHENNO);
        } else {
            try {
                if (redisLockHandler.getLock(LOCK_AUTHENNO)) {
                    String maxAuthenNo = supplierEmployeeAuthenticationService.findMaxAuthenNo();
                    if (Check.isNullOrEmpty(maxAuthenNo)) {
                        authenNo = 0L;
                        redisOperations.setex(AUTHENNO, 2592000, String.valueOf((authenNo + 1)));
                    } else {
                        authenNo = Long.parseLong(maxAuthenNo);
                        redisOperations.setex(AUTHENNO, 2592000, String.valueOf((authenNo + 1)));
                    }
                }
            } finally {
                redisLockHandler.releaseLock(LOCK_AUTHENNO);
            }
        }
        if(authenNo >= 100000000) {
            throw new BusinessException("自如认证编号超过8位，联系产品");
        }
        return String.valueOf(100000000 + authenNo).substring(1);
    }*/

    /**
     * 获取下一个储值卡卡号
     * <p/>
     * redis中存储的是当前卡号+1，数据库中取出最大卡号，处理方式略有不同
     *
     * @return
     */
    /*public Long getNextCardNumber() {
        String key = CacheConst.CURRENT_VALUECARD_NUMBER;
        // 从redis获取储值卡当前卡号
        Long cardNumber = null;
        if (redisLockHandler.getLock(key)) {
            String cardNumberStr = redisOperations.get(key);
            if (Check.NuNStrStrict(cardNumberStr)) {
                // redis中不存在，从数据库取最大值
                cardNumberStr = valueCardDao.getMaxCardNumber();
                if (Check.NuNStrStrict(cardNumberStr)) {
                    // 数据库不存在，取默认值，并将下一个卡号放入redis
                    redisOperations.setex(key, CacheConst.LONG_COMMON_TIME, String.valueOf(defaultCardNumber + 1));
                    redisLockHandler.releaseLock(key);
                    return defaultCardNumber;
                } else {
                    // 数据库中存在，返回下一个卡号，将+2的卡号放入redis
                    cardNumber = Long.valueOf(cardNumberStr);
                    redisOperations.setex(key, CacheConst.LONG_COMMON_TIME, String.valueOf(cardNumber + 2));
                    redisLockHandler.releaseLock(key);
                    return cardNumber + 1;
                }
            } else {
                // 取出卡号返回并重新设置
                cardNumber = Long.valueOf(cardNumberStr);
                redisOperations.setex(key, CacheConst.LONG_COMMON_TIME, String.valueOf(cardNumber + 1));
                redisLockHandler.releaseLock(key);
                return cardNumber;
            }
        }

        return cardNumber;
    }*/

    public String getPass(){
        return RandomStringUtils.random(6, "abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789".toCharArray());
    }
}
