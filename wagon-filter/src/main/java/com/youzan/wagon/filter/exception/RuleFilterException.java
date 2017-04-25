package com.youzan.wagon.filter.exception;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class RuleFilterException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    public RuleFilterException(Throwable cause) {
        super(cause);
    }

    public RuleFilterException(String errorDesc) {
        super(errorDesc);
    }

    public RuleFilterException(String errorDesc, Throwable cause) {
        super(errorDesc, cause);
    }

}
