package com.sdww8591.servletproxy.interceptor;

import com.sdww8591.servletproxy.entity.Response;

/**
 * ResponseInterceptor用于对Response进行修改，生成我们需要的Response
 * 多个ResponseInterceptor将采用责任链的方式被组织起来
 *
 * 它有三个重要方法：
 * getPriority:用于指明该Interceptor的优先级，优先级的数字越小，该Interceptor越早被执行
 * accept:用于判断该Interceptor是否会处理该Response
 * process:具体业务逻辑，其返回值代表该Response是否会被后续Interceptor处理。
 * 方法返回值代表Response是否会被后续Interceptor处理，如果该值为false，Response将跳过后续所有ResponseInterceptor，直接进行发送阶段
 *
 * Interceptor可能会同时被多个线程并发调用，请保证其中方法的线程安全性
 *
 */
public interface ResponseInterceptor {

    /**
     * 获取该Interceptor的优先级，值较小的ResponseInterceptor将较早对Response进行处理
     * 请保证该方法每次调用时都返回相同的值，修改priority并不能动态修改Interceptor的处理顺序
     * @return 优先级
     */
    int getPriority();

    /**
     * 用于判断Response是否会被当前Interceptor处理
     * true表示会，false将跳过该Interceptor的process方法
     * @param Response 待处理的Response
     * @return 当前Interceptor是否被允许处理Response
     */
    boolean accept(Response Response);

    /**
     * 具体业务逻辑
     * @param Response 待处理的Response
     * @return 代表Response是否会被后续Interceptor处理，如果该值为false，Response将跳过后续所有ResponseInterceptor，直接进行发送阶段
     */
    boolean process(Response Response);
}
