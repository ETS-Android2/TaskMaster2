package com.android.taskmaster2;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//@room.schemaLocation
@Database(entities = {TaskItem.class}, version = 1, exportSchema = false)
public abstract class AppDB extends RoomDatabase {
    public abstract TaskDao taskDao();
}