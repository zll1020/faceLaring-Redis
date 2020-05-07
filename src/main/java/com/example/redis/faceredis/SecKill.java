package com.example.redis.faceredis;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Description:
 * User: zhangll
 * Date: 2020-05-07
 * Time: 15:37
 */
public class SecKill {
    /**
     * 秒杀优惠券
     *
     * @param request
     * @return
     */
    /*@RequestMapping(value = "/seckillHandle", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ResponseSecurityDto> seckillPromotion(HttpServletRequest request) {
        String paramValue = (String) request.getAttribute(ConstDef.DECRYPT_PARAM_ATTRIBUTE);
        LOGGER.info("paramValue,{}", paramValue);
        ValidatorResult<SeckillPromotActivityReq> validatorResult = validator.checkParamValidate(paramValue, SeckillPromotActivityReq.class);
        if (!validatorResult.isSuccess()) {
            LogUtil.error(LOGGER, "activity seckillHandle request lose param!");
            return new ResponseEntity<>(ResponseSecurityDto.responseEncryptFail("参数格式有误"), HttpStatus.OK);
        }
        SeckillPromotActivityReq seckillPromotActivityReq = validatorResult.getExpectObj();
        LOGGER.info("seckillPromotion,{}", JSON.toJSONString(seckillPromotActivityReq));
        if (Check.NuNStr(seckillPromotActivityReq.getUid())) {
            ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptFail("用户信息为空");
            responseDto.getResponseDto().setCode(ActiveHelpCodeConst.USER_NULL_CODE);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        if (!Check.NuNStrStrict(seckillPromotActivityReq.getUid()) && Check.NuNStrStrict(seckillPromotActivityReq.getProductSeckillId())) {
            ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptFail("秒杀规则Id不能为空");
            responseDto.getResponseDto().setCode(ActiveHelpCodeConst.FAIL_CDOE);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        //1.检查活动有效性
        String activityResult = activityService.getActivieByCode(seckillPromotActivityReq.getActivityCode(), seckillPromotActivityReq.getActivityTheme(), 1);
        if (!DataTransferObjectJsonParser.checkSOAReturnSuccess(activityResult)) {
            ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptFail(DataTransferObjectJsonParser.getReturnMsg(activityResult));
            responseDto.getResponseDto().setCode(ActiveHelpCodeConst.ACTIVE_NOT_START_CODE);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        String ruleKey = seckillPromotActivityReq.getActivityCode() + seckillPromotActivityReq.getActivityTheme() + seckillPromotActivityReq.getProductSeckillId();
        String seckillRuleCacheStr = redisSentinelCacheClient.get(ruleKey);
        ActivitySeckillRuleResp seckillRuleResp = null;
        if (Check.NuNStr(seckillRuleCacheStr)) {
            seckillRuleResp = seckillPromoActivityService.findActivitySeckillRuleByLogicCode(seckillPromotActivityReq.getProductSeckillId());
            redisSentinelCacheClient.setnx(ruleKey, JSON.toJSONString(seckillRuleResp), REDIS_CACHE_TIME);
        } else {
            seckillRuleResp = JSON.parseObject(seckillRuleCacheStr, ActivitySeckillRuleResp.class);
        }
        //redis锁key
        String lockKey = seckillPromoActivityProcess.getSeckillLockKey(seckillPromotActivityReq.getActivityCode(), seckillPromotActivityReq.getActivityTheme(), seckillPromotActivityReq.getUid());
        //库存初始化key
        String initNumKey = seckillPromoActivityProcess.getInitStockNumKey(seckillRuleResp.getActivityCode(), seckillRuleResp.getActivityTheme(), seckillRuleResp.getLogicCode());
        //已抢到的产品key
        String seckillProductKey = seckillPromoActivityProcess.getSeckillProductKey(seckillRuleResp.getActivityCode(), seckillRuleResp.getActivityTheme(), seckillPromotActivityReq.getUid(), seckillRuleResp.getLogicCode(), false);
        //已抢到的产品key 分区日期
        String seckillProductKeyHasDate = seckillPromoActivityProcess.getSeckillProductKey(seckillRuleResp.getActivityCode(), seckillRuleResp.getActivityTheme(), seckillPromotActivityReq.getUid(), seckillRuleResp.getLogicCode(), true);
//        boolean isPop = false;
//        Jedis jedis = null;
        try {
            if (redisSentinelLockHandler.getLock(lockKey, 3000)) {
                //1.检查活动的时效性
                String relt = seckillPromoActivityProcess.getActiveStatus(seckillRuleResp.getStartTime(), seckillRuleResp.getEndTime());
                if (GeneralActivityConstant.countdownStatusEnum.IS_COUNTDOWN.getCode().equals(relt)) {
                    ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptFail("再等等，" + seckillRuleResp.getStartTime() + "准时开抢");
                    responseDto.getResponseDto().setCode(ActiveHelpCodeConst.ACTIVE_NOT_START_CODE);
                    responseDto.getResponseDto().setMessage("该场次的活动未开抢");
                    return new ResponseEntity<>(responseDto, HttpStatus.OK);
                }
                //2.检查用户是否参加此活动，如果参见过则返回优惠信息，从缓存中取数
                String userRecieveInfo = null;
                if (ActivitySeckillEnum.LimitType.ONLY_ONCE.getCode().equals(seckillRuleResp.getLimitType())) {
                    //仅限一次
                    userRecieveInfo = redisSentinelCacheClient.get(seckillProductKey);
                } else if (ActivitySeckillEnum.LimitType.ONCE_A_DAY.getCode().equals(seckillRuleResp.getLimitType())) {
                    //每天一次
                    userRecieveInfo = redisSentinelCacheClient.get(seckillProductKeyHasDate);
                }
                SeckillPromotActivityResp seckillPromotActivityResp = new SeckillPromotActivityResp();
                if (!Check.NuNStr(userRecieveInfo)) {
                    seckillPromotActivityResp.setReciveStatus(Integer.valueOf(GeneralActivityConstant.couponStatusEnum.ACTIVITY_RECEIVE_SUCCESS.getCode()));
                    ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptOK(seckillPromotActivityResp);
                    responseDto.getResponseDto().setCode(ActiveSeckillCodeConst.SEC_KILL_HASD_CODE);
                    responseDto.getResponseDto().setMessage("已经秒杀过了");
                    return new ResponseEntity<>(responseDto, HttpStatus.OK);
                }
                //3.秒杀
//                jedis = redisSentinelCacheClient.obtainJedis();
//                key = jedis.rpop(initNumKey);
//                LOGGER.info("redis pop,{}", key);
//                if (redisSentinelLockHandler.getLock(key, 2000)) {
                if (redisSentinelLockHandler.getMutexLock(initNumKey, 100)) {
                    if (redisSentinelLockHandler.getRedisOperations().exists(initNumKey)) {
                        redisSentinelLockHandler.getRedisOperations().incr(initNumKey);//利用redis单进程，原子操作，多线程情况保证累计值的正确性
                    } else {
                        redisSentinelLockHandler.getRedisOperations().setnx(initNumKey, "1");
                    }
                    Integer curNum = Integer.valueOf(redisSentinelLockHandler.getRedisOperations().get(initNumKey) == null ? "0" : redisSentinelLockHandler.getRedisOperations().get(initNumKey)).intValue();
                    LOGGER.info("剩余数量,{}", seckillRuleResp.getInitStockNum() - curNum);
                    if (curNum <= seckillRuleResp.getInitStockNum()) {
                        SeckillTypeService seckillTypeService = seckillTypeServiceFactory.getSeckillTypeService(seckillRuleResp.getSeckillProductType());
                        ResponseEntity<ResponseSecurityDto> responseEntity = seckillTypeService.seckillHandle(seckillPromotActivityReq, seckillRuleResp);
                        if (!Check.NuNObj(responseEntity) && responseEntity.getBody().getResponseDto().getStatus().equals("0")) {
                            if (ActivitySeckillEnum.LimitType.ONLY_ONCE.getCode().equals(seckillRuleResp.getLimitType())) {
                                //仅限一次
                                redisSentinelCacheClient.setnx(seckillProductKey, "1");
                            } else if (ActivitySeckillEnum.LimitType.ONCE_A_DAY.getCode().equals(seckillRuleResp.getLimitType())) {
                                //每天一次
                                redisSentinelCacheClient.setnx(seckillProductKeyHasDate, "1");
                            }
                        }
                        return responseEntity;
                    } else {
                        seckillPromotActivityResp.setReciveStatus(Integer.valueOf(GeneralActivityConstant.couponStatusEnum.ACTIVITY_IS_END.getCode()));
                        ResponseSecurityDto responseDto = ResponseSecurityDto.responseEncryptOK(seckillPromotActivityResp);
                        responseDto.getResponseDto().setCode(ActiveSeckillCodeConst.SEC_KILL_OUT_CODE);
                        responseDto.getResponseDto().setMessage("已经抢光了");
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("paramValue,{}", e.getMessage());
        } finally {
            redisSentinelLockHandler.releaseLock(lockKey);
        }
        return new ResponseEntity<>(ResponseSecurityDto.responseEncryptFail("活动太火爆，稍后再试！"), HttpStatus.OK);
    }*/
}
