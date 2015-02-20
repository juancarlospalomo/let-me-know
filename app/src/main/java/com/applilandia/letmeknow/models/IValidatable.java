package com.applilandia.letmeknow.models;

import java.util.List;

/**
 * Created by JuanCarlos on 20/02/2015.
 */
public interface IValidatable {
    public List<ValidationResult> validate();
}
