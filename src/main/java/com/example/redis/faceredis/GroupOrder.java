package com.example.redis.faceredis;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * User: zhangll
 * Date: 2020-05-07
 * Time: 15:40
 */
public class GroupOrder {
    /*
    // 创建活动订单
    public String activityOrderCreate(String Json) {
        ActivityCleanOrderCreateReq activityCleanOrderCreateReq = paramCheckLogic.checkParamValidate(Json, ActivityCleanOrderCreateReq.class);
        //检查活动有效性
        if (Check.NuNStr(activityCleanOrderCreateReq.getActiveInitCode())) {
            activityGroupBuyProcess.checkValidPromotionActive(activityCleanOrderCreateReq.getActiveTypeCode(), activityCleanOrderCreateReq.getActivityTheme(), 1);
        }
        //创建订单锁
        String lockKey = ACTIVITY_ORDER_CREATE + "_" + activityCleanOrderCreateReq.getActivityTheme() + "_" + activityCleanOrderCreateReq.getUid();
        if (!redisSentinelLockHandler.getMutexLock(lockKey)) {
            throw new BusinessException("正在创建订单，请稍后再试！");
        }
        LOGGER.info("活动订单下单开始,下单入参:{}", JSON.toJSONString(activityCleanOrderCreateReq));
        //团购表主键
        ActivityOrderInfoEntity activityOrderInfoEntity = null;
        DataTransferObject dto = new DataTransferObject();
        //下单取消支付后重新支付
        List<ActivityOrderInfoEntity> activityOrderInfoEntities = activityOrderInfoService.findWaitPayByUid(activityCleanOrderCreateReq.getUid(), activityCleanOrderCreateReq.getActiveTypeCode(), activityCleanOrderCreateReq.getActivityTheme());
        if (activityOrderInfoEntities.size() > 1) {
            throw new BusinessException("活动订单数量异常");
        } else if (activityOrderInfoEntities.size() == 1) {
            dto.putValue("data", activityOrderInfoEntities.get(0));
            return dto.toJsonString();
//                activityOrderExpireProxy.activityOrderExpire(activityOrderInfoEntities);
        }
        try {
            Integer limitNum = activityOrderInfoProcess.getLimitOrderNum(activityCleanOrderCreateReq.getActivityTheme());
            if (limitNum > 0) {
                LOGGER.info(activityCleanOrderCreateReq.getActivityTheme() + "每日限量：{}", limitNum);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = simpleDateFormat.format(new Date(System.currentTimeMillis()));
                //创建检验锁。。。此处要进行一次查询一次插入所以为两个操作加锁
                if (redisSentinelLockHandler.getMutexLock(activityCleanOrderCreateReq.getActivityTheme(), 50)) {
                    //创建每日团购次数上限
                    if (!redisSentinelLockHandler.getRedisOperations().exists(activityCleanOrderCreateReq.getActivityTheme() + "_" + date)) {
                        redisSentinelLockHandler.getRedisOperations().setex(activityCleanOrderCreateReq.getActivityTheme() + "_" + date, 24 * 60 * 60, limitNum.toString());
                    }
                    //判断当前是否还有购买名额
                    if (Integer.parseInt(redisSentinelLockHandler.getRedisOperations().get(activityCleanOrderCreateReq.getActivityTheme() + "_" + date)) > 0) {
                        //名额-1
                        Integer times = Integer.parseInt(redisSentinelLockHandler.getRedisOperations().get(activityCleanOrderCreateReq.getActivityTheme() + "_" + date)) - 1;
                        redisSentinelLockHandler.getRedisOperations().setex(activityCleanOrderCreateReq.getActivityTheme() + "_" + date, 24 * 60 * 60, times.toString());
                    } else {
                        throw new BusinessException("对不起，今日已抢完，明天再来试试吧");
                    }
                } else {
                    throw new BusinessException("正在努力抢单请稍后");
                }
                redisSentinelLockHandler.releaseLock(activityCleanOrderCreateReq.getActivityTheme());
            }
            //校验是否参加过活动
            String flagKey = activityCleanOrderCreateReq.getActiveTypeCode() + "_" + activityCleanOrderCreateReq.getActivityTheme() + "_" + activityCleanOrderCreateReq.getUid();
            boolean isExistsKey = redisSentinelLockHandler.getRedisOperations().exists(flagKey);
            if (isExistsKey) {
                throw new BusinessException("您已经购买过该服务");
            }
            activityOrderInfoEntity = activityCreateOrderService.createCommonOrder(activityCleanOrderCreateReq);
            if (!Check.NuNObj(activityOrderInfoEntity)) {
                LOGGER.info("活动订单下单结束,下单成功{}", JSON.toJSONString(activityOrderInfoEntity));
            } else {
                LOGGER.info("活动订单下单异常,入参:{}", JSON.toJSONString(activityCleanOrderCreateReq));
            }
            //下单成功redis存入参团记录
            String themes = groupBuyActivityConfig.getActivityOrderPerNolimitTheme();
            if (themes.indexOf(activityCleanOrderCreateReq.getActivityTheme()) == -1) {
                redisSentinelLockHandler.getRedisOperations().setnx(flagKey, "true");
            }
        } finally {
            //释放分布式锁
            redisSentinelLockHandler.releaseLock(lockKey);
        }
        dto.putValue("data", activityOrderInfoEntity);
        return dto.toJsonString();
    }*/


    /**
     * 合团
     */
    /*public String togetherGroup(String param) {
        GroupBuyTogetherReq groupBuyTogetherReq = paramCheckLogic.checkParamValidate(param, GroupBuyTogetherReq.class);
        DataTransferObject dto = new DataTransferObject();
        PromotionActiveEntity activeEntity = promotionActiveService.findByActivityCode(groupBuyTogetherReq.getActivityCode(), groupBuyTogetherReq.getActivityTheme());
        groupBuyTogetherReq.setGroupBuyStatus(ActivityShareConstEnum.GroupBuyStatus.GROUPBUYING.getCode());
        PromotionActivityShareLogEntity shareLogEntity1 = promotionActivityShareLogService.findShareLogByUserCodeAndActivity(groupBuyTogetherReq).get(0);
        PromotionActivityShareLogEntity shareLogEntity2 = promotionActivityShareLogService.findByCode(groupBuyTogetherReq.getGroupShareLogicCode());
        if (Check.NuNObj(shareLogEntity1) || Check.NuNObj(shareLogEntity1)) {
            return MessageUtil.returnError(messageSource, "group.buy.share.null").toJsonString();
        }
        if (Objects.equals(shareLogEntity1.getLogicCode(), shareLogEntity2.getLogicCode())) {
            return MessageUtil.returnError(messageSource, "group.buy.together.myself").toJsonString();
        }
        String firstLock = shareLogEntity1.getLogicCode().compareTo(shareLogEntity2.getLogicCode()) > 0 ? shareLogEntity2.getLogicCode() : shareLogEntity1.getLogicCode();
        String secondLock = shareLogEntity1.getLogicCode().compareTo(shareLogEntity2.getLogicCode()) > 0 ? shareLogEntity1.getLogicCode() : shareLogEntity2.getLogicCode();
        try {
            // 对分享记录加锁
            if (redisSentinelLockHandler.getLock(KEY + firstLock, 20000) && redisSentinelLockHandler.getLock(KEY + secondLock, 20000)) {
                // share 记录
                shareLogEntity1 = promotionActivityShareLogService.findByCode(shareLogEntity1.getLogicCode());
                shareLogEntity2 = promotionActivityShareLogService.findByCode(shareLogEntity2.getLogicCode());
                //查询share记录，判断拼团状态
                if (!Objects.equals(ActivityShareConstEnum.GroupBuyStatus.GROUPBUYING.getCode(), shareLogEntity1.getGroupBuyStatus()) || !Objects.equals(ActivityShareConstEnum.GroupBuyStatus.GROUPBUYING.getCode(), shareLogEntity2.getGroupBuyStatus())) {
                    return MessageUtil.returnError(messageSource, "group.buy.share.status.error").toJsonString();
                }
                // help记录
                List<PromotionActivityHelpLogEntity> helpLogEntities = promotionActivityHelpLogService.findByShareLogicCode(Arrays.asList(shareLogEntity1.getLogicCode(), shareLogEntity2.getLogicCode()), groupBuyTogetherReq.getActivityCode(), groupBuyTogetherReq.getActivityTheme());

                if (Check.NuNCollection(helpLogEntities)) {
                    return MessageUtil.returnError(messageSource, "group.buy.share.null").toJsonString();
                }

                // 查询help记录，是否存在help记录，判断支付状态
                // 存在未支付的参团记录且未支付的发起拼团记录不能访问到本接口，不进行支付状态过滤
                helpLogEntities = helpLogEntities.stream().filter(e -> (Objects.equals(ActivityShareConstEnum.GroupBuyPayStatus.GROUPBUY_YES_PAY.getCode(), e.getPayStatus()) || Objects.equals(ActivityShareConstEnum.GroupBuyPayStatus.GROUPBUY_NO_PAY.getCode(), e.getPayStatus()))).collect(Collectors.toList());
                if (Check.NuNCollection(helpLogEntities)) {
                    return MessageUtil.returnError(messageSource, "group.buy.pay.status.error").toJsonString();
                }

                if (helpLogEntities.size() > activeEntity.getLimitNum()) {
                    return MessageUtil.returnError(messageSource, "group.buy.together.person.more.limit").toJsonString();
                }

                // 判断有没有虚拟身份，有虚拟身份，则唯一团长，无虚拟身份，发起时间早的为团长
                PromotionActivityShareLogEntity groupLeader = null;
                PromotionActivityShareLogEntity groupMember = null;
                if (Objects.equals(ActivityShareConstEnum.IdentityType.VIRTUAL_IDENTITY.getCode(), shareLogEntity1.getIdentityType())) {
                    groupLeader = shareLogEntity2;
                    groupMember = shareLogEntity1;
                } else if (Objects.equals(ActivityShareConstEnum.IdentityType.VIRTUAL_IDENTITY.getCode(), shareLogEntity2.getIdentityType())) {
                    groupLeader = shareLogEntity1;
                    groupMember = shareLogEntity2;
                } else {
                    groupLeader = shareLogEntity1.getCreateTime().getTime() <= shareLogEntity2.getCreateTime().getTime() ? shareLogEntity1 : shareLogEntity2;
                    groupMember = shareLogEntity1.getCreateTime().getTime() <= shareLogEntity2.getCreateTime().getTime() ? shareLogEntity2 : shareLogEntity1;
                }
                // 组装信息
                activityGroupBuyProcess.organUpdateInfo(groupLeader, groupMember, helpLogEntities, groupBuyTogetherReq.getUserCode(), activeEntity);
                int count = promotionActivityHelpLogService.togetherGroup(Arrays.asList(groupLeader, groupMember), helpLogEntities);
                if (count < helpLogEntities.size() + 2) {
                    return MessageUtil.returnError(messageSource, "group.buy.together.fail").toJsonString();
                }
                // 发送短息和推送
                Set<String> userCodes = new HashSet<>();
                Set<String> userPhones = new HashSet<>();
                for (PromotionActivityHelpLogEntity helpLogEntity : helpLogEntities) {
                    userCodes.add(helpLogEntity.getUserCode());
                    userPhones.add(helpLogEntity.getUserPhone());
                    //生活节增加自由力
                    if ("festivalOfLive".equals(helpLogEntity.getActivityTheme())) {
                        promotionActivityHelpLogProcess.addZiYouLi(helpLogEntity.getUserCode());
                    }
                    LOGGER.info("shareLogicCode对应助力记录：{}发送短信和推送", helpLogEntity.getLogicCode());
                }
                String urlParamter = null;
                if ("grapGroupBuyActive".equals(groupBuyTogetherReq.getActivityCode())) {
                    urlParamter = "?leix=grapGroup&isMessage=1";
                }
                if ("commonGroupBuyActive".equals(groupBuyTogetherReq.getActivityCode())) {
                    urlParamter = "?leix=commonGroup&isMessage=1";
                }
                activityGroupBuyPushProcess.asyncPushMessage(activeEntity.getActivityTheme(), ActivityGroupBuyPushEnum.PushBusinessType.SUCCESS_GROUPBUY_PUSH.getCode(), userCodes, urlParamter, null);
                activityGroupBuyPushProcess.asyncSendMessage(activeEntity.getActivityTheme(), ActivityGroupBuyPushEnum.PushBusinessType.SUCCESS_GROUPBUY_PUSH.getCode(), userPhones, urlParamter);
                return dto.toJsonString();
            }
        } catch (Exception e) {
            LOGGER.error("合团异常，异常信息{}", e);
            return MessageUtil.returnError(messageSource, "group.buy.together.fail").toJsonString();
        } finally {
            redisSentinelLockHandler.releaseLock(KEY + firstLock);
            redisSentinelLockHandler.releaseLock(KEY + secondLock);
        }
        return MessageUtil.returnError(messageSource, "group.buy.together.fail").toJsonString();
    }*/

    /**
     * 团购单取消
     * 调用时需要判断：
     * 服务单取消调用时需要判断团购单处于拼团成功状态才能调用
     * 活动单取消调用时需要判断已支付且拼团中
     *
     * @return
     */
    /*public boolean cancelGroupByOrder(String activityOrderCode, Integer oldState, Integer newState, String operaUserCode, Integer cancleState) {
        boolean result = false;
        SearchMap map = new SearchMap();
        map.put("orderCode", activityOrderCode);
        map.put("oldState", oldState);
        map.put("newState", newState);
        map.put("applyState", cancleState);
        map.put("applyTime", System.currentTimeMillis());
        Integer rs = activityOrderInfoDao.changeActivityOrderState(map);
        if (rs.intValue() == 1) {
            FindPromotionCodeIsUseReq findPromotionCodeIsUseReq = new FindPromotionCodeIsUseReq();
            findPromotionCodeIsUseReq.setBuyOrderNum(activityOrderCode);
            String promotionStr = promotionCodeInnerService.findPromotionCodeByBuyCode(JSON.toJSONString(findPromotionCodeIsUseReq));
            if (!DataTransferObjectJsonParser.checkSOAReturnSuccess(promotionStr)) {
                LOGGER.error("优惠券查询服务异常");
                throw new BusinessException("优惠券查询服务异常");
            }
            List<PromotionCodeIsUseResp> promoList = DataTransferObjectJsonParser.getListValueFromDataByKey(promotionStr, "data", PromotionCodeIsUseResp.class);
            long needDecordAmount = 0l;
            List<ReceiptRefundPromotionConsumeReq> refundPromotionOrder = new ArrayList<>();
            if (!Check.NuNCollection(promoList)) {
                LOGGER.info("活动订单退款，检测到存在赠送的优惠券，单号：{}", activityOrderCode);
                ArrayList<String> promoCodes = new ArrayList<>();
                for (PromotionCodeIsUseResp item : promoList) {
                    if (item.getIsUse()) {
                        //已使用
                        needDecordAmount += item.getPromoPrice() * 100;
                        ReceiptRefundPromotionConsumeReq itemOrder = new ReceiptRefundPromotionConsumeReq();
                        itemOrder.setOrderCode(item.getOrderNumber());
                        itemOrder.setOrderCategoryCode(item.getSecondCategoryCode());
                        itemOrder.setPromotionCode(item.getLogicCode());
                        itemOrder.setPromotionPrice((int) (item.getPromoPrice() * 100));
                        refundPromotionOrder.add(itemOrder);
                    } else {
                        //未使用
                        if (item.getIsExpired()) {
                            //已过期
                        } else {
                            //未过期，需要作废
                            if (item.getIsDelete().intValue() == 0) {
                                promoCodes.add(item.getLogicCode());
                            }
                        }
                    }
                }
                if (!Check.NuNCollection(promoCodes)) {
                    PromotionCancelReq cancelReq = new PromotionCancelReq();
                    cancelReq.setLogicCodeList(promoCodes);
                    cancelReq.setModifyUserCode(operaUserCode);
                    String cancelPromotionStr = promotionCodeInnerService.cancelPromotionCode(JSON.toJSONString(cancelReq));
                    if (!DataTransferObjectJsonParser.checkSOAReturnSuccess(cancelPromotionStr)) {
                        LOGGER.error("作废优惠券失败，活动单号:{}", activityOrderCode);
                        throw new BusinessException("作废优惠券失败，活动单号：" + activityOrderCode);
                    } else {
                        LOGGER.info("作废优惠券成功，开始执行退款");
                    }
                }
            }
            ActivityOrderPaymentResultEntity paymentResultEntity = activityOrderPaymentResultDao.findPaymentByOrderCode(activityOrderCode);
            long refundAmount = paymentResultEntity.getPayAmount();
            refundAmount = refundAmount - needDecordAmount < 0 ? 0 : refundAmount - needDecordAmount;
            redisSentinelLockHandler.getRedisOperations().setnx("refund" + activityOrderCode, String.valueOf(refundAmount));
            //当需要退费金额为0时不再调用退款
            if (refundAmount == 0) {
                LOGGER.info("订单取消，需要退款金额为0，不再退款");
                return true;
            }
            if (!Check.NuNObj(paymentResultEntity)) {
                //TODO 调用退款，在退款的回调中变更支付状态
                ReceiptRefundSendReq receiptRefundSendReq = new ReceiptRefundSendReq();
                receiptRefundSendReq.setBillNum(paymentResultEntity.getBizCode());
                receiptRefundSendReq.setCancelerId(operaUserCode);

                LOGGER.info("退款金额：{}，支付金额：{}，优惠券使用后抵扣金额：{}", refundAmount, paymentResultEntity.getPayAmount(), needDecordAmount);
                receiptRefundSendReq.setRefundAmount(refundAmount);
                receiptRefundSendReq.setRefundType(ReceiptRefundConstant.RefundType.ORDER_CANCEL.getCode());
                receiptRefundSendReq.setPromotionConsumeList(refundPromotionOrder);
                //receiptRefundSendReq.setRefundAmount(0L);//手续费先设置为0
                LOGGER.info("团购单支付记录：{}", JSON.toJSONString(paymentResultEntity));
                LOGGER.info("团购单退款开始,请求退款接口入参:{}", JSON.toJSONString(receiptRefundSendReq));
                String refundRs = receiptRefundInnerService.sendReceiptRefundRequest(JSON.toJSONString(receiptRefundSendReq));
                LOGGER.info("团购单退款结束，返回值：" + refundRs);
                DataTransferObjectJsonParser.assertSOAReturnSuccess(refundRs);
                //变更订单状态为退款中
                Integer refundstat = activityOrderInfoDao.changePayState(activityOrderCode, ActivityShareConstEnum.TeamByOrderPayStatus.PAIED.getCode()
                        , ActivityShareConstEnum.TeamByOrderPayStatus.REFUND.getCode());
                if (refundstat.intValue() == 1) {
                    result = true;
                } else {
                    LOGGER.info("更改团购订单状态成功，变更支付状态失败");
                }
            }
        } else {
            LOGGER.info("更改团购订单状态失败");
        }
        return result;
    }
*/
}
