//package mpk225kaufman.UserInterface;


import java.util.*;
import java.lang.*;
import java.sql.*;
public class UserInterface{
    /**
     * Main Method
     * @param args instructions
     */
    public static void main(String[] args){
        //display logo
        displayLogo();
        //NOTE: sign in prevent sql injection
        displayMenu();
        //NOTE: put scan function here
        System.out.println();
    }

    public static void displayLogo(){
        System.out.println("\n\nHOTEL CALIFORNIA:");
        System.out.println("You can check in anytime you like, but you can never leave");
        System.out.println("\n---------------------------------------------------------------\n\n");
    }

    public static void displayMenu(){
        System.out.println("Menu:");
        System.out.println("--------------------");
        System.out.println("1.  Make Reservation");
        System.out.println("2.  Check In");
        System.out.println("3.  Check Out");
        System.out.println("4.  Cancellation");
        System.out.println("5.  Pay");
        System.out.println("6.  See Availability");
    }

    public static String[] parseDate(String date){
        String[] dates = new String[3];//mm-dd-yy => mm is 0 and dd is 1 and y is 2 =. parse on -
        dates = date.split("-");
        /*String mm = dates[0];
        String dd = dates[1];
        String yy = dates[2];*/
        return dates;
    }
}