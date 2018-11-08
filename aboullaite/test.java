package aboullaite;

import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        Scanner s=new Scanner(System.in);
        System.out.printf("Please enter an integer\n");
        while(!s.hasNextInt()){
            System.out.printf("you entered %s, please enter an integer",s.next());
        }
        //int a=s.nextInt();
        System.out.printf("congratulations, you entered %d",s.nextInt());
    }
}
