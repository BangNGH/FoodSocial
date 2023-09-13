package com.example.foodsocialproject.controller.config.momo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    public String partnerCode;
    public String accessKey;
    public String requestId;
    public String amount;
    public String orderId;
    public String orderInfo;
    public String orderType;
    public String transId;
    public String message;
    public String localMessage;
    public String responseTime;
    public String errorCode;
    public String payType;
    public String extraData;
    public String signature;
}
