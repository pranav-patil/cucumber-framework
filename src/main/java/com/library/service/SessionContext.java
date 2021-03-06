package com.library.service;

import com.library.model.User;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public interface SessionContext {

    IvParameterSpec getIvParamSpec();
    SecretKey getSecurityKey() throws Exception;
    User getLoggedInUser();
}
