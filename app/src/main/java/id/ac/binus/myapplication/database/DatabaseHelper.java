package id.ac.binus.myapplication.database;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import id.ac.binus.myapplication.models.Booking;
import id.ac.binus.myapplication.models.Car;
import id.ac.binus.myapplication.models.User;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "EZDriveDB";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "Users";
    public static final String TABLE_CARS = "Cars";
    public static final String TABLE_BOOKINGS = "Bookings";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    "userId TEXT PRIMARY KEY, " +
                    "userImg BLOB NOT NULL, " +
                    "username TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password TEXT UNIQUE NOT NULL, " +
                    "token TEXT NOT NULL, " +
                    "phoneNumber TEXT NOT NULL, " +
                    "city TEXT NOT NULL, " +
                    "country TEXT NOT NULL)";

    private static final String CREATE_TABLE_CARS =
            "CREATE TABLE " + TABLE_CARS + " (" +
                    "carId TEXT PRIMARY KEY, " +
                    "hostName TEXT NOT NULL," +
                    "location TEXT NOT NULL, " +
                    "description TEXT NOT NULL," +
                    "seats INT NOT NULL," +
                    "transmission TEXT NOT NULL, " +
                    "rules TEXT NOT NULL, " +
                    "carModel TEXT NOT NULL, " +
                    "carBrand TEXT NOT NULL, " +
                    "pricePerDay DECIMAL NOT NULL, " +
                    "availability TEXT NOT NULL, " +
                    "carImg BLOB NOT NULL)";

    private static final String CREATE_TABLE_BOOKINGS =
                "CREATE TABLE " + TABLE_BOOKINGS + " (" +
                "bookingId TEXT PRIMARY KEY, " +
                "userId TEXT NOT NULL, " +
                "carId TEXT NOT NULL, " +
                "startDate TEXT, " +
                "endDate TEXT, " +
                "totalPrice REAL, " +
                "FOREIGN KEY (userId) REFERENCES " + TABLE_USERS + "(userId) ON DELETE CASCADE, " +
                "FOREIGN KEY (carId) REFERENCES " + TABLE_CARS + "(carId) ON DELETE CASCADE)";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }

    public long addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userId", user.getUserId());
        cv.put("userImg", user.getUserImg());
        cv.put("username", user.getUsername());
        cv.put("email", user.getEmail());
        cv.put("password", user.getPassword());
        cv.put("token", user.getToken());
        cv.put("phoneNumber", user.getPhoneNumber());
        cv.put("city", user.getCity());
        cv.put("country", user.getCountry());

        long result = db.insert(TABLE_USERS, null, cv);
        db.close();

        return result;
    }

    public long addCar(Car car){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("carId", car.getCarId());
        cv.put("hostName", car.getHostName());
        cv.put("location", car.getLocation());
        cv.put("description", car.getDescription());
        cv.put("seats", car.getSeats());
        cv.put("transmission", car.getTransmission());
        String rules = String.join(", ", car.getRules());
        cv.put("rules", rules);
        cv.put("carModel", car.getModel());
        cv.put("carBrand", car.getBrand());
        cv.put("pricePerDay", car.getPricePerDay());
        cv.put("availability", car.getAvailability());
        cv.put("carImg", car.getCarImg());

        long result = db.insert(TABLE_CARS, null, cv);
        db.close();

        return result;
    }

    public long editCar(String carId, byte[] carImg, String carBrand, String carModel, String carHost,
                        int carSeats, String carTransmission, String carLocation,
                        double carPrice, String carDescription, String carRules){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("carImg", carImg);
        cv.put("carBrand", carBrand);
        cv.put("carModel", carModel);
        cv.put("hostName", carHost);
        cv.put("seats", carSeats);
        cv.put("transmission", carTransmission);
        cv.put("location", carLocation);
        cv.put("pricePerDay", carPrice);
        cv.put("description", carDescription);
        cv.put("rules", carRules);

        long result = db.update("Cars", cv, "carId = ?", new String[]{carId});
        db.close();

        return result;
    }

    public long editProfile(String userId, byte[] userImg, String username,
                            String email, String phoneNumber, String city, String country){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("userImg", userImg);
        cv.put("username", username);
        cv.put("email", email);
        cv.put("phoneNumber", phoneNumber);
        cv.put("city", city);
        cv.put("country", country);

        long result = db.update("Users", cv, "userId = ?", new String[]{userId});
        db.close();

        return result;
    }

    public void deleteCar(String carId){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CARS, "carId = ?", new String[]{carId});
        db.close();
    }

    public long addBooking(Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateFormatted = sdf.format(booking.getStartDate());
        String endDateFormatted = sdf.format(booking.getEndDate());

        String sql = "INSERT INTO " + TABLE_BOOKINGS + " (bookingId, userId, carId, startDate, endDate, totalPrice) VALUES(?,?,?,?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);

        statement.clearBindings();
        statement.bindString(1, booking.getBookingId());
        statement.bindString(2, booking.getUserId());
        statement.bindString(3, booking.getCarId());
        statement.bindString(4, startDateFormatted);
        statement.bindString(5, endDateFormatted);
        statement.bindDouble(6, booking.getTotalPrice());

        return statement.executeInsert();
    }

    public Car getCarByCarId(String carId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Car car = null;

        String query = "SELECT * FROM Cars WHERE carId = ?";
        Cursor cursor = db.rawQuery(query, new String[]{carId});

        if (cursor.moveToFirst()) {
            String carID = cursor.getString(cursor.getColumnIndexOrThrow("carId"));
            String carBrand = cursor.getString(cursor.getColumnIndexOrThrow("carBrand"));
            String carModel = cursor.getString(cursor.getColumnIndexOrThrow("carModel"));
            String hostName = cursor.getString(cursor.getColumnIndexOrThrow("hostName"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            int seats = cursor.getInt(cursor.getColumnIndexOrThrow("seats"));
            String transmission = cursor.getString(cursor.getColumnIndexOrThrow("transmission"));
            String rules = cursor.getString(cursor.getColumnIndexOrThrow("rules"));
            double pricePerDay = cursor.getDouble(cursor.getColumnIndexOrThrow("pricePerDay"));
            String availability = cursor.getString(cursor.getColumnIndexOrThrow("availability"));
            byte[] carImg = cursor.getBlob(cursor.getColumnIndexOrThrow("carImg"));
            ArrayList<String> convertedRules = new ArrayList<>(Arrays.asList(rules.split(",")));

            car = new Car(carImg, carID, carBrand, hostName, location, description, seats, transmission, carModel, pricePerDay, availability, convertedRules);
        }

        cursor.close();
        return car;
    }

    public ArrayList<Booking> getAllBookings(String userId) {
        ArrayList<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            String query = "SELECT Users.userId, Cars.carId, Cars.carImg, Cars.carBrand, Cars.carModel, Cars.pricePerDay, " +
                    "Bookings.startDate, Bookings.endDate, Bookings.totalPrice " +
                    "FROM Bookings " +
                    "INNER JOIN Cars ON Bookings.carId = Cars.carId " +
                    "INNER JOIN Users ON Bookings.userId = Users.userId " +
                    "WHERE Bookings.userId = ?";

            cursor = db.rawQuery(query, new String[]{userId});

            if (cursor.moveToFirst()) {
                do {
                    byte[] carImg = cursor.getBlob(cursor.getColumnIndexOrThrow("carImg"));
                    String carId = cursor.getString(cursor.getColumnIndexOrThrow("carId"));
                    String carBrand = cursor.getString(cursor.getColumnIndexOrThrow("carBrand"));
                    String carModel = cursor.getString(cursor.getColumnIndexOrThrow("carModel"));
                    String carName = carBrand + " " + carModel;
                    double pricePerDay = cursor.getDouble(cursor.getColumnIndexOrThrow("pricePerDay"));
                    String startDate = cursor.getString(cursor.getColumnIndexOrThrow("startDate"));
                    String endDate = cursor.getString(cursor.getColumnIndexOrThrow("endDate"));
                    double totalPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("totalPrice"));

                    @SuppressLint("SimpleDateFormat")
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date convertedStartDate = null;
                    Date convertedEndDate = null;

                    try {
                        convertedStartDate = formatter.parse(startDate);
                        convertedEndDate = formatter.parse(endDate);
                    } catch (ParseException e) {
                        System.out.println("Error Converting Booking Date!");
                    }

                    Booking booking = new Booking(carImg, carId, carName, pricePerDay, convertedStartDate, convertedEndDate, totalPrice);
                    bookings.add(booking);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            System.out.println("Error Converting Booking Date!");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return bookings;
    }

    public ArrayList<User> getAllUsers(){
        ArrayList<User> users = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.rawQuery("SELECT * FROM Users", null);

        if(cursor.moveToFirst()){
            do {

                String userId = cursor.getString(cursor.getColumnIndexOrThrow("userId"));
                String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                byte[] userImg = cursor.getBlob(cursor.getColumnIndexOrThrow("userImg"));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phoneNumber"));
                String country = cursor.getString(cursor.getColumnIndexOrThrow("country"));
                String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
                String password = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                String token = cursor.getString(cursor.getColumnIndexOrThrow("token"));

                users.add(new User(userId, userImg, username, password, phoneNumber, city, country, email, token));
            } while (cursor.moveToNext());
        }

        return users;
    }

    @SuppressLint("Recycle")
    public ArrayList<Car> getCarsByRole(String username){
        ArrayList<Car> cars = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        @SuppressLint("Recycle")
        Cursor cursor;

        if(username.equalsIgnoreCase("admin")){
            cursor = db.rawQuery("SELECT * FROM Cars", null);
        } else{
            cursor = db.rawQuery("SELECT * FROM Cars WHERE availability = 'Available'", null);
        }

        if(cursor.moveToFirst()){
            do {
                String carId = cursor.getString(cursor.getColumnIndexOrThrow("carId"));
                byte[] carImg = cursor.getBlob(cursor.getColumnIndexOrThrow("carImg"));
                String carBrand = cursor.getString(cursor.getColumnIndexOrThrow("carBrand"));
                String carModel = cursor.getString(cursor.getColumnIndexOrThrow("carModel"));
                String hostName = cursor.getString(cursor.getColumnIndexOrThrow("hostName"));
                String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int seats = cursor.getInt(cursor.getColumnIndexOrThrow("seats"));
                String transmission = cursor.getString(cursor.getColumnIndexOrThrow("transmission"));
                double pricePerDay = cursor.getDouble(cursor.getColumnIndexOrThrow("pricePerDay"));
                String availability = cursor.getString(cursor.getColumnIndexOrThrow("availability"));
                String rules = cursor.getString(cursor.getColumnIndexOrThrow("rules"));

                ArrayList<String> convertedRules = new ArrayList<>(Arrays.asList(rules.split(",")));

                cars.add(new Car(carImg, carId, carBrand, hostName, location, description, seats, transmission, carModel, pricePerDay, availability, convertedRules));
            } while (cursor.moveToNext());
        }

        return cars;
    }

    public void updateCarStatus(String carId){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("availability", "Not Available");
        db.update("Cars", cv, "carId = ?", new String[]{carId});
        System.out.println("Car Status Updated!");
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_CARS);
        db.execSQL(CREATE_TABLE_BOOKINGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

}
