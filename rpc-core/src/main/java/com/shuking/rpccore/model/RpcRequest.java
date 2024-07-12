package com.shuking.rpccore.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcRequest implements Serializable {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 调用的服务方法
     */
    private String methodName;
    /**
     * 传递的参数类型
     */
    private Class<?>[] paramsTypes;
    /**
     * 传递的参数
     */
    private Object[] params;

}
