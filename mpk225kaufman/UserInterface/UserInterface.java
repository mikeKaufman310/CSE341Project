//package mpk225kaufman.UserInterface;


import java.util.*;
import java.lang.*;
import java.sql.*;
public class UserInterface{
    
    //Fields
    static String password = "";
    static String userName = "";
    static Scanner scan = new Scanner(System.in);


    /**
     * Main Method
     * @param args instructions
     */
    public static void main(String[] args){
        //display logo
        displayLogo();
        //NOTE: maybe loop  login attempts and handle wrong password
        logIn(scan);
        System.out.println();

        
        try( Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
            Statement s = con.createStatement();){
                displayMenu();
                int choice = menuOption(scan);
                runOption(choice);
        }
        catch(Exception e){
            System.out.println(e);      //NOTE: make good Exception Catch
        }
    }

    public static void displayLogo(){
        System.out.println("\n\nHOTEL CALIFORNIA:");
        System.out.println("Check in anytime you like, but you can never leave!");
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

    public static void logIn(Scanner scan){
        System.out.println("Login:");
        System.out.println("--------------------");
        boolean go = false;//loop control variable
        while(!go){
            try{
                System.out.print("Input Username:\t");
                String tempUser = scan.next();
                //prepared statement function
                if(sqlInjection(tempUser)){
                    throw new Exception();
                }
                userName = tempUser;//update static username
                //password

                System.out.print("Input Password:\t");
                String tempPass = scan.next();//NOTE: may need a buffer clear
                scan.nextLine();
                //NOTE: maybe check for some other kind of attack
                password = tempPass;
                if(userName != "" && password != ""){
                    go = true;
                }
            }
            catch(Exception e){
                System.out.println("Invalid Input, Try Again");
            }
        }
    }

    //prepared statment to try to defer sql injection
    //returns true if possible sql injection attack, false otherwise
    public static boolean sqlInjection(String userName){
        return false;//set false for testing before implementation
    }

    public static int menuOption(Scanner scan){
        int choice = -1;//-1 if no choice made
        boolean go = false;//loop control boolean
        System.out.println();
        do{
            try{
                System.out.print("Enter a Choice (1-6):\t");
                //scan.next();
                String tempChoiceString = scan.next();
                int tempChoice = Integer.parseInt(tempChoiceString);
                if(tempChoice >= 1 && tempChoice <= 6){
                    choice = tempChoice;
                    go = true;
                }else{
                    throw new Exception();
                }
            }
            catch(Exception e){
                System.out.println("Invalid Input, Try Again");
            }
        }while(!go);
        scan.close();
        return choice;
    }

    public static void runOption(int choice){
        if(choice == 1){
            makeReservation();
        }else if(choice == 2){
            checkIn();
        }else if(choice == 3){
            checkOut();
        }else if(choice == 4){
            cancellation();
        }else if(choice == 5){
            payment();
        }else{
            seeAvailablity();
        }
    }

    public static void makeReservation(){//need arguments
        
    }

    public static void checkIn(){//need argument
    
    }

    public static void checkOut(){//need arguments
    
    }

    public static void cancellation(){//need argument
    
    }

    public static void payment(){//need arguments
    
    }

    public static void seeAvailablity(){//need arguments
    
    }
}
