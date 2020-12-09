package com.xiaoju.automarket.paladin.core.common;

/**
 * @Author Luogh
 * @Date 2020/12/6
 **/
public enum JobStatus {
    SUBMITTED, /*已提交*/
    DEPLOYED, /*已部署*/
    INITIALIZED, /*初始化*/
    RUNNING, /*运行中*/
    CANCELLING, /*停止中*/
    CANCELLED, /*已停止*/
    FAILED, /*已失败*/
}
