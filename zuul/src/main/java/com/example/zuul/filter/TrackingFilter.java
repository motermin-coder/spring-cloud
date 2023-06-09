package com.example.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TrackingFilter extends ZuulFilter {

    private static final int FILTER_ORDER = 1;
    private static final boolean SHOULD_FILTER = true;
    private static final Logger log = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;


    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }

    private boolean isCorrelationIdPresent(){
        if (filterUtils.getCorrelationId() != null){
            return true;
        }
        return false;
    }

    private String generateCorrelationId(){
        return UUID.randomUUID().toString();
    }

    @Override
    public Object run() {
        if (isCorrelationIdPresent()){
            log.info("前置拦截中发现 tmx-correlation-id: {}",filterUtils.getCorrelationId());
        } else {
            filterUtils.setCorrelationId(generateCorrelationId());
            log.info("tmx-correlation-id 在前置拦截器中生成了：{}",filterUtils.getCorrelationId());
        }
        RequestContext currentContext = RequestContext.getCurrentContext();
        log.info("处理传入的请求 {}", currentContext.getRequest().getRequestURI());
        return null;
    }
}
