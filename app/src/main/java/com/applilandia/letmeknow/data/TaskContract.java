package com.applilandia.letmeknow.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by JuanCarlos on 18/02/2015.
 */
public class TaskContract {
    //Content authority is a name for the entire content provider. A good choice
    //to use as this string is the package name of the app, which is unique on
    //the device
    public static final String CONTENT_AUTHORITY = "com.applilandia.letmeknow";
    //Base for all URI´s in the app
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //Paths to be added to the base content URI for setting the URI´s
    //For instance, content://com.productivity.letmeknow/task will be a valid path
    public static final String PATH_TASK = "task";
    public static final String PATH_NOTIFICATION = "notification";
    public static final String PATH_HISTORY = "history";

    /**
     *  inner class that defines the table contents of the Task table
     */
    public static final class TaskEntry implements BaseColumns {
        //content URI for Task Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TASK).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_TASK;
        //MIME format for a cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_TASK;
        //Table name
        public static final String TABLE_NAME = "task";
        //Task name
        public static final String COLUMN_TASK_NAME = "name";
        //Date & time for the task
        public static final String COLUMN_TARGET_DATE_TIME = "target_datetime";

        /**
         *Alias for fields if they are needed
         */
        public static final String ALIAS_ID = TABLE_NAME + "_" + _ID;

        //Uri functions to manage the parameters

        /**
         * extract task id from uri
         * @param uri to match
         * @return task identifier
         */
        public static int getUriTaskId(Uri uri) {
            int taskId = Integer.parseInt(uri.getPathSegments().get(1));
            return taskId;
        }

        /**
         * Build URI adding the task id
         * @param id task identifier
         * @return URI
         */
        public static Uri setUriTaskId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    /**
     * inner class that defines the table contents of the Notification table
     */
    public static final class NotificationEntry implements BaseColumns {
        //Content URI for Notification Entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFICATION).build();
        //MITE format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_NOTIFICATION;
        //MIME format for cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_NOTIFICATION;
        //Table name
        public static final String TABLE_NAME = "notification";
        //Task ID referenced
        public static final String COLUMN_TASK_ID = "task_id";
        //Date & time where the notification has to be triggered
        public static final String COLUMN_DATE_TIME = "date_time";
        //Type of notification: 5 minutes before, 1 day before, ...
        public static final String COLUMN_TYPE = "type";
        //Current status of the notification
        public static final String COLUMN_STATUS = "status";

        //functions to manager uri parameters

        /**
         * Extract the notification id from the uri
         * @param uri uri to match
         * @return notification identifier
         */
        public static int getUriNotificationId(Uri uri) {
            int id = Integer.parseInt(uri.getPathSegments().get(1));
            return id;
        }

        /**
         * Build URI adding the notification id
         * @param id notification identifier
         * @return URI
         */
        public static Uri setUriNotificationId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /**
     * inner class the defines the table contents for the History table
     */
    public static final class HistoryEntry implements BaseColumns {
        //Content URI for history entities
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build();
        //MIME format for cursor
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_HISTORY;
        //MIME format for cursor item
        public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item" +
                "vnd." + CONTENT_AUTHORITY + "." + PATH_HISTORY;
        //Table name
        public static String TABLE_NAME = "history";
        //Task name
        public static String COLUMN_TASK_NAME = "name";
        //Last target date for the task
        public static String COLUMN_TARGET_DATE_TIME ="target_datetime";
        //Last completed date for the task
        public static String COLUMN_COMPLETED_DATE_TIME = "compleated_date_time";
    }



}
