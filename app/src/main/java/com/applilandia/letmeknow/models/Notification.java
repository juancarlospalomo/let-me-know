package com.applilandia.letmeknow.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by JuanCarlos on 18/02/2015.
 * Notification entity domain model
 */
public class Notification implements Parcelable {

    public enum TypeNotification {
        FiveMinutesBefore(0),
        OneHourBefore(1),
        OneDayBefore(2),
        OneWeekBefore(3);

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
            //As the first number starts with 0
            //we have only to return the position belonging to value in the array
            return values()[value];
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

    public final static Creator<Notification> CREATOR = new Creator<Notification>() {
        @Override
        public Notification createFromParcel(Parcel source) {
            return new Notification(source);
        }

        @Override
        public Notification[] newArray(int size) {
            return new Notification[size];
        }
    };

    public Notification() {
        super();
    }

    /**
     * Create the object from a parcel
     *
     * @param parcel
     */
    public Notification(Parcel parcel) {
        //Read in the same order they was written in writeToParcel method
        _id = parcel.readInt();
        taskId = parcel.readInt();
        dateTime = new Date(parcel.readLong());
        type = TypeNotification.map(parcel.readInt());
        status = TypeStatus.map(parcel.readInt());
    }

    /**
     * Parcelable Interface
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeInt(taskId);
        dest.writeLong(dateTime.getTime());
        dest.writeInt(type.getValue());
        dest.writeInt(status.getValue());
    }



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
