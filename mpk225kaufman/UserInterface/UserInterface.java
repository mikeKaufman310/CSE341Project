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

        
        try(Connection con = DriverManager.getConnection("jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241",userName,password);
            Statement s = con.createStatement();){
                String[] cidAndName = customer(scan);//to be parsed
                int cid = parseCid(cidAndName[0]);
                String customerName = cidAndName[1];
                if(cid == -1){//need a new customer id
                    //get new value for cid
                    System.out.println("flag");//for test
                    ResultSet maxCid = s.executeQuery("select max(c_id) from customer;");
                    System.out.println("flag");//for test
                    cid = Integer.parseInt(maxCid.getString("max(c_id)"));
                    String customerInsert = newCustomer(scan, cid, customerName);
                    s.executeUpdate(customerInsert);    
                }
                displayMenu();
                int choice = menuOption(scan);
                String q = runOption(choice, cid);
                s.executeUpdate(q);
        }
        catch(Exception e){
            System.out.println(e);      
        }
        scan.close();
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

    public static String[] parseDateStringArr(String date){
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
        //scan.close();
        return choice;
    }

    public static String runOption(int choice, int cid){
        if(choice == 1){
            return makeReservation(scan, cid);
        }else if(choice == 2){
            checkIn();
        }else if(choice == 3){
            checkOut();
        }else if(choice == 4){
            cancellation();
        }else if(choice == 5){
            payment();
        }else{
            seeAvailablity(scan);
        }
        return null;//FOR TEST
    }

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
            //function to display room types for the clerks use
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

    public static String date(Scanner scan, String dateType){
        boolean go = false;
        String date = "";
        do{
            System.out.print("Enter " + dateType + "(mm-dd-yy):\t");
            date = scan.next();
            if(date.matches("(\\d{2}-\\d{2}-\\d{2})|(\\d{1}-\\d{2}-\\d{2})|(\\d{1}-\\d{1}-\\d{2})|(\\d{2}-\\d{1}-\\d{2})")){
                //check if valid date using parseDate function
                int[] checkDateArr = parseDate(date);
                if((checkDateArr[0]>=1 && checkDateArr[0]<= 12)&&(checkDateArr[1]>=1 && checkDateArr[1]<=31)&&(checkDateArr[2]>=24 && checkDateArr[2]<=99)){//note only taking reservations after 2024
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


    public static int pid(Scanner scan){
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

    public static void checkIn(){//need argument
        
    }

    public static void checkOut(){//need arguments
    
    }

    public static void cancellation(){//need argument
    
    }

    public static void payment(){//need arguments
    
    }

    public static void seeAvailablity(Scanner scan){
        boolean bigGo = false;
        do{
            boolean go = false;
            int pid = pid(scan);
            int roomNumber = roomNumber(scan, pid);//need to do query for this function?
            //IMPLEMENT:    query to see if room is occupied
            System.out.println("\nProperty ID:\t" + pid);
            System.out.println("Room Number:\t" + roomNumber);
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
    }

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
            boolean exists = false;//IMPLEMENT:     query to check if room number exists in pid
            if(exists){
                go = true;
            }else{
                System.out.println("\nInvalid Room Number, Try Again\n");
                num = 0;
            }
        }while(!go);
        return num;
    }

    //function to see if someone is a previous customer returns cid NOTE: maybe run this before run option and put cid as arguments in subsequent functions
    //if returned cid is -1 need new cid
    public static String[] customer(Scanner scan){
        boolean go = false;
        int cid = -1;
        String[] retArr = {" ", " "};
        do{
            try{
                System.out.print("Past Customer? (y/n)\t");
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

    public static int parseCid(String cid){
        int retCid = Integer.parseInt(cid);
        return retCid;
    }

    public static String newCustomer(Scanner scan, int cid, String name){
        String date = date(scan, "Today's Date");
        String addy = address(scan);
        String q = "insert into customer values (" + cid + ", '" + date + "', '" + addy + "', '" + name + "');";
        return q;
    }

    public static String address(Scanner scan){
        boolean go = false;
        String addy = "";
        do{
            try{
                System.out.print("Enter Customer Address:\t");
                addy = scan.nextLine();
            }
            catch(Exception e){
                System.out.println("\nInvalid Input, Try Again\n");
            }
        }while(!go);
        return addy;
    }
}
