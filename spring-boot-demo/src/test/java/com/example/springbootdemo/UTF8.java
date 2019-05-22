package com.example.springbootdemo;

public class UTF8 {
    public boolean validUtf8(int[] data) {
        boolean res= true;
        for (int i = 0; i < data.length; i++) {
            char[] chars = Integer.toBinaryString(data[i]).toCharArray(); //int/char转二进制表示
            double size = chars.length/8.0;
            if (size==1 && chars[0]==0){

            }else if (size == 2 && check0(chars,2)){

            }else if (size == 3){

            }else if (size == 4){

            }else {
                res=false;
            }
        }
        return res;
    }

    private boolean check0(char[] data,int n){
        if (n==1&&data[0]!=0){
            return false;
        }
        if (data[n]!=0||data[2*8-6-1]!=0){
            return false;
        }
        return true;
    }

    private boolean check1(char[] data,int n){
        if (n==1&&data[0]!=0){
            return false;
        }
        if (data[n]!=0||data[2*8-6-1]!=0){
            return false;
        }
        return true;
    }


    public static void main(String[] args) {
        boolean b = new UTF8().validUtf8(new int[]{1, 99999999, 3});
        System.out.println(b);
    }
}
