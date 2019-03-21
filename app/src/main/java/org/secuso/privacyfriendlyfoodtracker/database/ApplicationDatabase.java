/*
This file is part of Privacy friendly food tracker.

Privacy friendly food tracker is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Privacy friendly food tracker is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Privacy friendly food tracker.  If not, see <https://www.gnu.org/licenses/>.
*/
package org.secuso.privacyfriendlyfoodtracker.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.commonsware.cwac.saferoom.SafeHelperFactory;

import org.secuso.privacyfriendlyfoodtracker.Converter.DateConverter;
import org.secuso.privacyfriendlyfoodtracker.helpers.KeyGenHelper;

import static com.commonsware.cwac.saferoom.SQLCipherUtils.encrypt;
import static com.commonsware.cwac.saferoom.SQLCipherUtils.getDatabaseState;

/**
 * Database singleton.
 *
 * @author Andre Lutz
 */
@Database(entities = {ConsumedEntries.class, Product.class},
        version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class ApplicationDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "consumed_entries_database";

    public abstract ConsumedEntriesDao getConsumedEntriesDao();
    public abstract ProductDao getProductDao();
    public abstract ConsumedEntrieAndProductDao getConsumedEntrieAndProductDao();

    private static ApplicationDatabase sInstance;

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static ApplicationDatabase getInstance(final Context context) throws Exception {
        if (sInstance == null) {
            synchronized (ApplicationDatabase.class) {
                if (sInstance == null) {
                    SafeHelperFactory factory = new SafeHelperFactory(KeyGenHelper.getSecretKeyAsChar(context));

                    sInstance = Room.databaseBuilder(context.getApplicationContext(),ApplicationDatabase.class, DATABASE_NAME)
                            .openHelperFactory(factory)
                            .allowMainThreadQueries()
                            .addCallback(new Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            try {
                                ApplicationDatabase database = ApplicationDatabase.getInstance(context);
                                // notify that the database was created and it's ready to be used
                                database.setDatabaseCreated();
                            } catch (Exception e) {
                                Log.e("ApplicationDatabase", e.getMessage());
                            }
                        }
                    }).build();

                }
            }
        }
        return sInstance;
    }

    private void setDatabaseCreated() {
        mIsDatabaseCreated.postValue(true);
    }

    /**
     * Indicates if the database is created.
     * @return if database is created
     */
    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
