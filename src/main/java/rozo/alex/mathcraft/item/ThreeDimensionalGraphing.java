package rozo.alex.mathcraft.item;

import java.util.*;

/**
 * This class takes a range of x and y
 * and generate a set of x,y,z (in integers) coordinate
 * the calculation is discrete
 * the step is fixed at 1
 * x and y values are all integers
 */
public class ThreeDimensionalGraphing {
    private int[] ranges;
    //upper and lower bound of x and y
    private String infixOperations;
    //just for record
    private String[] postfixOperations;
    //The operations in post fix order
    private boolean is3D;
    //do we have 2 parameters or one
    private Queue<Integer> Z_values;

    private HashSet<Character> legalChars;

    private final String[] longOps={"sin","cos","log","ln"};


    //private final String discription="The class takes 5 parameters. The first is a function of x and y.\nThe second and third are the lower and upper limits of x.\nThe 4th and 5th are the lower and upper limit of y.\nThe class will generate a list of x,y,z values from the function. ";

    private double  approximateLimitOfUndefine=0.001;




    public ThreeDimensionalGraphing(String operations){
        this(operations,-10,10,-10,10);
        //default interval

    }

    public ThreeDimensionalGraphing(String operations,double x_lower,double x_upper,double y_lower,double y_upper){
        this.legalChars=setLegalChars();
        operations=simpleStringPolish(operations);
        this.postfixOperations=makePostfixOperation(operations);
        //debug
        if(postfixOperations==null){
            System.out.println("NULL NULL NULL NULL NULL NULL NULL NULL NULL NULL");
        }else {
            String s = "";
            for (int i = 0; i < postfixOperations.length; i++) {
                s = s + postfixOperations[i];
            }
            System.out.println(s);
        }
        //debug
        this.ranges = new int[4];
        if(x_lower>x_upper){
            double temp=x_lower;
            x_lower=x_upper;
            x_upper=temp;
        }
        if(y_lower>y_upper){
            double temp=y_lower;
            y_lower=y_upper;
            y_upper=temp;
        }
        ranges[0] = (int)Math.ceil(x_lower);
        ranges[1] = (int)x_upper;
        ranges[2] = (int)Math.ceil(y_lower);
        ranges[3] = (int)y_upper;
        is3D=true;
        Z_values=generateZ();



    }


    //2d version, not often used
    public ThreeDimensionalGraphing(String operations,double t_start,double t_end){
        legalChars=setLegalChars();
        operations=simpleStringPolish(operations);
        postfixOperations=makePostfixOperation(operations);
        //debug
        if(postfixOperations==null){
            System.out.println("NULL NULL NULL NULL NULL NULL NULL NULL NULL NULL");
        }else {
            String s = "";
            for (int i = 0; i < postfixOperations.length; i++) {
                s = s + postfixOperations[i];
            }
            System.out.println(s);
        }
        //debug
        ranges = new int[2];
        if(t_start>t_end){
            double temp=t_start;
            t_start=t_end;
            t_end=temp;
        }
        ranges[0] = (int)Math.ceil(t_start);
        ranges[1] = (int)t_end;
        is3D=false;
        Z_values=generateZ();
    }



    //this method clears all spaces and converts everything to lower cases
    //and assign field infixOperation to the polished operation
    private String simpleStringPolish(String operations) {
        if (operations==null){
            if(is3D){
            System.out.println("operationNullError");
            }//null inp is only an error if it is not parametric
            return null;
        }//if null, return null

        operations=operations.replaceAll(" ","");
        //get rid of spaces

        operations=operations.toLowerCase();
        //not case sensitive

        this.infixOperations=operations;
        //store the polished operation

        return operations;
    }




    //This method add all legal characters in to a hash set
    private HashSet<Character> setLegalChars() {
        HashSet<Character> legalchars= new HashSet<Character>();
        legalchars.add('x');
        legalchars.add('y');
        legalchars.add('z');
        legalchars.add('e');
        legalchars.add('l');
        legalchars.add('c');
        legalchars.add('o');
        legalchars.add('s');
        legalchars.add('i');
        legalchars.add('t');
        legalchars.add('n');
        legalchars.add('+');
        legalchars.add('-');
        legalchars.add('*');
        legalchars.add('/');
        legalchars.add('^');
        legalchars.add('(');
        legalchars.add(')');
        legalchars.add('.');
        legalchars.add('9');
        legalchars.add('0');
        legalchars.add('8');
        legalchars.add('7');
        legalchars.add('6');
        legalchars.add('5');
        legalchars.add('4');
        legalchars.add('3');
        legalchars.add('2');
        legalchars.add('1');
        return legalchars;
    }



    //The expression of function is organized in the method
    //some checks for illegal expression are done
    //if there is an illegal expression, the method will return null, and PRINT the error.
    //no exceptions will be thrown
    private String[] makePostfixOperation(String operations) {

        if (operations==null){
            System.out.println("nullInputError");
            return null;
        }//stop if inp is null

        //handle numbers with more than 2 digits
        Stack<String> tempCh = new Stack<String>();
        int operationCounter =0; //count number of operations
        for(int i=0;i<operations.length();i++){
            char ch=operations.charAt(i);
            if(containsIllegalChars(ch)){
                System.out.println("containsIllegalCharsError at i= "+i+"\nIllegal Char: "+ch);
                return null;
            }//check if there is any illegal chars
            if((ch=='.')||(isNumeric(String.valueOf(ch)) && i!=0)){
                //if the next char is a number and it is not the first one
                //if((isLegalParameter(tempCh.peek()))&&isNumeric(String.valueOf(ch))){
                if((isLegalParameter(tempCh.peek()))||isLegalLongOperation(tempCh.peek())&&isNumeric(String.valueOf(ch))){
                    tempCh.push("*");
                    //handle the case i.e. x6+y5
                    //make it x*6+y*5
                }
                if((ch=='.')||isNumeric(tempCh.peek())) {
                    //if the previous one is a number,
                    // pop it and add the new number to the string
                    tempCh.push(tempCh.pop() + ch);
                    if(!isNumeric(tempCh.peek())){
                        System.out.println("isNumericError at i= "+i);
                        return null;
                    }
                    //return null if we get sth like 6.7.
                }else{
                    tempCh.push(String.valueOf(ch));
                    //if the previous one is not a number
                    //just push the new char

                }
                operationCounter=0;
            }else {
                if(!tempCh.isEmpty()){
                    if(isLegalParameter(String.valueOf(ch))&&isNumeric(tempCh.peek())){
                        tempCh.push("*");
                        //handle the case i.e. 6x+5y
                        //make it 6*x+5*y
                    }
                }

                int temp_i=replaceLongOperations(ch,tempCh,operations,i);
                if(temp_i==-1){
                    return null;
                }//handle cos sin log ln

                if(ch == '-' && (isNumeric(tempCh.peek())||isLegalParameter(String.valueOf(tempCh.peek())))){
                    tempCh.push("-");
                }else if(ch=='-') {
                    tempCh.push("0");
                    tempCh.push("-");
                }else{
                    if(i==temp_i &&(isNumeric(String.valueOf(ch))||isShortOperation(ch)||isLegalParameter(String.valueOf(ch)))) {
                        tempCh.push(String.valueOf(ch));
                    }else{
                        i=temp_i;
                    }
                    //handle minus sign
                    //if not a number, or it's the first one
                    //push it
                }




                if(i!=0){
                    operationCounter++;
                }
                if(ch=='(' ||ch==')'||isLegalParameter(String.valueOf(ch))){
                    operationCounter=0;
                }
                if((!tempCh.isEmpty())&&isLegalLongOperation(tempCh.peek())){
                    operationCounter=0;
                }

            }
            if(operationCounter>=2){
                System.out.println("excessiveOperationsError at i= "+i);
                return null;
                //if two out more operations are in a row
                //the expression is wrong
                //return null
            }
        }
        //handle numbers with more than 2 digits


        return toPostFix(tempCh);

    }



    //find out if the given char is the first char of a long operation
    //push the long operation to the stack
    //return the index of the last char in the long operation in the String operation
    //-1 means this is an error
    private int replaceLongOperations(char ch, Stack<String> tempCh, String operations, int i) {
        if(!(ch=='c'||ch=='s'||ch=='l')){
            return i;
        }
        else if(operations.length()<=(i+4)){
            System.out.println("wrongLongOperationError at i = "+i);
            tempCh=null;
            return -1;
        }else {
            String twochars = operations.substring(i,i+2) ;
            if(twochars.equals("ln")){
                if(!tempCh.isEmpty()){
                    if(isLegalParameter(tempCh.peek())||isNumeric(tempCh.peek())){
                        tempCh.push("*");
                        //handle the case i.e. 6x+5y
                        //make it 6*x+5*y
                    }
                }
                tempCh.push("ln");
                return i+1;
            }else{
                String threechars = operations.substring(i,i+3) ;
                if(threechars.equals("cos")){
                    if(!tempCh.isEmpty()){
                        if(isLegalParameter(tempCh.peek())||isNumeric(tempCh.peek())){
                            tempCh.push("*");
                            //handle the case i.e. 6x+5y
                            //make it 6*x+5*y
                        }
                    }
                    tempCh.push("cos");
                    return i+2;
                }else if(threechars.equals("sin")){
                    if(!tempCh.isEmpty()){
                        if(isLegalParameter(tempCh.peek())||isNumeric(tempCh.peek())){
                            tempCh.push("*");
                            //handle the case i.e. 6x+5y
                            //make it 6*x+5*y
                        }
                    }
                    tempCh.push("sin");
                    return i+2;
                } else if(threechars.equals("log")){
                    if(!tempCh.isEmpty()){
                        if(isLegalParameter(tempCh.peek())||isNumeric(tempCh.peek())){
                            tempCh.push("*");
                            //handle the case i.e. 6x+5y
                            //make it 6*x+5*y
                        }
                    }
                    tempCh.push("log");
                    return i+2;
                } else
                    System.out.println("wrongLongOperationError at i = "+i);
                    System.out.println("The Char is "+threechars);
                    return -1;
            }
        }
    }




    //The expression is converted to post fix in this method
    private String[] toPostFix(Stack<String> tempCh) {
        Stack<String> reverseTempCh=new Stack<String>();
        Stack<String> operations=new Stack<String>();
        while(!tempCh.isEmpty()){
            reverseTempCh.push(tempCh.pop());
        }
        tempCh.clear();
        int numberOfLeftBracket=0;
        int numberOfLongOperationBracket=0;
        while(!reverseTempCh.isEmpty()){
            String temp=reverseTempCh.pop();
            //temp is the current element
            //can be x y e
            //and all allowed operations
            if(isNumeric(temp)||isLegalParameter(temp)){
                tempCh.push(temp);
            }else if(temp.equals("(")){
                numberOfLeftBracket++;//count the number of left bracket
            }else if(temp.equals(")")){
                while (!operations.isEmpty()){
                    tempCh.push(operations.pop());
                }
            }else if(isPriorityOperation(temp)||isLegalLongOperation(temp)){
                    operations.push(temp);
            }else if(numberOfLeftBracket!=0){
                operations.push(temp);
                numberOfLeftBracket--;
            }
            else{
                if(operations.isEmpty()){
                    operations.push(temp);
                }else {
                    while (!operations.isEmpty()) {
                    //while ((!operations.isEmpty())&&isPriorityOperation(operations.peek())) {
                        tempCh.push(operations.pop());
                    }

                    operations.push(temp);
                }
            }
        }
        while(!operations.isEmpty()){
            tempCh.push(operations.pop());
        }
        String[] postfixOperationsArray=new String[tempCh.size()];
        for(int i=tempCh.size()-1;i>=0;i--){
            postfixOperationsArray[i]=tempCh.pop();
        }
        return postfixOperationsArray;
    }







    //helper methods



    //determine if the given string is a legal operation that contains 2 or 3 characters
    private boolean isLegalLongOperation(String peek) {
        return (peek.equals("cos")||peek.equals("sin")||peek.equals("ln")||peek.equals("log"));
    }

    //determine if the given string is a legal operation that contains only one character
    private boolean isShortOperation(char ch) {
        return (ch=='-'||ch=='+'||ch=='/'||ch=='*'||ch=='^'||ch=='('||ch==')');
    }

    //see if the given string is a operation that requires priority
    private boolean isPriorityOperation(String temp) {
        return (temp.equals("*")||temp.equals("/")||temp.equals("^"));
    }

    //see if the given string a legal parameter
    private boolean isLegalParameter(String temp){
        return (temp.equals("x")||temp.equals("y")||temp.equals("e")||temp.equals("t"));
    }




    //see if the given char a legal char
    private boolean containsIllegalChars(char ch) {
        return (!legalChars.contains(ch));
    }

    //check if a string can be a double
    private boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }






    //Do the calculation
    private Queue<Integer> generateZ(){
        if(this.postfixOperations==null){
            return null;
        }
        if(is3D){
            return generateZ_XY();
        }else {
            return generateZ_X();
        }
    }

    //3d situation
    private Queue<Integer> generateZ_XY(){
        Queue<Integer> zxy =new LinkedList<Integer>();
        Stack<Character> operationStack=new Stack<Character>();
        for (int x=ranges[0];x<=ranges[1];x++){
            for(int y=ranges[2];y<=ranges[3];y++){
                zxy.add(functionZ(x,y));
                zxy.add(x);
                zxy.add(y);
            }
        }
        return zxy;

    }

    //2d situation
    private Queue<Integer>  generateZ_X(){
        Queue<Integer> zt =new LinkedList<Integer>();
        Stack<String> operationStack=new Stack<String>();
        for (int x=ranges[0];x<=ranges[1];x++){
            zt.add(functionZ(x,0));
            zt.add(x);
        }
        return zt;
    }

    //The z(x,y)
    //returns a value of z with given x and y
    private Integer functionZ(int x, int y) {
        Stack<String> tempCalculation=new Stack<String>();
        for(int i=0;i<postfixOperations.length;i++){
            if(isNumeric(postfixOperations[i])){
                tempCalculation.push(postfixOperations[i]);
            }else if(postfixOperations[i].equals("x")||postfixOperations[i].equals("t")){
                tempCalculation.push(String.valueOf(x));
                //replace letter with number
            }else if(postfixOperations[i].equals("y")){
                tempCalculation.push(String.valueOf(y));
                //replace letter with number
            }else if(postfixOperations[i].equals("e")){
                tempCalculation.push("2.71828");
                //Euler's number
            }
            else {
                double Second = Double.parseDouble(tempCalculation.pop());
                if (postfixOperations[i].equals("sin")) {
                    tempCalculation.push(Double.toString(Math.sin(Second)));
                } else if (postfixOperations[i].equals("cos")) {
                    tempCalculation.push(Double.toString(Math.cos(Second)));
                } else if (postfixOperations[i].equals("log")) {
                    if (Second == 0) {
                        Second = Second + approximateLimitOfUndefine;
                        //approximate to handle undefined situations
                    }
                    tempCalculation.push(Double.toString(Math.log10(Second)));
                } else if (postfixOperations[i].equals("ln")) {
                    if (Second == 0) {
                        Second = Second + approximateLimitOfUndefine;
                        //approximate to handle undefined situations
                    }
                    tempCalculation.push(Double.toString(Math.log(Second)));
                } else{
                    double first = Double.parseDouble(tempCalculation.pop());
                    switch (postfixOperations[i].charAt(0)) {
                        case '+':
                            tempCalculation.push(Double.toString(Second + first));
                            break;
                        case '-':
                            tempCalculation.push(Double.toString(first - Second));
                            break;
                        case '*':
                            tempCalculation.push(Double.toString(Second * first));
                            break;
                        case '/':
                            if (Second == 0) {
                                Second = Second + approximateLimitOfUndefine;
                                //approximate to handle undefined situations
                            }
                            tempCalculation.push(Double.toString(first / Second));
                            break;
                        case '^':
                            tempCalculation.push(Double.toString(Math.pow(first, Second)));
                            break;
                    }
                }




            }
        }
        return (int)Math.round(Double.parseDouble(tempCalculation.pop()));
    }



    public String toString(){
        Queue<Integer> copyZ_values=new LinkedList<Integer>();
        for(int i=0;i<Z_values.size();i++){
            int temp=Z_values.poll();
            Z_values.add(temp);
            copyZ_values.add(temp);
        }
        if(postfixOperations==null){
            return "There is an error in the expression of the function.";
        }
        String toStringZ;
        if(is3D) {
            toStringZ = "z(x,y) = " + this.infixOperations + "\n(z,x,y) =";
            int counter=0;
            while (!copyZ_values.isEmpty()){
                if(counter%5==0){
                    toStringZ=toStringZ+"\n";
                }
                counter++;
                toStringZ=toStringZ+" ("+copyZ_values.remove()+","+copyZ_values.remove()+","+copyZ_values.remove()+"),";

            }
        }else{
            toStringZ = "z(x) = " + this.infixOperations + "\n(z,x) =";
            int counter=0;
            while (!copyZ_values.isEmpty()){
                if(counter%7==0){
                    toStringZ=toStringZ+"\n";
                }
                counter++;
                toStringZ=toStringZ+" ("+copyZ_values.remove()+","+copyZ_values.remove()+"),";

            }
        }
        return toStringZ;
    }

    public void setApproximateLimitOfUndefine(double lim){
        if (lim>1){
            approximateLimitOfUndefine=0.001;
            System.out.println("Enter value too large set to 0.001");
        }else{
            approximateLimitOfUndefine=lim;
        }
    }

    public double getApproximateLimitOfUndefine(){return approximateLimitOfUndefine;}

    public String[] getPostfixOperations(){return postfixOperations;}

    public String getInfixOperations(){return infixOperations;}

    public Queue<Integer> getResults(){return Z_values;}

    public int getDimension(){
        if(is3D){
            return 3;
        }else {return 2;}
    }





}
