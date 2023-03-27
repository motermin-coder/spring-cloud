package com.example.myproductservice.util;

import org.springframework.util.Assert;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> userContext = new ThreadLocal<>();

    public static final UserContext getContext() {
        UserContext context = UserContextHolder.userContext.get();
        if (context == null){
            userContext.set(new UserContext());
        }
        return userContext.get();
    }

    public static final void setUserContext(UserContext context){
        Assert.notNull(userContext,"Only non-null UserContext instance are permitted");
        userContext.set(context);
    }
}
