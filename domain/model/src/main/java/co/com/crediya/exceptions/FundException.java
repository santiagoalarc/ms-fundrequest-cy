package co.com.crediya.exceptions;

import co.com.crediya.enums.FundErrorEnum;

public class FundException extends RuntimeException {

    private final FundErrorEnum errorEnum;


    public FundException(FundErrorEnum errorEnum) {
        super(errorEnum.name());
        this.errorEnum = errorEnum;
    }

    public FundErrorEnum getError() {
        return errorEnum;
    }

}
