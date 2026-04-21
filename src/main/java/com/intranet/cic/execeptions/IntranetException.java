package com.intranet.cic.execeptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IntranetException extends RuntimeException {
    private final HttpStatus status;
    public IntranetException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
