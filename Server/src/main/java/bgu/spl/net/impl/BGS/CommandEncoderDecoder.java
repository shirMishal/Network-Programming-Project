package bgu.spl.net.impl.BGS;

import bgu.spl.net.api.MessageEncoderDecoder;


import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CommandEncoderDecoder implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private int countBytes;
    private short shortOpcode;
    private int num_of_zero;
    private String Output;
    private short follow;
    private short num_of_users;

    private String[] str_to_encode;


    public CommandEncoderDecoder() {//TODO check on initialize
        countBytes = 0;
        num_of_zero = 0;
        Output ="";
        shortOpcode = -1;
        follow = -1;
        num_of_users = -1;
    }

    public String decodeNextByte(byte nextByte) {
        if (countBytes == 0) {
            pushByte(nextByte);
            countBytes++;
        }
        else if (countBytes == 1){
            pushByte(nextByte);
            countBytes++;
            shortOpcode = popBytesToShort();
            Output = String.valueOf(shortOpcode);
            switch (shortOpcode) {
                case 1:
                    Output = Output + " ";
                    break;
                case 2:
                    Output = Output + " ";
                    break;
                case 3://logout
                    String Output_to_return3 = Output;
                    //System.out.println(Output_to_return3);
                    clean();//func clean fields
                    return Output_to_return3;
                case 4:
                    Output = Output + " ";
                    break;
                case 5:
                    Output = Output + " ";
                    break;
                case 6:
                    Output = Output + " ";
                    break;
                case 7://userlist
                    String Output_to_return7 = Output;
                    clean();//func clean fields
                    return Output_to_return7;
                case 8:
                    Output = Output + " ";
                    break;
            }
        }
        /*
        //else if (countBytes >= 2){
            //shortOpcode = popBytesToShort();
            //Output = String.valueOf(shortOpcode) + " ";
            //Output = Output + " ";
            switch (shortOpcode) {
                case 1:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 2:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 3://logout
                    String Output_to_return3 = Output.substring(0, Output.length()-1);
                    System.out.println(Output_to_return3);
                    clean();//func clean fields
                    return  Output_to_return3;
                case 4:
                    //countBytes ++;//TODO check on this line location
                    return FollowDecodeNextByte(nextByte);
                case 5:
                    return PostStatDecodeNextByte(nextByte);
                case 6:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 7://userlist
                    String Output_to_return7 = Output.substring(0, Output.length()-1);
                    clean();//func clean fields
                    return  Output_to_return7;
                case 8:
                    return PostStatDecodeNextByte(nextByte);
            }
        }*/
        else{
            switch (shortOpcode) {
                case 1:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 2:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 4:
                    //countBytes ++;//TODO check on this line location
                    return FollowDecodeNextByte(nextByte);
                case 5:
                    return PostStatDecodeNextByte(nextByte);
                case 6:
                    return RegisterLoginPmDecodeNextByte(nextByte);
                case 8:
                    return PostStatDecodeNextByte(nextByte);
            }
        }
        return null;
    }


    public byte[] encode(String s) {//TODO check on null option do we need to consider that option?i think not
        if (s!= null){
            String[] strArr = s.split(" ");// TODO drisa?
            str_to_encode = strArr;
            shortOpcode = Short.valueOf(str_to_encode[0]);
            switch (shortOpcode){
                case 9:
                    return encodeNotification();
                case 10:
                    return encodeAck();
                case 11:
                    return encodeError();
            }
        }
        return null;
    }


    private byte[] encodeNotification(){
        byte [] opcode = shortToBytes(shortOpcode);
        pushByteArr(opcode);
                //TODO do we need to check str_to_encode[1].length()==1?
        byte[] tmp;
        for (int i=1; i<str_to_encode.length ; i=i+1){
            tmp = str_to_encode[i].getBytes();
            pushByteArr(tmp);
            if (i==2|| i==str_to_encode.length-1){
                pushByte((byte)'\0');//push 0//TODO check this byte... do we need here casting
            }
            else if(i>2 && i< str_to_encode.length-1){
                pushByte((byte)' ');
            }
        }
        return bytesToOutput();
    }

    private byte[] encodeAck(){
        byte [] opcode = shortToBytes(shortOpcode);
        pushByteArr(opcode);
        short m_code = Short.valueOf(str_to_encode[1]);
        switch (m_code) {
            case 1:
                return AckSimpleEncode(m_code);
            case 2:
                return AckSimpleEncode(m_code);
            case 3://logout
                return AckSimpleEncode(m_code);
            case 4:
                return AckFollowUserlistEncode(m_code);
            case 5:
                return AckSimpleEncode(m_code);
            case 6:
                return AckSimpleEncode(m_code);
            case 7://userlist
                return AckFollowUserlistEncode(m_code);
            case 8:
                return AckStatEncode(m_code);
        }
        return null;//TODO check on null
    }

    private byte[] AckSimpleEncode(short m_op){
        byte [] m_opcode = shortToBytes(m_op);
        pushByteArr(m_opcode);
        return bytesToOutput();
    }

    private byte[] AckFollowUserlistEncode(short m_op){
        byte [] m_opcode = shortToBytes(m_op);
        pushByteArr(m_opcode);
        byte [] numOfUsers = shortToBytes(Short.valueOf(str_to_encode[2]));
        pushByteArr(numOfUsers);
        if (Short.valueOf(str_to_encode[2]) == 0){
            return bytesToOutput();
        }
        for(int i = 3; i<str_to_encode.length;i++){
            pushByteArr(str_to_encode[i].getBytes());
            pushByte((byte) '\0');
        }
        return bytesToOutput();
    }

    private byte[] AckStatEncode(short m_op){
        byte [] m_opcode = shortToBytes(m_op);
        pushByteArr(m_opcode);
        for(int i = 2; i<str_to_encode.length;i++){
            byte [] tmp = shortToBytes(Short.valueOf(str_to_encode[i]));
            pushByteArr(tmp);
        }
        return bytesToOutput();
    }

    private byte[] encodeError(){
        byte[] byteArrOutput = new byte[4];
        byteArrOutput[0] = (byte)((shortOpcode >> 8) & 0xFF);
        byteArrOutput[1] = (byte)(shortOpcode & 0xFF);
        short m_code = Short.valueOf(str_to_encode[1]);
        byteArrOutput[2] = (byte)((m_code >> 8) & 0xFF);
        byteArrOutput[3] = (byte)(m_code & 0xFF);
        return byteArrOutput;
    }

    private byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private void pushByteArr(byte[] ByteToAdd) {
        if (len + ByteToAdd.length >= bytes.length) {
            bytes = Arrays.copyOf(bytes, (len + ByteToAdd.length) * 2);
        }
        for (byte b: ByteToAdd){
            bytes[len++] = b;
        }
    }

    private byte[] bytesToOutput(){
        byte [] output = new byte[len];
        for (int i = 0; i< len; i++){
            output[i] = bytes[i];
        }
        len = 0;
        return output;
    }
    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        //TODO initialize all
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return result;
    }

    private short popBytesToShort() {
        short result = (short)((bytes[0] & 0xff) << 8);
        result += (short)(bytes[1] & 0xff);
        len = 0;
        return result;
    }

    private short popOneBytesToShort() {
        char c = (char) bytes[0];
        short result = Short.parseShort(c + "");
        //short result = (short) ((bytes[0] & 0xff) << 8);
        len = 0;
        return result;
    }

    private void clean(){
        countBytes = 0;
        num_of_zero = 0;
        Output ="";
        shortOpcode = -1;
        follow = -1;
        num_of_users = -1;
    }

    private String RegisterLoginPmDecodeNextByte(byte nextByte) {
        if (nextByte == '\0') {
            Output = Output + popString();
            num_of_zero ++;
            countBytes++;
            if (num_of_zero ==2){
                String Output_to_return = Output;
                clean();//func clean fields
                //System.out.println(Output_to_return);
                return  Output_to_return;
            }
            else{
                Output = Output + " ";
                return null;
            }
        }
        else{
            pushByte(nextByte);
            countBytes++;
            return null; //not a word yet
        }
    }


    private String FollowDecodeNextByte(byte nextByte) {
        countBytes++;
        if (countBytes == 3) {
            pushByte(nextByte);
            follow = popOneBytesToShort();
            Output = Output + String.valueOf(follow) + " ";
            //System.out.println("enter follow");
            return null;
        }
        else if (countBytes == 4){
            pushByte(nextByte);
            return null;
        }

        else if (countBytes == 5){
            pushByte(nextByte);
            num_of_users = popBytesToShort();
            Output = Output + String.valueOf(num_of_users);
            if (num_of_users ==0){
                String Output_to_return = Output;
                clean();//func clean fields
                //System.out.println(Output_to_return);
                return  Output_to_return;
            }
            else{
                Output = Output+ " ";
                return  null;
            }
        }
        else if (nextByte == '\0'){
            Output = Output + popString();
            num_of_zero ++;
            if (num_of_zero == num_of_users){
                String Output_to_return = Output;
                clean();//func clean fields
                //System.out.println(Output_to_return);
                return  Output_to_return;
            }
            else{
                Output = Output + " ";
                return null;
            }
        }
        else{
            pushByte(nextByte);
            //countBytes++;
            return null; //not a word yet
        }
    }

    private String PostStatDecodeNextByte(byte nextByte) {
        countBytes++;
        if (nextByte == '\0') {
            Output = Output + popString();
            String Output_to_return = Output;
            //func clean fields
            clean();
            //System.out.println(Output_to_return);
            return  Output_to_return;
        }
        else{
            pushByte(nextByte);
            return null; //not a word yet
        }

    }

}
