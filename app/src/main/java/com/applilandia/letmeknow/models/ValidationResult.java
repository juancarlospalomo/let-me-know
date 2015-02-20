package com.applilandia.letmeknow.models;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public class ValidationResult {

    public enum ValidationCode {
        Empty,
        LessThanRange,
        GreaterThanRange
    }

    public ValidationCode code;
    public String member;

    public ValidationResult(String property, ValidationCode errorCode) {
        member = property;
        code = errorCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ValidationResult) {
            if ((this.member.equals(((ValidationResult) other).member) &&
                    (code == ((ValidationResult) other).code))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
