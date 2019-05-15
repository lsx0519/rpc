package com.lsx.rpc.client;

import com.lsx.rpc.register.RpcDiscover;
import rpc.common.RpcRequest;
import rpc.common.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcProxy {
    private RpcDiscover rpcDiscover;

    public <T> T getInstance(Class<T> serviceClass) {
        T instance = (T) Proxy.newProxyInstance(
                    serviceClass.getClassLoader(),
                     new Class<?>[]{serviceClass},
                        new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest rpcRequest = new RpcRequest();

                        String serviceName = method.getDeclaringClass().getName();

                        Class<?>[] parameterTypes = method.getParameterTypes();

                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(serviceName);
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(parameterTypes);
                        rpcRequest.setParameters(args);

                        RpcResponse rpcResponse = new RpcClient(rpcRequest,rpcDiscover).send();


                        return rpcResponse.getResult();
                    }
                });

        return instance;
    }
}
