package com.cfs.bms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SeatUnavaliableException extends RuntimeException{

    public SeatUnavaliableException(String message)
    {
        super(message);
    }
}