package com.applilandia.letmeknow;

import android.test.AndroidTestCase;

import com.applilandia.letmeknow.cross.LocalDate;
import com.applilandia.letmeknow.data.NotificationSet;
import com.applilandia.letmeknow.models.Notification;
import com.applilandia.letmeknow.models.Task;
import com.applilandia.letmeknow.usecases.UseCaseTask;

/**
 * Created by JuanCarlos on 18/03/2015.
 */
public class testNotification extends AndroidTestCase {

    public void testShouldSendNotification() {
        LocalDate date = new LocalDate();
        date.addMinutes(10);
        Task expectedTask = new Task();
        expectedTask.name = "today test task";
        expectedTask.targetDateTime = date;
        expectedTask.typeTask = Task.TypeTask.Today;
        Notification notification = new Notification();
        notification.type = Notification.TypeNotification.FiveMinutesBefore;
        notification.status = Notification.TypeStatus.Pending;
        expectedTask.addNotification(notification);
        UseCaseTask useCaseTask = new UseCaseTask(mContext);
        expectedTask._id = useCaseTask.createTask(expectedTask);

        assertTrue(expectedTask._id>0);
        Task resultTask = useCaseTask.getTask(expectedTask._id);

        int notificationId = resultTask.getNotifications().get(Notification.TypeNotification.FiveMinutesBefore.getValue())._id;

        NotificationSet notificationSet = new NotificationSet(mContext);
        notificationSet.send(notificationId);
    }
}
