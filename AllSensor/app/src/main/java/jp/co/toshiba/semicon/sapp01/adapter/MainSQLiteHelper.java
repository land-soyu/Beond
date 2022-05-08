package jp.co.toshiba.semicon.sapp01.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by sjkim on 17. 9. 20.
 */

public class MainSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "maindata.db";
    private static final int DATABASE_VERSION = 1108;


    public static final String TABLE_NAME_MAIN_LIST = "table_main_list";
    public static final String TABLE_NAME_USER_LIST = "table_user_list";
    public static final String TABLE_NAME_WORKING_LIST = "table_working_list";
    public static final String TABLE_NAME_CHECK_LIST = "table_check_list";
    public static final String TABLE_NAME_CHECK_LIST2 = "table_check_list2";

    public static final String COL_INEDEX = "_id";
    public static final String SENSORNAME = "sensorname";
    public static final String SENSORVALUE = "sensorvalue";

    public static final String U_NAME = "name";
    public static final String U_YEAR = "year";
    public static final String U_SEX = "sex";
    public static final String U_HEIGH = "tall";
    public static final String U_WEIGHT = "weight";
    public static final String ETC = "etc";

    public static final String DATE = "Tdate";
    public static final String W_WORK_COUNT = "work_count";
    public static final String W_WORK_KCAL = "work_kcal";

    public static final String C_HR = "hr";
    public static final String C_SPO2 = "spo2";
    public static final String C_BMI = "bmi";
    public static final String C_STRESS = "stress";
    public static final String C_FATP = "fatp";
    public static final String C_FATM = "fatm";
    public static final String C_MUSCLEM = "musclem";
    public static final String C_BASICM = "basicm";
    public static final String C_WATER = "water";
    public static final String C_WALK = "walk";



    private static final String DATABASE_CREATE_CHECK_LIST = "create table "
            + TABLE_NAME_CHECK_LIST+ "(" + COL_INEDEX
            + " integer primary key autoincrement, "
            + DATE + " text , "
            + SENSORVALUE + " text , "
            + ETC + " datetime);";

    public MainSQLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_CHECK_LIST);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CHECK_LIST);
        onCreate(db);
    }

    public void deleteCheck(SQLiteDatabase db){
        db.execSQL("delete from " + TABLE_NAME_CHECK_LIST);
//        onCreate(db);
    }


    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }




    public void checkInsert(SQLiteDatabase db, String Tdate , String value, String etc){
        String sql = "insert into " + TABLE_NAME_CHECK_LIST + " values(NULL, '" + Tdate + "', '" + value +  "', '" + etc +  "' );";
        db.execSQL(sql);
    }


    public void select(SQLiteDatabase db) {

        Cursor ch = db.rawQuery("select * from table_check_list;", null);
        while(ch.moveToNext()) {
            String id = ch.getString(0);
            String date = ch.getString(1);
            String hr = ch.getString(2);
            String spo2 = ch.getString(3);
            String bmi  = ch.getString(4);
            String basicm = ch.getString(5);
            String etc = ch.getString(6);
            Log.d("sqlite", "working data ==== "+ date + "hr == "+  hr+  "spo2 == " + spo2 + bmi + basicm + etc);

        }

    }







}