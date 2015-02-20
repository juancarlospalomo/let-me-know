package com.applilandia.letmeknow.models;

import java.util.Date;

/**
 * Created by JuanCarlos on 18/02/2015.
 */
public class History {
    //Local history task identifier
    public int _id;
    //Task name
    public String name;
    //Task target date;
    public Date targetDate;
    //The date when the task was actually completed
    public Date completedDate;

    @Override
    public boolean equals(Object other) {
        if (other instanceof History) {
            History history = (History) other;
            if ((history.name.equals(this.name)) &&
                    (history.targetDate.equals(this.targetDate)) &&
                    (history.completedDate.equals(this.completedDate))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
