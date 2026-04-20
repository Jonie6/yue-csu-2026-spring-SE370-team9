package com.example.clockedinprojectt9.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.clockedinprojectt9.dao.EventDao;
import com.example.clockedinprojectt9.dao.RSVPDao;
import com.example.clockedinprojectt9.dao.UserDao;
import com.example.clockedinprojectt9.models.Event;
import com.example.clockedinprojectt9.models.RSVP;
import com.example.clockedinprojectt9.models.User;

@Database(entities = {User.class, Event.class, RSVP.class}, version = 1, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {

    private static volatile AppDataBase INSTANCE;

    public abstract UserDao userDao();
    public abstract EventDao eventDao();
    public abstract RSVPDao rsvpDao();

    public static AppDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBase.class, "clocked_in_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
