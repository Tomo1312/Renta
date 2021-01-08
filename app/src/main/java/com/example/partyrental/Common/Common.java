package com.example.partyrental.Common;

import com.example.partyrental.Model.HouseCard;
import com.example.partyrental.Model.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Common {
    public static String AuthCredentials = "";
    public static User currentUser;
    public static HouseCard selectedHouse;
    public static Calendar endDate;
    public static Calendar startDate;
    public static SimpleDateFormat simpleFormatDate = new SimpleDateFormat("dd_MM_yyyy");
    public static SimpleDateFormat simpleFormatDateWithDot = new SimpleDateFormat("dd.MM.yyyy");
    public static String county;
    public static HouseCard ownerHouse;
    public static int step = 0;
    public static String userPhone;
}
