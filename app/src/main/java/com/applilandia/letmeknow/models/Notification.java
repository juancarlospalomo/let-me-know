package com.applilandia.letmeknow.models;

import java.util.Date;

/**
 * Created by JuanCarlos on 18/02/2015.
 * Notification entity domain model
 */
public class Notification {

    public enum TypeNotification {
        FiveMinutesBefore(1),
        OneHourBefore(2),
        OneDayBefore(3),
        OneWeekBefore(4);

        private int mValue = 0;

        private TypeNotification(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        /**
         * map a int value to the enum value
         * @param value int to map
         * @return enum value
         */
        public static TypeNotification map(int value) {
            //As the first number starts with 1
            //and the array starts by 0, we must to take the before item in the array
            return values()[value - 1];
        }

    }

    /**
     * Enum for possible notification status
     * Pending: notification created and not sent yet to the notification bar
     * Sent: notification already sent to the notification bar and user didn't open it yet
     */
    public enum TypeStatus {
        Pending(1),
        Sent(2);

        private int mValue = 0;

        private TypeStatus(int value) {
            mValue = value;
        }

        public int getValue() {
            return mValue;
        }

        /**
         * map a int value to the enum value
         * @param value int to map
         * @return enum value
         */
        public static TypeStatus map(int value) {
            //As the first number starts with 1
            //and the array starts by 0, we must to take the before item in the array
            return values()[value - 1];
        }

    }

    //local identifier of the notification
    public int _id;
    //task identifier that notification belongs to. It is like a FK
    public int taskId;
    //date & time where the notification alarm has to be triggered
    public Date dateTime;
    //type of notification
    public TypeNotification type;
    //current status of the notification
    public TypeStatus status;

    @Override
    public boolean equals(Object other) {
        if (other instanceof Notification) {
            Notification notification = (Notification) other;
            if ((notification.taskId == this.taskId) &&
                    (notification.type.getValue() == this.type.getValue()) &&
                    (notification.dateTime.equals(this.dateTime)) &&
                    (notification.status.getValue() == this.status.getValue())) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
