package com.nekonade.common.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseEntity {

    private int errorCode;

    private String errorMsg;

    private Object data;
}
