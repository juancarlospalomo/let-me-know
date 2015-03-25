package com.applilandia.letmeknow.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.applilandia.letmeknow.cross.LocalDate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuanCarlos on 18/02/2015.
 * Task entity domain model
 */
public class Task implements IValidatable, Parcelable {

    private final static String LOG_TAG = Task.class.getSimpleName();

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

        public static TypeTask map(int value) {
            return values()[value];
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
    private SparseArray<Notification> mNotifications;

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel source) {
            return new Task(source);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Task() {
        super();
    }

    /**
     * Create the object from a parcel
     *
     * @param parcel
     */
    public Task(Parcel parcel) {
        Log.e(LOG_TAG, "Task");
        //Read in the same order it was parceled in the writeToParcel method
        _id = parcel.readInt();
        name = parcel.readString();
        String value = parcel.readString();
        if (!TextUtils.isEmpty(value)) {
            try {
                targetDateTime = new LocalDate(value);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            targetDateTime = null;
        }
        value = parcel.readString();
        if (!TextUtils.isEmpty(value)) {
            typeTask = TypeTask.map(Integer.parseInt(value));
        } else {
            typeTask = null;
        }
        readParcelableNotifications(parcel.readParcelableArray(Notification.class.getClassLoader()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.e(LOG_TAG, "writeToParcel");
        //Write with a fixed order
        dest.writeInt(_id);
        dest.writeString(name);
        if (targetDateTime == null) {
            dest.writeString(null);
        } else {
            dest.writeString(targetDateTime.toString());
        }
        if (typeTask == null) {
            dest.writeString(null);
        } else {
            dest.writeString(String.valueOf(typeTask.getValue()));
        }
        dest.writeParcelableArray(convertNotificationsToParcelableArray(), 0);
    }

    /**
     * Load the parcelables into the notification list
     * @param parcelables
     */
    private void readParcelableNotifications(Parcelable[] parcelables) {
        if (parcelables != null) {
            for(Parcelable parcelable : parcelables) {
                Notification notification = (Notification) parcelable;
                addNotification(notification);
            }
        }
    }

    /**
     * Convert Notification List to a Parcelable Array
     *
     * @return
     */
    private Parcelable[] convertNotificationsToParcelableArray() {
        if (mNotifications != null) {
            Parcelable[] parcelables = new Parcelable[mNotifications.size()];
            for (int index = 0; index < mNotifications.size(); index++) {
                parcelables[index] = mNotifications.get(index);
            }
            return parcelables;
        }
        return null;
    }

    /**
     * Set the current existing notifications in database for this task
     *
     * @param value current notifications number
     */
    public void setCurrentNotificationsCount(int value) {
        mCurrentNotificationsCount = value;
    }

    /**
     * Return the number of notifications in database for this task
     *
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
            mNotifications = new SparseArray<>();
        }
        LocalDate notificationDateTime = new LocalDate(targetDateTime);
        if (notification.type == Notification.TypeNotification.FiveMinutesBefore) {
            notificationDateTime.addMinutes(-5);
        }
        if (notification.type == Notification.TypeNotification.OneHourBefore) {
            notificationDateTime.addHours(-1);
        }
        if (notification.type == Notification.TypeNotification.OneDayBefore) {
            notificationDateTime.addDays(-1);
        }
        if (notification.type == Notification.TypeNotification.OneWeekBefore) {
            notificationDateTime.addDays(-7);
        }
        notification.dateTime = notificationDateTime.getDateTime();
        mNotifications.put(notification.type.getValue(), notification);
    }

    /**
     * Return the notification which type is passed
     *
     * @param typeNotification
     * @return Notification or null
     */
    public Notification getNotification(Notification.TypeNotification typeNotification) {
        return mNotifications.get(typeNotification.getValue());
    }

    /**
     * Set the element in the array to null
     *
     * @param typeNotification type of notification to remove
     */
    public void removeNotification(Notification.TypeNotification typeNotification) {
        mNotifications.remove(typeNotification.getValue());
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
    public SparseArray<Notification> getNotifications() {
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
     * Find out if a type of notification is allowed according to the task target date.
     * If target date - previous time for notification is less than current date time it isn't allowed
     *
     * @param typeNotification
     * @return true if the notification is allowed
     */
    public boolean isNotificationAllowed(Notification.TypeNotification typeNotification) {
        boolean isAllowed = false;
        LocalDate notificationDateTime = new LocalDate(targetDateTime);
        if (typeNotification == Notification.TypeNotification.FiveMinutesBefore) {
            notificationDateTime.addMinutes(-5);
        }
        if (typeNotification == Notification.TypeNotification.OneHourBefore) {
            notificationDateTime.addHours(-1);
        }
        if (typeNotification == Notification.TypeNotification.OneDayBefore) {
            notificationDateTime.addDays(-1);
        }
        if (typeNotification == Notification.TypeNotification.OneWeekBefore) {
            notificationDateTime.addDays(-7);
        }
        if (notificationDateTime.compareTo(new LocalDate()) >= 0) {
            isAllowed = true;
        }
        return isAllowed;
    }

    /**
     * Return if the current TypeTask is equal to another one
     *
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
            if (!isValidLengthName(name)) {
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

    public static boolean isValidLengthName(String name) {
        if (name.length() > NAME_MAX_SIZE) {
            return false;
        }
        return true;
    }

}
