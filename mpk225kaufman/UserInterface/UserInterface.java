//package mpk225kaufman.UserInterface;


import java.util.*;
import java.sql.*;
import java.time.*;

public class UserInterface{
    
    //Fields
    static String password = "";
    static String userName = "";
    //static Scanner scan = new Scanner(System.in);
    static final int[] userInterfaces = {1, 2, 3};//array to determine which interface (front desk clerk, housekeeping, etc.)
    //1 is front desk clerk, 2 is house keeping, 3 is customer
    static int userInterface = 0;
    //current date
    static String currentDate;


    /**
     * Main Method
     * @param args instructions
     */
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        //display logo
        displayLogo();
        boolean go = true;;// var for failed password //NOTE: maybe loop  login attempts and handle wrong password
        currentDate = currentDate();
        do{
            logIn(scan);
            System.out.println();
            try(Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
                Statement s = con.createStatement();){
                    int option = displayUserInterfaceOptions(scan);
                    int cid = -1;
                    if(option == 1 || option == 3){
                        String[] cidAndName = customer(scan, option);//to be parsed
                        cid = parseCid(cidAndName[0]);
                        String customerName = cidAndName[1];
                        if(cid == -1){//need a new customer id
                            //get new value for cid
                            try{
                                ResultSet maxCid = s.executeQuery("select max(c_id) from customer");
                                if(maxCid.next())
                                    cid = Integer.parseInt(maxCid.getString("max(c_id)"));
                            }
                            catch(SQLException e){
                                System.out.println("flag");//for test
                            }
                            customerName = name(scan);
                            //System.out.println(customerName);//for test
                            //scan.nextLine();//buffer clear
                            cid++;
                            String customerInsert = newCustomer(scan, cid, customerName);
                            s.executeUpdate(customerInsert);    
                        }else if(cid == -23){
                            System.out.println("\nUI Test Mode Engaged\n");
                        }//else check if cid matches one in system OR just say in readme that all customers are honest too idk
                    }    
                    displayMenu(option);
                    int choice = menuOption(scan, option);
                    if((option == 1 && choice == 6) || (option ==2 && choice == 2) || (option == 3 && choice == 4)){
                        con.close();
                        System.out.println("\nLogging Out...\n\nGoodbye! Take it Easy!");
                        System.exit(0);
                    }
                    String q = runOption(scan, choice, cid, option);
                    if(q != null){
                        s.executeUpdate(q);
                    }
                    con.close();
                    System.out.println("\nLogging Out...\n\nGoodbye! Take it Easy!");
                    go = true;
            }
            catch(SQLException e){
                if(e.getErrorCode() == 1017){
                    System.out.println("\nInvalid Login Attempt, Try Again\n");
                    go = false;
                }else{
                    e.printStackTrace();
                    System.out.println("\nLogging Out...\n\nGoodbye! Take it Easy!");
                    go = true;
                }
            }
            catch(Exception e){
                System.out.println(e);
                System.out.println("\nLogging Out...\n\nGoodbye! Take it Easy!");
                go = true;      
            }
        }while(!go);
        scan.close();
    }


    /**
     * Method to make a global current date for program and checking
     * @return String of currentDate
     */
    public static String currentDate(){
        LocalDate date = LocalDate.now();
        String day = Integer.toString((date.getDayOfMonth()));
        String month = Integer.toString((date.getMonthValue()));
        String year = Integer.toString((date.getYear()));
        return (month + "-" + day + "-" + year);
    }

    /**
     * Method to display logo when application is started up
     */
    public static void displayLogo(){
        System.out.println("\n\nHOTEL CALIFORNIA:");
        System.out.println("Check in anytime you like, but you can never leave!");
        System.out.println("\n---------------------------------------------------------------\n\n");
    }

    /**
     * Method to display user interface options
     * OPTIONS: 
     * @param scan Scanner object for system.in
     * @return int chosen option for user interface
     */
    public static int displayUserInterfaceOptions(Scanner scan){
        boolean go = false;
        int option = 0;
        do{
            System.out.println("1.  Front Desk Clerk");
            System.out.println("2.  Housekeeping");
            System.out.println("3.  Customer\n");//TBD may change to customer interface cuz I don't wanna change my rates
            try{
                System.out.print("Enter Interface Option:\t");
                option = Integer.parseInt(scan.next());
                if(option >= 1 && option <= 3){
                    go = true;
                }else{
                    throw new Exception("invalid");
                }
            }
            catch(Exception e){
                System.out.println("\nInvalid Option, Try Again\n");
            }
        }while(!go);
        return option;
    }

    /**
     * Method to display menu of possible actions based on user interface option
     * @param option user interface option chosen value
     */
    //need to update based on interface number
    public static void displayMenu(int option){
        System.out.println("Menu:");
        System.out.println("--------------------");
        if(option == 1){
            System.out.println("1.  Check In");
            System.out.println("2.  Check Out");
            System.out.println("3.  Cancellation");
            System.out.println("4.  Pay");
            System.out.println("5.  See Availability");
            System.out.println("6.  Exit");
        }else if(option == 2){
            System.out.println("1.  Mark Room As Clean");
            System.out.println("2.  Exit");
        }else if(option == 3){
            System.out.println("1.  Make Reservation");
            System.out.println("2.  Pay");
            System.out.println("3.  See Availability");
            System.out.println("4.  Exit");
        }
        
    }

    /**
     * Method to parse the chose date format for the database system
     * DATE FORMAT: mm-dd-yy 
     * NOTE: the dashes in between mm, dd, and yy are not optional
     * @param date String for date in format
     * @return size 3 array of strings (months, days, years)
     */
    public static String[] parseDateStringArr(String date){
        String[] dates = new String[3];//mm-dd-yy => mm is 0 and dd is 1 and y is 2 =. parse on -
        dates = date.split("-");
        /*String mm = dates[0];
        String dd = dates[1];
        String yy = dates[2];*/
        return dates;
    }

    /**
     * Method to prompt user via system.in for login credentials
     * @param scan  Scanner object initialized to system.in
     */
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

    //NOTE: may go unused
    /**
     * Method to check login credentials for sql injection attack
     * @param userName username to be checked for sql injection
     * @return boolean => true if possible attack, false otherwise
     */
    //prepared statment to try to defer sql injection
    //returns true if possible sql injection attack, false otherwise
    public static boolean sqlInjection(String userName){
        return false;//set false for testing before implementation
    }

    /**
     * Method to get user input of a menu option
     * @param scan Scanner object initialized to system.in
     * @param option int option chose for user interface
     * @return int menu option chosen by user
     */
    public static int menuOption(Scanner scan, int option){
        int choice = -1;//-1 if no choice made
        boolean go = false;//loop control boolean
        System.out.println();
        do{
            try{
                if(option == 1){
                    System.out.print("Enter a Choice (1-6):\t");
                }else if(option == 2){
                    System.out.print("Enter a Choice (1-2):\t");
                }else{
                    System.out.print("Enter a Choice (1-4):\t");
                }
                //scan.next();
                String tempChoiceString = scan.next();
                int tempChoice = Integer.parseInt(tempChoiceString);
                if(option == 1 && tempChoice >= 1 && tempChoice <= 7){
                    choice = tempChoice;
                    go = true;
                }else if(option == 2 && tempChoice >= 1 && tempChoice <= 2){
                    choice = tempChoice;
                    go = true;
                }else if(option == 3 && tempChoice >= 1 && tempChoice <= 4){
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
        //scan.close();
        return choice;
    }

    /**
     * Method to run specific option chosen by user (call associated function)
     * @param choice int menu option chosen by user
     * @param cid int customer id of the user, inputted after previous customer prompt
     * @param option int option of user interface chosen by user
     * @return String of query or update to be run in main method
     */
    public static String runOption(Scanner scan, int choice, int cid, int option){
        if(option == 1 && choice == 1){
            return checkIn(scan);
        }else if(option == 1 && choice == 2){
            return checkOut(scan);
        }else if(option == 1 && choice == 3){
            return cancellation(scan);
        }else if(option == 1 && choice == 4){
            return payment(scan, cid);
        }else if(option == 1 && choice == 5){
            seeAvailablity(scan);
        }else if(option == 3 &&  choice == 1){
            return makeReservation(scan, cid);
        }else if(option == 2 && choice == 1){
            return cleanRoom(scan);
        }else if(option == 3 && choice == 2){
            return payment(scan, cid);
        }else{
            seeAvailablity(scan);
        }
        return null;//FOR TEST
    }

    /**
     * Method to set a room to be clean in database by hoousekeeping personnel
     * @param scan Scanner object initialized to system.in
     * @return String of update to be run in main method
     */
    public static String cleanRoom(Scanner scan){
        int pid = pid(scan);
        int roomNum = roomNumber(scan, pid);
        System.out.println("\nRoom " + roomNum + " Updated as Clean\n");
        String q = "begin cleanRoom (" + pid + ", " + roomNum + "); end;";
        return q;
    }

    //NOTE: may go unused
    /**
     * Method to display desired business analytics by business/upper management
     */
    public static void businessAnalytics(){

    }

    /**
     * Method to display all the Hotel california properties
     * Used in seeAvailability method so user does not have to commit properties to memory
     */
    public static void displayProperties(){
        System.out.println("Properties:");
        System.out.println("--------------------");
        System.out.println("Property_ID:\t1\tCity:\tBradenton, FL");
        System.out.println("Property_ID:\t2\tCity:\tWillingboro, NJ");
        System.out.println("Property_ID:\t3\tCity:\tFayvill, NC");
        System.out.println("Property_ID:\t4\tCity:\tLagrange, GA");
        System.out.println("Property_ID:\t5\tCity:\tOntario, CA");
        System.out.println("Property_ID:\t6\tCity:\tKey West, FL");
        System.out.println("Property_ID:\t7\tCity:\tMillin, TN");
        System.out.println("Property_ID:\t8\tCity:\tEvanston, IL");
        System.out.println("Property_ID:\t9\tCity:\tMillvill, NJ");
        System.out.println("Property_ID:\t10\tCity:\tCoventry, RI");
        System.out.println("Property_ID:\t11\tCity:\tPowell, TN");
        System.out.println("Property_ID:\t12\tCity:\tWinder, GA");
        System.out.println("Property_ID:\t13\tCity:\tMacomb, MI");
        System.out.println("Property_ID:\t14\tCity:\tLacey, WA");
        System.out.println("Property_ID:\t15\tCity:\tAndover, MA");
        System.out.println("Property_ID:\t16\tCity:\tPensacola, FL");
        System.out.println("Property_ID:\t17\tCity:\tNatick, MA");
        System.out.println("Property_ID:\t18\tCity:\tOlney, MD");
        System.out.println("Property_ID:\t19\tCity:\tRoy, UT");
        System.out.println("Property_ID:\t20\tCity:\tTiffin, OH");
    }

    /**
     * Method to display the rooms in a property
     * @param pid int property to see rooms housed
     */
    public static void displayRooms(int pid){
        ResultSet r = null;
        try(Connection con=DriverManager.getConnection
		("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
         Statement s=con.createStatement();){
            r = s.executeQuery("select room_number, rt_id from room where p_id = " + pid);
            if(r.next()){
                System.out.println("\nProperty " + pid + " Room Numbers and Room Types:");
                System.out.println("--------------------");
                while(r.next()){
                    System.out.println(r.getString("room_number") + " - " + r.getString("rt_id"));
                }
            }else{
                throw new Exception();
            }
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to display the available room types for a property input by user 
     * @param pid int property id
     */
    public static void displayRoomTypesAvailable(int pid){
        ResultSet r = null;
        try(Connection con=DriverManager.getConnection
		("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
         Statement s=con.createStatement();){
            r = s.executeQuery("select distinct rt_id from room where (cleanoccupiedbool = 10 or cleanoccupiedbool = 0) and p_id = " + pid);
            if(r.next()){
                System.out.println("\nProperty " + pid + " Room Types Available:");
                System.out.println("--------------------");
                
                do{
                    if(r.getString("rt_id").equals("1")){
                        System.out.println("1 - Queen Bed");
                    }else if(r.getString("rt_id").equals("2")){
                        System.out.println("2 - King Bed (Comes With Pull Out Couch)");
                    }else if(r.getString("rt_id").equals("3")){
                        System.out.println("3 - Luxury Suit (Comes With Pull Out Couch)");
                    }else{
                        System.out.println("4 - Life In The Fast Lane Suite (Comes With Additional Closet Bed and Pull Out Couch)");
                    }
                }while(r.next());
                /*String num = r.getString("rt_id");
                if(num.equals("1")){
                    System.out.println("1 - Queen Bed");
                }else if(num.equals("2")){
                    System.out.println("2 - King Bed (Comes With Pull Out Couch)");
                }else if(num.equals("3")){
                    System.out.println("3 - Luxury Suit (Comes With Pull Out Couch)");
                }else{
                    System.out.println("4 - Life In The Fast Lane Suite (Comes With Additional Closet Bed and Pull Out Couch)");
                }*/
            }else{
                throw new Exception();
            }
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method to make a reservation 
     * @param scan Scanner object initialized to System.in
     * @param cid int customer id used for reservation
     * @return String of update to be run in main method
     */
    public static String makeReservation(Scanner scan, int cid){//need arguments
        boolean bigGo = false;
        int pid;
        int rtid;
        String startDate;
        String endDate;
        int numPeople;
        int dollarCost;
        int pointCost;
        do{
        //function to display properties and their ids for the clerks use
            pid = pid(scan);
            displayRoomTypesAvailable(pid);
            System.out.println();
            rtid = rtid(scan);
            startDate = date(scan, "Start Date");
            endDate = date(scan, "End Date");
            numPeople = numPeople(scan, rtid);
            dollarCost = dollarCostRes(startDate, endDate, rtid);
            pointCost = pointCostRes(startDate, endDate, rtid);
            System.out.println("\nProperty ID:\t" + pid);
            System.out.println("Room Type:\t" + rtid);
            System.out.println("Start Date:\t" + startDate);
            System.out.println("End Date:\t" + endDate);
            System.out.println("Number of People:\t" + numPeople);
            boolean go = false;
            do{
                System.out.print("Is this info correct?(y/n):\t");
                String response = scan.next();
                response = response.toLowerCase();
                if(response.equals("y")){
                    go = true;
                    bigGo = true;
                }else if(response.equals("n")){
                    go = true;
                }else{
                    System.out.println("\nInvalid Response, Try Again\n");
                    response = "";
                }
            }while(!go);
        }while(!bigGo);

        //print out stats before calling stored procedure and acquire confirmation
        

        //call stored procedure at the end
        String q = "begin makereservation(" + pid + ", " + rtid + ", '" + startDate + "', '" + endDate + "', " + numPeople + ", " + dollarCost + ", " + pointCost + ", " + cid + "); end;";
        //System.out.println(q);//for test
        return q;
    }

    /**
     * Method to calculate the dollar cost of a reservation
     * @param startDate String starting date of the reservation in date format
     * @param endDate String ending date of the reservation in date format
     * @param rtid int room type id, describes features of a reservation's room
     * @return int of dollar cost of reservation stay
     */
    public static Integer dollarCostRes(String startDate, String endDate, int rtid){
        //parse dates and find difference, multiply times current dollar rate, return
        int[] startArr = parseDate(startDate);
        int[] endArr = parseDate(endDate);
        int dol = 0;
        int days = 0;
        if(rtid == 1){
            dol = 120;
        }else if(rtid == 2){
            dol = 200;
        }else if(rtid == 3){
            dol = 400;
        }else{
            dol = 800;
        }
        if(startArr[2] == endArr[2] && startArr[0] == endArr[0]){//same year and month
            days = endArr[1] - startArr[1];
        }else if(startArr[2] == endArr[2] && endArr[0] > startArr[0]){//same year different month
            days = (31 - startArr[1]) + endArr[1] + 31*(endArr[0] - startArr[0] - 1);
        }else if(startArr[2] < endArr[2]){//different years
            days = (365*(endArr[2] - startArr[2])) + (31 - startArr[1]) + endArr[1] + 31*(endArr[0] - startArr[0] - 1);
        }else{
            //invalid date 
            System.out.println("\nInvalid Date Entered\n");
            return null;
        }
        return (days * dol);
    }

    /**
     * Method to calculate the point cost of a reservation
     * @param startDate String starting date of the reservation in date format
     * @param endDate String ending date of the reservation in date format
     * @param rtid int room type id, describes features of a reservation's room
     * @return int of point cost of reservation stay
     */
    public static Integer pointCostRes(String startDate, String endDate, int rtid){
        //parse dates and find difference, multiply times current point rate, and return
        int[] startArr = parseDate(startDate);
        int[] endArr = parseDate(endDate);
        int dol = 0;
        int days = 0;
        if(rtid == 1){
            dol = 5;
        }else if(rtid == 2){
            dol = 6;
        }else if(rtid == 3){
            dol = 8;
        }else{
            dol = 10;
        }
        if(startArr[2] == endArr[2] && startArr[0] == endArr[0]){//same year and month
            days = endArr[1] - startArr[1];
        }else if(startArr[2] == endArr[2] && endArr[0] > startArr[0]){//same year different month
            days = (31 - startArr[1]) + endArr[1] + 31*(endArr[0] - startArr[0] - 1);
        }else if(startArr[2] < endArr[2]){//different years
            days = (365*(endArr[2] - startArr[2])) + (31 - startArr[1]) + endArr[1] + 31*(endArr[0] - startArr[0] - 1);
        }else{
            //invalid date 
            System.out.println("\nInvalid Date Entered\n");
            return null;
        }
        return (days * dol);
    }

    /**
     * Method to determinee number of people described by user
     * @param scan Scanner object initalized to System.in
     * @param rtid int room type id
     * @return int number of pople staying for reservation
     */
    public static int numPeople(Scanner scan, int rtid){
        boolean go = false;
        int numPeople; 
        do{
            System.out.print("Enter Number of People Staying:\t");
            try{
                numPeople = Integer.parseInt(scan.next());
            }
            catch(Exception e){
                System.out.println("\nInvalid Number of People, Try Again\n");
                numPeople = 0;
                continue;
            }
            if(rtid == 1 && numPeople >= 1 && numPeople<=2){
                go = true;
            }else if(rtid == 2 && numPeople >= 1 && numPeople <= 4){
                go = true;
            }else if(rtid == 3 && numPeople >= 1 && numPeople <= 4){
                go = true;
            }else if(rtid == 4 && numPeople >= 1 && numPeople <= 6){
                go = true;
            }else{
                System.out.println("\nInvalid Number of People, Try Again\n");
                numPeople = 0;
            }
        }while(!go);
        return numPeople;
    }

    /**
     * Method to take in a date and verify its format
     * @param scan Scanner object initialized to System.in
     * @param dateType String description of date being prompted for
     * @return String valid date (in described date format)
     */
    public static String date(Scanner scan, String dateType){
        boolean go = false;
        String date = "";
        do{
            System.out.print("Enter " + dateType + "(mm-dd-yy):\t");
            date = scan.next();
            if(date.matches("(\\d{2}-\\d{2}-\\d{2})|(\\d{1}-\\d{2}-\\d{2})|(\\d{1}-\\d{1}-\\d{2})|(\\d{2}-\\d{1}-\\d{2})")){
                //check if valid date using parseDate function
                int[] checkDateArr = parseDate(date);
                if((checkDateArr[0]>=1 && checkDateArr[0]<= 12)&&(checkDateArr[1]>=1 && checkDateArr[1]<=31)&&(checkDateArr[2]>=23 && checkDateArr[2]<=99)){//note only taking reservations after 2024
                    go = true;
                }else{
                    System.out.println("\nInvalid Date, Try Again\n");
                    date = "";
                }
            }else{
                System.out.println("\nInvalid Date, Try Again\n");
                date = "";
            }
        }while(!go);
        return date;
    }

    /**
     * Method to parse a string date in the described date format
     * @param date String date in valid date format
     * @return integer array of size 3 holding int representations of mm, dd and yy
     */
    public static int[] parseDate(String date){
        int[] mmddyy = new int[3];
        try{
            String[] dateParts = date.split("-");
            mmddyy[0] = Integer.parseInt(dateParts[0]);
            mmddyy[1] = Integer.parseInt(dateParts[1]);
            mmddyy[2] = Integer.parseInt(dateParts[2]);
        }
        catch(Exception e){
            System.out.println("\nInvalid Date String\n");
        }
        return mmddyy;
    }

    /**
     * method to get the room type id number from user
     * @param scan Scanner object initialized to System.in
     * @return int room type id
     */
    public static int rtid(Scanner scan){
        boolean go = false;//loop control var
        int rtid;
        do{
            System.out.print("Enter Room Type ID:\t");
            try{
                rtid = Integer.parseInt(scan.next());
            }
            catch(Exception e){
                System.out.println("\nInvalid Room Type ID, Try Again\n");
                rtid = 0;
                continue;
            }
            if(rtid >= 1 && rtid <= 4){
                go = true;
            }else{
                System.out.println("\nInvalid Room Type ID, Try Again\n");
                rtid = 0;
            }
        }while(!go);
        return rtid;
    }

    /**
     * method to get the property id number from user
     * @param scan Scanner object initialized to System.in
     * @return int property id
     */
    public static int pid(Scanner scan){
        displayProperties();
        boolean go = false;//loop contorl var
        int pid;
        do{
            System.out.print("Enter Property ID:\t");
            try{
                pid = Integer.parseInt(scan.next());
            }
            catch(Exception e){
                System.out.println("\nInvalid Property ID, Try Again\n");
                pid = 0;
                continue;
            }
            if(pid >= 1 && pid <= 20){
                go = true;
            }else{
                System.out.println("\nInvalid Property ID, Try Again\n");
                pid = 0;
            }
        }while(!go);
        return pid;
    }

    /**
     * Method to check in a customer to hotel
     * @param scan Scanner object initialized to system.in
     * @return String update to be run in main method
     */
    public static String checkIn(Scanner scan){//need argument
        boolean bigGo = false;
        int resid;
        int pid;
        String date;
        do{
            resid = resid(scan);
            pid = pid(scan);
            date = date(scan, "the Date of Reservation");
            if(!(date.equals(currentDate))){//invalid checkin date
                return null;
            }
            boolean go = false;
            do{
                try{
                    System.out.println("\nReservation ID:\t" + resid);
                    System.out.println("Property ID:\t" + pid);
                    System.out.println("Date of Reservation:\t" + date + "\n");
                    System.out.print("Is this correct? (y/n):\t");
                    String response = scan.next();
                    response = response.toLowerCase();
                    if(response.equals("y")){
                        go = true;
                        bigGo = true;
                    }else if(response.equals("n")){
                        go = true;
                    }else{
                        throw new Exception("Invalid");
                    }
                }
                catch(Exception e){
                    System.out.println("\nInvalid Input, Try Again\n");
                }
            }while(!go);
            //figure out room number in stored procedure
        }while(!bigGo);
        String q = "begin makeCheckIn (" + resid + ", " + pid + ", " + date + "); end;";
        return q;
    }

    /**
     * method to get the reservation id number from user
     * @param scan Scanner object initialized to System.in
     * @return int reservation id
     */
    public static int resid(Scanner scan){
        boolean go = false;
        int resid = 0;
        do{
            try{
                System.out.print("Input Reservation ID:\t");
                resid = Integer.parseInt(scan.next());
                if(resid > 0){
                    go = true;
                }else{
                    throw new Exception("invalid");
                }
            }
            catch(Exception e){
                System.out.println("\nInvalid Reservation ID, Try Again");
            }
        }while(!go);
        return resid;
    }

    /**
     * Method to check out a customer to hotel
     * @param scan Scanner object initialized to system.in
     * @return String update to be run in main method
     */
    public static String checkOut(Scanner scan){//need arguments
        boolean bigGo = false;
        int resid;
        int pid;
        String date;
        int roomNum;
        do{
            resid = resid(scan);
            pid = pid(scan);
            date = date(scan, "Today's Date");
            if(!date.equals(currentDate)){
                return null;
            }
            roomNum = roomNumber(scan, pid);
            boolean go = false;
            do{
                try{
                    System.out.println("\nReservation ID:\t" + resid);
                    System.out.println("Property ID:\t" + pid);
                    System.out.println("Date of Check Out:\t" + date);
                    System.out.println("Room Number:\t" + roomNum);
                    System.out.print("\nIs this correct? (y/n):\t");
                    String response = scan.next();
                    response = response.toLowerCase();
                    if(response.equals("y")){
                        go = true;
                        bigGo = true;
                    }else if(response.equals("n")){
                        go = true;
                    }else{
                        throw new Exception("Invalid");
                    }
                }
                catch(Exception e){
                    System.out.println("\nInvalid Input, Try Again\n");
                }
            }while(!go);
        }while(!bigGo);
        String q = "begin makeCheckOut (" + resid + ", " + pid + ", " + date + ", " + roomNum +"); end;";
        return q;
    }

    /**
     * Method to cancel a customer to hotel
     * @param scan Scanner object initialized to system.in
     * @return String update to be run in main method
     */
    public static String cancellation(Scanner scan){//need argument
        boolean bigGo = false;
        int resid;
        int pid;
        String date;
        int roomNum;
        do{
            resid = resid(scan);
            pid = pid(scan);
            date = date(scan, "the Date of Reservation");
            roomNum = roomNumber(scan, pid);
            boolean go = false;
            do{
                try{
                    System.out.println("\nReservation ID:\t" + resid);
                    System.out.println("Property ID:\t" + pid);
                    System.out.println("Date of Reservation:\t" + date);
                    System.out.println("Room Number:\t" + roomNum);
                    System.out.print("\nIs this correct? (y/n):\t");
                    String response = scan.next();
                    response = response.toLowerCase();
                    if(response.equals("y")){
                        go = true;
                        bigGo = true;
                    }else if(response.equals("n")){
                        go = true;
                    }else{
                        throw new Exception("Invalid");
                    }
                }
                catch(Exception e){
                    System.out.println("\nInvalid Input, Try Again\n");
                }
            }while(!go);
        }while(!bigGo);
        String q = "begin makeCancel (" + resid + ", " + pid + ", " + date + ", " + roomNum + "); end;";
        return q;
    }

    /**
     * Method to have customer pay for their stay at hotel
     * @param scan Scanner object initialized to System.in
     * @param cid int customer id number
     * @return String update to be run in main method
     */
    public static String payment(Scanner scan, int cid){//need arguments
        boolean bigGo = false;
        int pid;
        String date;
        int roomNum;
        do{
            boolean go = false;
            pid = pid(scan);
            date = date(scan, "Today's Date");
            if(!(date.equals(currentDate))){
                return null;
            }
            roomNum = roomNumber(scan, pid);
            do{
                try{
                    System.out.println("\nProperty ID:\t" + pid);
                    System.out.println("Date of Transaction:\t" + date);
                    System.out.println("Customer ID:\t" + cid);
                    System.out.println("Room Number:\t" + roomNum);
                    System.out.print("\nIs this info correct? (y/n):\t");
                    String response = scan.next();
                    response = response.toLowerCase();
                    if(response.equals("y")){
                        go = true;
                        bigGo = true;
                    }else if(response.equals("n")){
                        go = true;
                    }else{
                        throw new Exception("Invalid");
                    }
                }
                catch(Exception e){
                    System.out.println("\nInvalid Input, Try Again\n");
                }
            }while(!go);
        }while(!bigGo);
        String q = "begin makeTx (" + pid + ", " + date + ", " + cid + ", " + roomNum + "); end;";
        return q;
    }

    
    /**
     * Method to see availability of a room at a specific property
     * @param scan Scanner object initialized to System.in
     */
    public static void seeAvailablity(Scanner scan){
        boolean bigGo = false;
        int pid;
        int roomNumber;
        do{
            boolean go = false;
            pid = pid(scan);
            System.out.println();
            displayRoomTypesAvailable(pid);
            System.out.println();
            displayRooms(pid);
            roomNumber = roomNumber(scan, pid);//need to do query for this function?
            //IMPLEMENT:    query to see if room is occupied
            System.out.println("\nProperty ID:\t" + pid);
            System.out.println("Room Number:\t" + roomNumber);
            do{
                System.out.print("Is this info correct? (y/n):\t");
                String response = scan.next();
                response = response.toLowerCase();
                if(response.equals("y")){
                    go = true;
                    bigGo = true;
                }else if(response.equals("n")){
                    go = true;
                }else{
                    System.out.println("\nInvalid Response, Try Again\n");
                    response = "";
                }
            }while(!go);
        }while(!bigGo);

        //NEED TO WRITE A STRING TO CALL PROCEDURE IN MAIN METHOD TO SEE IF ROOM NUMBER AT PROPERTY IS AVAILABLE
        try (
         Connection con=DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
         Statement s=con.createStatement();
        ){
            ResultSet result;
            String q = "select cleanoccupiedbool from room where p_id = " + pid + " and room_number = " + roomNumber;
            result = s.executeQuery(q);
            if(!result.next()){
                System.out.println("Invalid Room for Property");
            }else{
                int bool = result.getInt("cleanoccupiedbool");
                if(bool == 10 || bool == 0){
                    System.out.println("\nRoom " + roomNumber + " is Available\n");
                }else{
                    System.out.println("\nRoom " + roomNumber + " is NOT Available\n");
                }
            }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    /**
     * Method to get a room number from user
     * @param scan Scanner object initalized to System.in
     * @param pid int property id number
     * @return int room number inputted
     */
    public static int roomNumber(Scanner scan, int pid){
        boolean go = false;
        int num = 0;
        do{
            System.out.print("Enter Room Number:\t");
            try{
                num = Integer.parseInt(scan.next());
            }
            catch(Exception e){
                System.out.println("\nInvalid Input, Try Again\n");
                continue;
            }
            boolean exists = true;//IMPLEMENT:     query to check if room number exists in pid or say in readme that the room number will always exist because we only deal with perfectly honest and remembering customers
            if(exists){
                go = true;
            }else{
                System.out.println("\nInvalid Room Number, Try Again\n");
                num = 0;
            }
        }while(!go);
        return num;
    }

    /**
     * Method to prompt user if thee current customer being interacted with is a past customer
     * @param scan Scanner object initialized to system.in
     * @return Array of string obejcts containing codes to be parsed in other methods (main method right now)
     */
    //function to see if someone is a previous customer returns cid NOTE: maybe run this before run option and put cid as arguments in subsequent functions
    //if returned cid is -1 need new cid
    public static String[] customer(Scanner scan, int option){
        boolean go = false;
        //int cid = -1;
        String[] retArr = {" ", " "};
        do{
            try{
                if(option == 3){
                    System.out.print("Past Customer? (y/n):\t");
                }else{
                    System.out.print("Is Client a Past Customer? (y/n):\t");
                }
                String response1 = scan.next();
                String response2;
                String response3;
                response1 = response1.toLowerCase();
                if(response1.equals("y")){
                    System.out.print("\nInput Customer ID:\t");
                    response2 = ((Integer)(Integer.parseInt(scan.next()))).toString();
                    System.out.print("\nInput Customer Name:\t");
                    response3 = scan.next();
                    retArr[0] = response2;//String version of cid
                    retArr[1] = response3;
                    go = true;
                }else if(response1.equals("n")){
                    retArr[0] = "-1";
                    retArr[1] = "-1";
                    go = true;
                }else if(response1.equals("test")){
                    go = true;
                    retArr[0] = "-23";
                }else{
                    System.out.println("\nInvalid Response, Try Again\n");
                }
            }
            catch(Exception e){
                System.out.println("\nInvalid Response, Try Again\n");
            }
        }while(!go);
        return retArr;
    }

    /**
     * Method to parse a string customer id number
     * @param cid String customer id number
     * @return int customer id number
     */
    public static int parseCid(String cid){
        int retCid = Integer.parseInt(cid);
        return retCid;
    }

    /**
     * Method to construct string update to add a new customer
     * @param scan Scanner object initialized to system.in
     * @param cid int customer id number
     * @param name String name of customer
     * @return String update to be run in main method
     */
    public static String newCustomer(Scanner scan, int cid, String name){
        String date = currentDate;
        String addy = address(scan);
        //String q = null;//POOP
        String q = "insert into customer values (" + cid + ", '" + date + "', '" + addy + "', '" + name + "')";
        return q;
    }

    /**
     * Method to get a valid address from a user
     * @param scan Scanner object initalized to System.in
     * @return string address inputted by user
     */
    public static String address(Scanner scan){
        boolean go = false;
        String addy = "";
        do{
            try{
                //scan.nextLine();//buffer clear
                System.out.print("Enter Customer Address:\t");
                addy = scan.nextLine();
                go = true;
            }
            catch(Exception e){
                System.out.println("\nInvalid Input, Try Again\n");
            }
        }while(!go);
        return addy;
    }

    /**
     * Method to get valid name input by user
     * @param scan Sccanner initilialized to System.in
     * @return String name input by user
     */
    public static String name(Scanner scan){
        String name = "";
        boolean go = false;
        do{
            try{
                scan.nextLine();
                System.out.print("Enter Customer Name:\t");
                name = scan.nextLine();
                //System.out.println(name);
                if(name.length() > 30){
                    throw new Exception("Name too long");
                }
                go = true;
            }
            catch(Exception e){
                System.out.println("\nInvalid Input, Try Again\n");
            }
        }while(!go);
        return name;
    }
}
