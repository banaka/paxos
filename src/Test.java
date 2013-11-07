
public class Test {

    static public void printBraces(int no, int toBeClosed, String str){
        if (no < 0 || toBeClosed < 0)
            return ;

        if (no == 0 && toBeClosed == 0 ) {
            System.out.println(str);
            return ;
        }
        printBraces ( no - 1, toBeClosed + 1, str + "(");
        printBraces ( no , toBeClosed - 1 , str + ")");
     return ;
    }

    public static void main(String[] args){
        printBraces(3,0,"");
    }
}
