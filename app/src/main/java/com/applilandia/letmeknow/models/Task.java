package com.applilandia.letmeknow.models;

import android.text.TextUtils;

import com.applilandia.letmeknow.cross.LocalDate;

import java.util.ArrayList;
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

    //It has the current notifications stored in database for the task
    private int mCurrentNotificationsCount = 0;

    //Local identifier for the task
    public int _id;
    //task name
    public String name;
    //Date & time when the task has to be carried out
    public LocalDate targetDateTime;
    //Type task. This field isn't persisted in database
    public TypeTask typeTask;
    //List of notifications for the task
    private List<Notification> mNotifications;

    /**
     * Set the current existing notifications in database for this task
     * @param value current notifications number
     */
    public void setCurrentNotificationsCount(int value) {
        mCurrentNotificationsCount = value;
    }

    /**
     * Return the number of notifications in database for this task
     * @return
     */
    public int getCurrentNotificationsCount() {
        return mCurrentNotificationsCount;
    }

    /**
     * Add a notification to the list of them
     *
     * @param notification to add
     */
    public void addNotification(Notification notification) {
        if (mNotifications == null) {
            mNotifications = new ArrayList<Notification>();
        }
        mNotifications.add(notification);
    }

    /**
     * Set notifications list to null
     */
    public void removeNotifications() {
        mNotifications = null;
    }

    /**
     * Return the current notifications list
     *
     * @return list of notifications
     */
    public List<Notification> getNotifications() {
        return mNotifications;
    }

    /**
     * find out if any notification exists
     *
     * @return true if it has some notification
     */
    public boolean hasNotifications() {
        return ((mNotifications != null) && ((mNotifications.size() > 0)));
    }

    /**
     * Return if the current TypeTask is equal to another one
     * @param other
     * @return
     */
    private boolean areTypeTaskEquals(TypeTask other) {
        if ((typeTask == null) && (other == null)) {
            return true;
        } else {
            if (typeTask == null) {
                return false;
            } else {
                if (other == null) {
                    return false;
                } else {
                    if (!typeTask.equals(other)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
    }

    /**
     * compare if the current task is equal to the one passed as parameter
     *
     * @param other task object to compare with
     * @return true if both tasks are equal
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof Task) {
            //Both targetDateTime could be null
            boolean bothAreNull = ((this.targetDateTime == null) && (((Task) other).targetDateTime == null));
            if (this.name.equals(((Task) other).name)) {
                if (bothAreNull) {
                    return true;
                } else {
                    boolean someIsNull = ((this.targetDateTime == null) || (((Task) other).targetDateTime == null));
                    if (someIsNull) {
                        return false;
                    } else {
                        if (this.targetDateTime.equals(((Task) other).targetDateTime)) {
                            return areTypeTaskEquals(((Task) other).typeTask);
                        } else {
                            return false;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Validate the task entity constraints
     *
     * @return list of validation errors o null
     */
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
        if (targetDateTime != null) {
            if (targetDateTime.compareTo(new LocalDate()) < 0) {
                result.add(new ValidationResult("targetDateTime", ValidationResult.ValidationCode.LessThanRange));
            }
        }
        if (result.size() == 0) {
            result = null;
        }
        return result;
    }

}
