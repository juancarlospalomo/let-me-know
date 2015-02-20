package com.applilandia.letmeknow.models;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by JuanCarlos on 18/02/2015.
 * Task entity domain model
 */
public class Task implements IValidatable {
    //Max size constraint for name field
    private static final int NAME_MAX_SIZE = 50;
    /**
     * Enum for stating the task status
     */
    public enum TypeTask {
        All(0),
        Expired(1),
        Today(2),
        Future(3),
        AnyTime(4);

        private int mValue = 0;

        private TypeTask(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }
    }

    //Local identifier for the task
    public int _id;
    //task name
    public String name;
    //Date & time when the task has to be carried out
    public Date targetDatetime;
    //Type task. This field isn't persisted in database
    public TypeTask typeTask;
    //List of notifications for the task
    public List<Notification> notifications;

    @Override
    public boolean equals(Object other) {
        if (other instanceof Task) {
            if ((this.name.equals(((Task) other).name)) &&
                    (this.targetDatetime.equals(((Task) other).targetDatetime))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public List<ValidationResult> validate() {
        List<ValidationResult> result = new ArrayList<ValidationResult>();
        if (TextUtils.isEmpty(name)) {
            result.add(new ValidationResult("name", ValidationResult.ValidationCode.Empty));
        } else {
            if (name.length() > NAME_MAX_SIZE) {
                result.add(new ValidationResult("name", ValidationResult.ValidationCode.GreaterThanRange));
            }
        }
        if (targetDatetime!=null) {
            if (targetDatetime.compareTo(new Date()) < 0) {
                result.add(new ValidationResult("targetDateTime", ValidationResult.ValidationCode.LessThanRange));
            }
        }
        if (result.size()==0) {
            result = null;
        }
        return result;
    }

}
