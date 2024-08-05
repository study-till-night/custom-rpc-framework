package com.shuking.rpccore.constant;

/**
 * 容错策略键名常量
 *
 */
public interface TolerantStrategyConstants {

    /**
     * 快速失败
     */
    String FAIL_FAST = "fastFail";

    /**
     * 故障转移
     */
    String TRANSFER = "transfer";

    /**
     * 静默处理
     */
    String SILENCE = "silence";

}
