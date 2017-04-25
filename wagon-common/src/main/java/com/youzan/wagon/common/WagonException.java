package com.youzan.wagon.common;

import org.apache.commons.lang.exception.NestableRuntimeException;

public class WagonException extends NestableRuntimeException {

    private static final long serialVersionUID = -7288830284122672209L;

    public WagonException(Throwable cause) {
        super(cause);
    }

    public WagonException(String errorDesc) {
        super(errorDesc);
    }

    public WagonException(String errorDesc, Throwable cause) {
        super(errorDesc, cause);
    }

    public WagonException(String errorCode, String errorDesc) {
        super(errorCode + ":" + errorDesc);
    }

    public WagonException(String errorCode, String errorDesc, Throwable cause) {
        super(errorCode + ":" + errorDesc, cause);
    }


}
