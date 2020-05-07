package com.example.redis.faceredis;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * User: zhangll
 * Date: 2020-05-07
 * Time: 15:50
 */
public class ShareAndHelp {

    /**
     * 领取优惠券
     *
     * @return status 2 已领取
     * status 1 失败
     * status 0 领取成功
     */
    /*@RequestMapping("/receive")
    public ResponseEntity receivePromotion(InviteFriendActivityReceiveReq request) {
        *//**
         * 领取优惠券：
         * 领取优惠券规则
         1.判断活动是否结束
         2.判断该分享的助力记录是否已经达到最大预警值
         3.判断领取人是否为发起者本人
         4.判断领取人是否领取过该分享者的券
         5.判断领取人是否已经拥有该活动的最高领券次数 10次
         6.判断领取人是否友家用户，友家用户领取5元卧室保洁券
         7.判断领取人客户类别：新客领取 券包，老客1->优惠券类型1，老客2->优惠券类型2
         * 先进行领取明细记录，异步领取优惠券，反写领取成功标记（同一个分享shareLogicCode，openId，只能对应一个手机号领取）
         *//*
        String lock = ACTIVITY_INVITE_FRIEND_RECEIVE_LOCK + request.getShareLogicCode();
        paramCheckLogic.checkParamValidate(Json.toJsonString(request), InviteFriendActivityReceiveReq.class);
        if (redisSentinelLockHandler.getMutexLock(lock, 6000)) {
            try {
                PromoActivityShareLogByLogicReq reqLogic = new PromoActivityShareLogByLogicReq();
                reqLogic.setLogicCode(request.getShareLogicCode());
                // 根据shareLogicCode查询uid、券包码、活动进行时间限制等字段
                PromoActivityShareLogResponse promoActivityShareLogResponse = coupanActivityService.findAllByCode(reqLogic);
                if (Check.NuNObj(promoActivityShareLogResponse)) {
                    ResponseDto responseDto = ResponseDto.responseFail("您领取的分享记录不存在");
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }

                //1.判断活动是否结束
                PromotionActiveEntity entity = promotionActiveService.findActivieByCode(promoActivityShareLogResponse.getActivityCode());
                //2.判断该分享的助力记录是否已经达到最大预警值
                String activityHelpLogString = coupanActivityService.findHelpLogByShareLogicCode(request.getShareLogicCode());
                List<PromotionActivityHelpLogEntity> activityHelpLogList = DataTransferObjectJsonParser.getListValueFromDataByKey(activityHelpLogString, "data", PromotionActivityHelpLogEntity.class);
                if (activityHelpLogList.size() >= Integer.parseInt(AddPraiseEnum.INVITE_FRIENDS_RECEIVE_MAX.getDefaultValue())) {
                    ResponseDto responseDto = ResponseDto.responseFail("推荐的好友已达到上限，请查询");
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }

                //3.判断领取人是否为发起者本人 通过openId

                CouponJudgeReq req = new CouponJudgeReq();
                req.setActivityCode(promoActivityShareLogResponse.getActivityCode());
                req.setOpenId(request.getOpenId());
                req.setShareLogicCode(request.getShareLogicCode());
                req.setUserPhone(request.getPhone());
                Optional<PromoActivityShareLogResponse> shareOptional = promotionActivityShareLogService.findByCode(request.getShareLogicCode());
                //0. 助力者，1. 失败 , 2.发起者 , 3,未知身份
                int identity = couponActivityProcess.judgeIdentity(req, shareOptional);
                if (2 == identity) {
                    //本人不可以
                    ResponseDto responseDto = ResponseDto.responseFail("本人不可以领取自己分享的券");
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }
                if (3 == identity) {//未知身份,APP分享
                    //1.检查,手机号不为空时，如果手机号相同，则为本人不可以领取自己分享的券
                    //case1. 若app进行分享，则openId绑定关系中openid是空，此时如果用微信再进行该shareLogicCode的发起，如果微信的openId已经和userCode2进行了绑定，则该app发起的记录是永远无法绑定openId，
                    //并且在用户认为本人发起的活动，竟然可以在微信中进行领取
                    //case2. 若进行反绑定，需根据手机号是否为发起的手机号，或者手机号查到的用户userCode是否为发起记录的userCode，此时疑点1凭什么要用该微信的openId进行绑定到该记录。
                    //疑点2.如果先进行了绑定该微信openId，则该微信连接无法进行任何手机号的领取，如果先领取了再进行绑定微信openId也违反了本人不可以领取自己分享的券的规则。
                    if (!Check.NuNStr(shareOptional.get().getUserPhone()) && !Check.NuNStr(req.getUserPhone()) && Objects.equals(shareOptional.get().getUserPhone(), req.getUserPhone())) {
                        ResponseDto responseDto = ResponseDto.responseFail("本人不可以领取自己分享的券");
                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    } else {//2.如果手机号不相同，发起者手机号为空时，用用户uid判断，如果用户uid相同，则为本人不可以领取自己分享的券
                        ZrkUserEntity zrkUserEntity = ZrkUserUtil.getInfoByMobile(request.getPhone());
                        if (!Check.NuNObj(zrkUserEntity)) {
                            String curUid = zrkUserEntity.getUid();
                            if (!Check.NuNStr(curUid) && Objects.equals(curUid, shareOptional.get().getUserCode())) {
                                ResponseDto responseDto = ResponseDto.responseFail("本人不可以领取自己分享的券");
                                return new ResponseEntity(responseDto, HttpStatus.OK);
                            }
                        }
                    }
                }

                *//**
                 * 当前open_id 第一次通过 a 的手机号领取此优惠券时，应该返回当前领取的手机号a和a手机号领取的金额
                 * 1，当前open_id 再次输入 a 时，提示此手机号已经参加过活动，返回输入的手机号和 手机号领取的金额
                 * 2，当前open_id 输入 一个没有领取过优惠券的手机号 b 时，返回此open_id 之前领取过的 手机号 和 手机号的金额
                 * 3，当前open_id 输入 已经领取过优惠券的手机号 b 时，返回手机号 b 和 b手机号领取的金额
                 *//*

                //4.判断领取人是否领取过该分享者的券（领取过了提示：1如果点击过该分享的人并且已经有手机号领取过，本次领取的手机号已存在则提示改手机号领取过了，2否则提示您已参与过该分享的领券活动）
                PromotionActivityHelpLogSearchReq helpLogSearchReq = new PromotionActivityHelpLogSearchReq();
                helpLogSearchReq.setShareLogCode(request.getShareLogicCode());
                helpLogSearchReq.setPageSize(2000);
                List<PromotionActivityHelpLogEntity> helpLogEntities = coupanActivityService.searchHelpLogByCondition(helpLogSearchReq);
                List<PromotionActivityHelpLogEntity> helpLogEntitiesA = helpLogEntities.stream().filter(e -> request.getOpenId().equals(e.getWxOpenId())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(helpLogEntitiesA)) {
                    ResponseDto responseDto = ResponseDto.responseFail("您已经参与过此活动，快去推荐好友得奖励吧～");
                    responseDto.setCode(2);
                    //已经领取过的人，需要返回已经领取的金额
                    FindHelpPromotionCodeListSearchReq friendsReq = new FindHelpPromotionCodeListSearchReq();
                    friendsReq.setSource(promoActivityShareLogResponse.getActivityCode());
                    friendsReq.setRecommendAccount(promoActivityShareLogResponse.getUserCode());
                    friendsReq.setUserPhones(Arrays.asList(helpLogEntitiesA.get(0).getUserPhone()));

                    //当前open_id 输入 已经领取过优惠券的手机号 b 时，返回手机号 b 和 b手机号领取的金额
                    List<PromotionActivityHelpLogEntity> helpLogEntitiesC = helpLogEntities.stream().filter(e -> request.getPhone().equals(e.getUserPhone())).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(helpLogEntitiesC)) {
                        friendsReq.setUserPhones(Arrays.asList(request.getPhone()));
                    }
                    String promotionCode = promotionCodeOuterService.findPromotionByUserAndPhones(JSON.toJSONString(friendsReq));
                    List<PromotionCodeEntity> promotionCodeEntities = DataTransferObjectJsonParser.getListValueFromDataByKey(promotionCode, "data", PromotionCodeEntity.class);
                    if (!Check.NuNCollection(promotionCodeEntities)) {
                        Optional<PromotionCodeEntity> codeEntityOp = promotionCodeEntities.stream().max(Comparator.comparingDouble(PromotionCodeEntity::getAmount));
                        PromotionCodeEntity codeEntity = codeEntityOp.get();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("amount", codeEntity.getAmount());
                        map.put("phone", friendsReq.getUserPhones().get(0)); //手机号不确定是哪一个，friendsReq 中的最准确
                        responseDto.setData(map);
                    }

                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }
                //判断领取人的手机号是否已经领取过该分享人分享的活动
                List<PromotionActivityHelpLogEntity> helpLogEntitiesB = helpLogEntities.stream().filter(e -> request.getPhone().equals(e.getUserPhone())).collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(helpLogEntitiesB)) {
                    ResponseDto responseDto = ResponseDto.responseFail("您已领取过此张优惠券，快去下单吧");
                    responseDto.setCode(2);

                    //已经领取过的人，需要返回已经领取的金额
                    FindHelpPromotionCodeListSearchReq friendsReq = new FindHelpPromotionCodeListSearchReq();
                    friendsReq.setSource(promoActivityShareLogResponse.getActivityCode());
                    //没有领取过优惠券的微信，输入一个已经领取优惠券的手机号时，返回已经领取手机号的金额
                    friendsReq.setUserPhones(Arrays.asList(request.getPhone()));
                    friendsReq.setRecommendAccount(promoActivityShareLogResponse.getUserCode());
                    String promotionCode = promotionCodeOuterService.findPromotionByUserAndPhones(JSON.toJSONString(friendsReq));
                    List<PromotionCodeEntity> promotionCodeEntities = DataTransferObjectJsonParser.getListValueFromDataByKey(promotionCode, "data", PromotionCodeEntity.class);
                    if (!Check.NuNCollection(promotionCodeEntities)) {
                        Optional<PromotionCodeEntity> codeEntityOp = promotionCodeEntities.stream().max(Comparator.comparingDouble(PromotionCodeEntity::getAmount));
                        PromotionCodeEntity codeEntity = codeEntityOp.get();
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("amount", codeEntity.getAmount());
                        map.put("phone", request.getPhone());
                        responseDto.setData(map);
                    }

                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }

                //5.判断领取人是否已经拥有该活动的最高领券次数 10次  通过
                InviteFriendReceiveSearchReq receiveSearchReq = new InviteFriendReceiveSearchReq();
                receiveSearchReq.setActivityCode(entity.getActivityCode());
                receiveSearchReq.setPhone(request.getPhone());
                Integer receiveCoount = coupanActivityService.getReceiveCountByCondition(receiveSearchReq);
                if (!Check.NuNObj(receiveCoount) && receiveCoount >= MAX_HELP_TIMES) {
                    ResponseDto responseDto = ResponseDto.responseFail("您已超过领券次数啦，快去推荐好友得奖励吧～");
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }


                //校验客户类型
                String customerType = null;
                ZrkUserEntity zrkUserEntity = ZrkUserUtil.getInfoByMobile(request.getPhone());
                String userCode = Check.NuNObj(zrkUserEntity) ? null : zrkUserEntity.getUid();

                PromotionCodeBatchGenerateReq batchGenerateReq = new PromotionCodeBatchGenerateReq();
                List<String> userInfos = new ArrayList<>();
                String userName = Check.NuNObj(zrkUserEntity) ? null : zrkUserEntity.getName();
                userInfos.add(userCode + "->" + request.getPhone());
                batchGenerateReq.setUserInfos(userInfos);
                batchGenerateReq.setOperatorName(userName);

                if (!Check.NuNStrStrict(userCode)) {
                    boolean isZrk = couponActivityProcess.hasEffectiveContract(userCode);
                    if (isZrk) {
                        //发送卧室保洁券
                        customerType = CustomerTypeEnum.ZR_FRIEND.getCustomerTypeStatus();
                    } else {
                        //非友家
                        String customerJson = userOrderQueryOuterService.judgeIsNewUserByUserCode(userCode);
                        DataTransferObjectJsonParser.assertSOAReturnSuccess(customerJson);
                        customerType = Json.parseJsonNode(customerJson, "data").get("data").get("customerTypeStatus").textValue();
                    }
                } else {
                    //未查询到userCode时按照新用户处理
                    customerType = CustomerTypeEnum.ZR_NEW_CUSTOMER.getCustomerTypeStatus();
                }


                boolean valid = checkConfig();
                if (!valid) {
                    String message = "领取优惠券--客户类型与优惠券类型配置数据为空配置参数非法";
                    LogUtil.error(LOGGER, message);
                    Cat.logError(message, new IllegalArgumentException(message));
                    ResponseDto responseDto = ResponseDto.responseFail(message);
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }

                List<CustomerTypePromotion> customerTypePromotions = customerTypePromotionConfig.customerTypePromotionList();
                String finalCustomerType = customerType;
                List<CustomerTypePromotion> opCustomerType = customerTypePromotions.stream().filter(e -> e.getCustomerType().equals(finalCustomerType)).collect(Collectors.toList());
                if (Check.NuNCollection(opCustomerType)) {
                    ResponseDto responseDto = ResponseDto.responseFail("您暂时不符合领券资格。。。");
                    return new ResponseEntity(responseDto, HttpStatus.OK);
                }

                //调用 保存助力记录
                PromotionActivityInsertHelpLogReq helpLogReq = new PromotionActivityInsertHelpLogReq();
                helpLogReq.setActivityCode(promoActivityShareLogResponse.getActivityCode());
                helpLogReq.setWxOpenId(request.getOpenId());
                helpLogReq.setShareLogCode(request.getShareLogicCode());
                helpLogReq.setModifyUserCode(request.getOpenId());
                helpLogReq.setHelpPackageCode(promoActivityShareLogResponse.getActivityCode());
                helpLogReq.setCreateUserCode(request.getOpenId());
                //设置领取人
                helpLogReq.setUserCode(userCode);
                helpLogReq.setUserPhone(request.getPhone());
                helpLogReq.setHelpTimes(1);
                //判断是否为新客户，新客户发送券包，如果已经领取过该券包，则继续发送领取5元优惠券
                if (CustomerTypeEnum.ZR_NEW_CUSTOMER.getCustomerTypeStatus().equals(customerType)) {
                    PromotionPackageReceiveReq receiveReq = new PromotionPackageReceiveReq();
                    receiveReq.setPhone(request.getPhone());
                    receiveReq.setPackageMarkCode(opCustomerType.get(0).getTypeCode());
                    receiveReq.setChannelCode(promoActivityShareLogResponse.getUserCode());
                    receiveReq.setUserCode(userCode);
                    receiveReq.setSource(promoActivityShareLogResponse.getActivityCode());
                    receiveReq.setReceiveWay(PromotionConst.ReceiveWay.INITIATIVE_RECEIVE.getCode());
                    String result = promotionPackageOuterService.receivePromotionCodeByPackage(Json.toJsonString(receiveReq));
                    int code = DataTransferObjectJsonParser.getReturnCode(result);
                    //已经领取过券包了，继续领取5元的
                    if (2 == code) {
                        customerType = CustomerTypeEnum.ZR_SEC_NEW_CUSTOMER.getCustomerTypeStatus();
                    }
                    if (0 != code && 2 != code) {
                        //领取失败
                        ResponseDto responseDto = ResponseDto.responseFail(DataTransferObjectJsonParser.getReturnMsg(result));
                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    }

                    if (code == 0) {
                        List<PromotionCodeTypeEntity> types = DataTransferObjectJsonParser.getListValueFromDataByKey(result, "promotionCodeTypeData", PromotionCodeTypeEntity.class);

                        int insertCount = coupanActivityService.insertHelpLogByShareLogCodeLock(helpLogReq);
                        if (insertCount == 0) {
                            ResponseDto responseDto = ResponseDto.responseFail("领取优惠券失败，请稍后再试");
                            return new ResponseEntity<>(responseDto, HttpStatus.OK);
                        }

                        //2018-10-26 领取成功时，返回领取的金额 和 领取优惠券的手机号
                        HashMap<String, Object> map = new HashMap<>();
                        if (!Check.NuNCollection(types)) {
                            map.put("amount", types.get(0).getAmount());
                        }
                        map.put("phone", request.getPhone());
                        ResponseDto responseDto = ResponseDto.responseOK(map);
                        responseDto.setMessage("领取成功");
                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    }
                }

                String finalCustomerType2 = customerType;
                if (!CustomerTypeEnum.ZR_NEW_CUSTOMER.getCustomerTypeStatus().equals(customerType)) {
                    List<CustomerTypePromotion> opCustomerType2 = customerTypePromotions.stream().filter(e -> e.getCustomerType().equals(finalCustomerType2)).collect(Collectors.toList());
                    if (Check.NuNCollection(opCustomerType)) {
                        ResponseDto responseDto = ResponseDto.responseFail("您暂时不符合领券资格。。。");
                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    }

                    batchGenerateReq.setTypeCode(opCustomerType2.get(0).getTypeCode());
                    batchGenerateReq.setValidDay(Integer.valueOf(opCustomerType2.get(0).getValidDays()));
                    batchGenerateReq.setChannelCode(promoActivityShareLogResponse.getUserCode());
                    batchGenerateReq.setSource(promoActivityShareLogResponse.getActivityCode());
                    //新增认领方式
                    batchGenerateReq.setReceiveWay(PromotionConst.ReceiveWay.INITIATIVE_RECEIVE.getCode());
                    String result = promotionCodeOuterService.batchSendPromotionCode(Json.toJsonString(batchGenerateReq));
                    String message = DataTransferObjectJsonParser.getReturnMsg(result);
                    if (DataTransferObjectJsonParser.checkSOAReturnSuccess(result)) {

                        List<PromotionCodeTypeEntity> types = DataTransferObjectJsonParser.getListValueFromDataByKey(result, "promotionCodeTypeData", PromotionCodeTypeEntity.class);
                        int insertCount = coupanActivityService.insertHelpLogByShareLogCodeLock(helpLogReq);
                        if (insertCount == 0) {
                            ResponseDto responseDto = ResponseDto.responseFail("领取优惠券失败，请稍后再试");
                            return new ResponseEntity<>(responseDto, HttpStatus.OK);
                        }

                        //2018-10-26 领取成功时，返回领取的金额 和 领取优惠券的手机号
                        HashMap<String, Object> map = new HashMap<>();
                        if (!Check.NuNCollection(types)) {
                            map.put("amount", types.get(0).getAmount());
                        }
                        map.put("phone", request.getPhone());
                        ResponseDto responseDto = ResponseDto.responseOK(map);
                        responseDto.setMessage("领取成功");

                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    } else {
                        ResponseDto responseDto = ResponseDto.responseFail(message);
                        return new ResponseEntity(responseDto, HttpStatus.OK);
                    }
                }

                ResponseDto responseDto = ResponseDto.responseOK("领取成功");
                return new ResponseEntity(responseDto, HttpStatus.OK);

            } catch (Exception e) {
                LOGGER.error("领取优惠券发生错误，error:{}", e);
                ResponseDto responseDto = ResponseDto.responseFail(e.getMessage());
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            } finally {
                redisSentinelLockHandler.releaseLock(lock);
            }
        } else {
            ResponseDto responseDto = ResponseDto.responseFail("正在领取,请稍后再试");
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
    }*/

   /* private boolean checkConfig() {
        LogUtil.info(LOGGER, "领取优惠券--客户类型与优惠券类型配置----开始进行参数校验");
        if (Objects.isNull(customerTypePromotionConfig)) {

            customerTypePromotionConfig = ApplicationContext.getContext().getBean(CustomerTypePromotionConfig.class);
        }

        List<CustomerTypePromotion> customerTypePromotions = customerTypePromotionConfig.customerTypePromotionList();
        if (CollectionUtils.isEmpty(customerTypePromotions)) {
            String message = "领取优惠券--客户类型与优惠券类型配置数据为空----";
            LogUtil.error(LOGGER, message);
            Cat.logError(message, new IllegalArgumentException(message));
            return false;
        }

        LogUtil.info(LOGGER, "领取优惠券--客户类型与优惠券类型配置----结束参数校验，参数校验成功");
        return true;
    }*/

    /**
     * 发起助力活动
     * 1.检查活动有效性
     * 2 活动时效
     * 3.检查user表是否存在uid，没有的话需要插入一条记录
     * 4.校验用户有没有发起资格
     * 5.发起活动
     *
     * @param inviteFriendsAddShareRequest
     * @return
     */
    /*@RequestMapping("/share")
    public ResponseEntity<ResponseDto> share(InviteFriendsAddShareRequest inviteFriendsAddShareRequest) {
        inviteFriendsAddShareRequest = paramCheckLogic.checkObjParamValidate(inviteFriendsAddShareRequest);
        TokenUtil.getUser(inviteFriendsAddShareRequest);
        //1.检查活动有效性
        String activityResult = coupanActivityService.getActivieByCode(inviteFriendsAddShareRequest.getActivityCode());
        if (!DataTransferObjectJsonParser.checkSOAReturnSuccess(activityResult)) {
            ResponseDto responseDto = ResponseDto.responseFail(DataTransferObjectJsonParser.getReturnMsg(activityResult));
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }

        //获取用户uid锁，防止重复点击
        if (redisSentinelLockHandler.getMutexLock(ACTIVITY_FRIEND_USER_LOCK + inviteFriendsAddShareRequest.getUserCode(), 3500)) {
            try {
                //2.检查user表是否存在uid，没有的话需要插入一条记录
                PromotionUserOpenIdSearchReq userOpenIdSearchReq = new PromotionUserOpenIdSearchReq();
                userOpenIdSearchReq.setUserCode(inviteFriendsAddShareRequest.getUserCode());
                userOpenIdSearchReq.setActivityCode(inviteFriendsAddShareRequest.getActivityCode());
                userOpenIdSearchReq.setActivityTheme(inviteFriendsAddShareRequest.getActivityTheme());
                PromotionUserOpenIdEntity userEntity = coupanActivityService.findUserByCondition(userOpenIdSearchReq);
                if (Check.NuNObj(userEntity)) {
                    ZrkUserEntity zrkUserEntity = ZrkUserUtil.getInfoByUid(inviteFriendsAddShareRequest.getUserCode());
                    if (Check.NuNObj(zrkUserEntity) || Check.NuNObj(zrkUserEntity.getPhone())) {
                        LOGGER.error("获取用户数据发生错误，uid：{}", inviteFriendsAddShareRequest.getUserCode());
                        ResponseDto responseDto = ResponseDto.responseFail(MessageSourceUtil.getChinese(messageSource, MessageConst.LAUNCH_ACTIVITY_USER_INFO_ERROR));
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                    PromotionBindUserOpenIdReq userOpenIdReq = new PromotionBindUserOpenIdReq();
                    userOpenIdReq.setActivityCode(inviteFriendsAddShareRequest.getActivityCode());
                    userOpenIdReq.setOpenId(inviteFriendsAddShareRequest.getOpenId());
                    userOpenIdReq.setOpenType(CouponActivityEnum.OpenType.WEIXIN.getCode());
                    userOpenIdReq.setUserCode(inviteFriendsAddShareRequest.getUserCode());
                    userOpenIdReq.setUserPhone(zrkUserEntity.getPhone());

                    String insertResult = coupanActivityService.bindUserInfo(userOpenIdReq);
                    if (!DataTransferObjectJsonParser.checkSOAReturnSuccess(insertResult)) {
                        LOGGER.error("绑定用户数据发生错误，error：{}", Json.toJsonString(userOpenIdReq));
                        ResponseDto responseDto = ResponseDto.responseFail(MessageSourceUtil.getChinese(messageSource, MessageConst.LAUNCH_ACTIVITY_ERROR));
                        return new ResponseEntity<>(responseDto, HttpStatus.OK);
                    }
                }
                //3.检验该用户是否发起过该活动，已发起，返回shareLogicCode
                String shareResult = inviteFriendsActivityService.findShareLogByUserCodeAndActivity(inviteFriendsAddShareRequest.getUserCode(), inviteFriendsAddShareRequest.getActivityCode());
                List<PromotionActivityShareLogEntity> promotionActivityShareLogEntities = DataTransferObjectJsonParser.getListValueFromDataByKey(shareResult, "data", PromotionActivityShareLogEntity.class);
                if (!Check.NuNCollection(promotionActivityShareLogEntities)) {
                    ResponseDto responseDto = ResponseDto.responseFail(MessageSourceUtil.getChinese(messageSource, MessageConst.LAUNCH_ACTIVITY_ACTIVE_ONLY_ONCE));
                    LaunchShareResponse launchShareResponse = LaunchShareResponse.construct(promotionActivityShareLogEntities.get(0).getLogicCode());
                    responseDto.setCode(2);
                    responseDto.setData(launchShareResponse);
                    return new ResponseEntity<>(responseDto, HttpStatus.OK);
                }
                //4.保存发起活动记录
                InviteFriendsAddShareReq inviteFriendsAddShareReq = new InviteFriendsAddShareReq();
                String shareLogicCode = UUIDGenerator.hexUUID();
                inviteFriendsAddShareReq.setLogicCode(shareLogicCode);
                inviteFriendsAddShareReq.setActivityCode(inviteFriendsAddShareRequest.getActivityCode());
                inviteFriendsAddShareReq.setOpenId(inviteFriendsAddShareRequest.getOpenId());
                inviteFriendsAddShareReq.setUserCode(inviteFriendsAddShareRequest.getUserCode());

                String launchResult = inviteFriendsActivityService.addShare(inviteFriendsAddShareReq);
                if (DataTransferObjectJsonParser.checkSOAReturnSuccess(launchResult)) {
                    //发起成功
                    LaunchShareResponse launchShareResponse = LaunchShareResponse.construct(shareLogicCode);
                    ResponseDto responseDto = ResponseDto.responseOK(launchShareResponse);
                    responseDto.setMessage("发起成功");
                    return new ResponseEntity<>(responseDto, HttpStatus.OK);
                } else {
                    ResponseDto responseDto = ResponseDto.responseFail("发起失败");
                    return new ResponseEntity<>(responseDto, HttpStatus.OK);
                }
            } catch (Exception e) {
                LOGGER.error("发起分享发生错误，error:{}", e.getMessage());
                ResponseDto responseDto = ResponseDto.responseFail("发起失败");
                return new ResponseEntity<>(responseDto, HttpStatus.OK);
            } finally {
                redisSentinelLockHandler.releaseLock(ACTIVITY_FRIEND_USER_LOCK + inviteFriendsAddShareRequest.getUserCode());
            }
        } else {
            ResponseDto responseDto = ResponseDto.responseFail("正在发起,请稍后再试");
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
    }
*/


    //0. 助力者，1. 失败 , 2.发起者 , 3,未知身份
    /*public int judgeIdentity(CouponJudgeReq req, Optional<PromoActivityShareLogResponse> shareOptional) {
        if (!shareOptional.isPresent()) {
            return 1;
        }
        PromoActivityShareLogResponse resp = shareOptional.get();

        //两者 openId相等是发起者
        if (!Check.NuNStrStrict(resp.getOpenId()) && req.getOpenId().equals(resp.getOpenId())) {
            return 2;
        }
        //两者 openId不相等是助力者
        if (!Check.NuNStrStrict(resp.getOpenId()) && !req.getOpenId().equals(resp.getOpenId())) {
            return 0;
        }
        //没有openId
        if (Check.NuNStrStrict(resp.getOpenId())) {
            return judgeIdentityByNoOpenId(req, resp);
        }
        return 3;

    }*/

    /*public int judgeIdentityByNoOpenId(CouponJudgeReq req, PromoActivityShareLogResponse resp) {
        PromoActivityShareSearchByConditionReq shareSearchByConditionReq = new PromoActivityShareSearchByConditionReq();
        shareSearchByConditionReq.setOpenType(CouponActivityEnum.OpenType.WEIXIN.getCode());
        shareSearchByConditionReq.setOpenId(req.getOpenId());
        shareSearchByConditionReq.setShareLogicCode(req.getShareLogicCode());
        List<PromoActivityShareLogResponse> shareLogResponses = promotionActivityShareLogService.findByContion(shareSearchByConditionReq);
        //没有数据 未知身份
        if (Check.NuNCollection(shareLogResponses)) {
            return 3;
        }
        if (!Check.NuNCollection(shareLogResponses)) {
            PromoActivityShareLogResponse promoActivityShareLogResponse = shareLogResponses.get(0);
            //发起者
            if (resp.getUserCode().equals(promoActivityShareLogResponse.getUserCode())) {
                return 2;
            }
            //助力者
            if (!resp.getUserCode().equals(promoActivityShareLogResponse.getUserCode())) {
                return 0;
            }
        }
        return 3;
    }*/
}
