import java.util.*;
public class Tester {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        String a = UserInterface.name(scan);
        System.out.println("\n" + a + "\n");
        scan.close();
    }
}
