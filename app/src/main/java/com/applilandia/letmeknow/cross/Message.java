package com.applilandia.letmeknow.cross;

import android.content.Context;

import com.applilandia.letmeknow.models.Task;

import java.util.List;

/**
 * Created by JuanCarlos on 27/04/2015.
 */
public class Message {

    private Context mContext;

    public Message(Context context) {
        mContext = context;
    }

    /**
     * Create a string with a task list
     *
     * @param taskList list of tasks
     * @return String with the list of tasks
     */
    public String getFormattedTaskMessage(List<Task> taskList) {
        String message = "";
        String crlf = System.getProperty("line.separator");
        if (taskList != null) {
            for (Task task : taskList) {
                message += getFormattedTaskMessage(task) + crlf;
            }
        }
        return message;
    }

    /**
     * Create a string with the task data
     *
     * @param task task to convert in a string
     * @return string with the task data
     */
    public String getFormattedTaskMessage(Task task) {
        String message = "";
        if (task != null) {
            message += task.name;
            if ((task.targetDateTime != null) && (!task.targetDateTime.isNull())) {
                message += "    " + task.targetDateTime.getDisplayFormatWithToday(mContext);
            }
        }
        return message;
    }

}
