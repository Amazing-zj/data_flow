package Test;


import com.mcg.entity.flow.connector.ConnectorData;
import com.mcg.entity.flow.connector.ParamData;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static List<ConnectorData> list;
    public static void main(String[] args) throws Exception{

    }



    private static void persistent() throws  Exception{
        String path = "E://recovery//seri.class";
    }

    private static boolean isNameLegal(String name){
        return name.matches("[A-Za-z](\\w*_*)");
    }

    static {
        list = new LinkedList<>();
    }

    static class ConnectorList{
        private List<Integer> in;
        private List<Integer> out;
        public ConnectorList(int s, int t){
            in = new LinkedList<>();
            out = new LinkedList<>();
            in.add(s);
            in.add(t);
        }

        public boolean isLegal(int s, int t){
            if(in.contains(t) && out.contains(s))
                return false;
            return true;
        }

        public boolean isContain(int s, int t, List<ConnectorList> sList, List<ConnectorList> tList){
            if(in.contains(s))
            {
                sList.add(in.contains(s)? this : out.contains(s)? this: this);
                tList.add(this);
                out.add(t);
                return true;
            }else if(out.contains(s)){
              in.add(s);
              out.add(t);
              sList.add(this);
              return true;
            } else{
                return false;
            }
        }

        public boolean addGroup(ConnectorList target){
            boolean flag = false;
            for(int i : target.in){
                if(!flag &&out.contains(i)){
                    flag = true;
                }
                if(!in.contains(i)){
                    in.add(i);
                }
            }
            for(int i: target.out){
                if(in.contains(i)){
                    return false;
                }
                if(!out.contains(i)){
                    out.add(i);
                }
            }
            return true;
        }

    }


    private static  boolean TP(){
//        list.add(new ConnectorData("1","2"));
//        list.add(new ConnectorData("1","3"));
        list.add(new ConnectorData("1","4"));
        list.add(new ConnectorData("2","3"));
        list.add(new ConnectorData("2","5"));
        list.add(new ConnectorData("3","5"));
        list.add(new ConnectorData("4","3"));
        list.add(new ConnectorData("3","1"));
        int i = 0 ;
        int index = 0;
        int s ,t;
        List <ConnectorList> c_list = new LinkedList<>();
        Map <String,Integer> map = new TreeMap<>();
        List <ConnectorList> mergeListSource ;
        List <ConnectorList> mergeListTarget ;
        while(i < list.size()){
            ConnectorData data = list.get(i);
            if( map.containsKey(data.getSourceId())){
                s = map.get(data.getSourceId());
            }else{
                map.put(data.getSourceId(),index);
                s = index++;
            }
            if(map.containsKey(data.getTargetId())){
                t = map.get(data.getTargetId());
            }else{
                map.put(data.getTargetId(),index);
                t = index++;
            }
            mergeListSource = new LinkedList<>();
            mergeListTarget = new LinkedList<>();
            if(c_list.size() != 0){
                for(ConnectorList temp : c_list){
                    if(temp.isLegal(s,t)){
                        temp.isContain(s, t, mergeListSource, mergeListTarget);
                    }else {
                        System.out.println(i);
                        return false;
                    }
                }
                if(mergeListSource.size() == 0 && mergeListTarget.size() ==0){
                    c_list.add(new ConnectorList(s,t));
                }else{
                    if(mergeListSource.size() != 0 && mergeListTarget.size() != 0){
                        for(ConnectorList source: mergeListSource){
                            for(ConnectorList target: mergeListTarget){
                                if(!source.addGroup(target)){
                                    System.out.println(i);
                                    return false;
                                }
                            }
                        }
                        c_list.removeAll(mergeListTarget);
                    }
                }
            }else{
                c_list.add(new ConnectorList(s,t));
            }
            i++;
        }
        return  true;
    }

    private static void replace(){
        String str = "String    name = name1 ; String name2 = name_1 +  name; name.split(); if(name<1){ if ( name >2)} ; name+= 2 ; x+=name.split(); name=\" name \"";
        StringBuilder s = new StringBuilder("");
        String [] sA = str.split("\\s+");
        int i = 0;
        while (i<sA.length) {
            s.append(" ");
            if (sA[i].matches("((\\w*\\W+)|(\\W*))name((\\W+\\w*\\W*)|(\\W*))") && !sA[i].matches("\"name\"")) {
                s.append(sA[i].replaceAll("name", "NAME"));
            } else {
                s.append(sA[i]);
            }
            i++;
        }
        System.out.println(s.toString());

        //perfect function
        s.delete(0,s.length()-1);
        sA = str.split("name");
        i = 1;
        s.append(sA[0]);
        while (i<sA.length){
//            System.out.println(sA[i]);
            int index = 0;
            if(sA[i-1].substring(sA[i-1].length()-1).matches("\\W") &&
                    ( (index = sA[i-1].lastIndexOf("\"")) == -1 || !sA[i].substring(index).matches("\"\\s*") ) &&
                    sA[i].charAt(0) != '_' &&
                    ( (index = sA[i].indexOf("\"",0)) == -1 || !sA[i].substring(0,index).matches("\\s*\"") )&&
                    sA[i].substring(0,1).matches("\\W")){
               s.append("NAME");
            } else {
                s.append("name");
            }
            s.append(sA[i]);
            i++;
        }
        System.out.println(s.toString());
        str = s.toString();
        //replace space
        sA = str.split("\\s+");
        s .delete(0,s.length()-1);
        s.append(sA[0]);
        i = 1;
        while(i<sA.length){
            s.append(" "+sA[i]);
            i++;
        }
        System.out.println(s.toString());

    }

    private static void ato(){
        for(int i = 0 ; i < 1000 ; i++) {
            new Thread(new threadB1()).start();
        }
    }

    private static class threadB1 implements Runnable{
        private static AtomicInteger aBoolean;
        private static volatile int index ;

        static {
            aBoolean = new AtomicInteger(0);
            index = 0;
        }

        @Override
        public void run() {
                System.out.println(aBoolean.getAndIncrement());
        }
    }

    private static void CUR(){
        CountDownLatch latch = new CountDownLatch(3);
        Runnable r1 = new threadCDL(latch);
        Runnable r2 = new threadCDL(latch);
        Runnable r3 = new threadCDL2(latch);
        threadCDL r4 = new threadCDL(latch);
        new Thread(r3).start();
        new Thread(r1).start();
        new Thread(r4).start();
        new Thread(r2).start();
    }

    static class threadCDL implements Runnable{
        private CountDownLatch  latch= null;

        public threadCDL(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void run() {
            latch.countDown();
            System.out.println("ready");
        }
    }

    static class threadCDL2 implements Runnable{
        private CountDownLatch  latch= null;

        public threadCDL2(CountDownLatch latch){
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                latch.await();
            }catch (InterruptedException e){System.out.println(latch.getCount());}
            System.out.println("go");
        }

    }

    private static void text(){
        StringBuilder text = new StringBuilder("String parm1,");
        if(text.indexOf("\\,",text.length()-1) != -1){
            text.deleteCharAt(text.length()-1);
        }
        System.out.println(text.toString());
    }

    private static void set(){
        Set<CompareCon> list = new TreeSet<>();
        list.add(new CompareCon("1","2"));
        list.add(new CompareCon("6","2"));
        list.add(new CompareCon("3","2"));
        list.add(new CompareCon("4","2"));
        list.add(new CompareCon("1","2"));
        System.out.println(list.size());
        Set<CompareCon>s_list = new TreeSet<>();
        s_list=list;
        list.remove(new CompareCon("1","2"));
        Iterator(s_list);
        Iterator(list);
    }

    private static void Iterator(Collection collection){
        Iterator i = collection.iterator();
        while(i.hasNext()){
            System.out.println(i.next());
        }
    }

    private static void write() throws Exception{
        File file = new File("E://recovery//Main.txt");
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("A \r");
        bw.write("B \n");
        bw.write("C \n\r");
        bw.flush();
        bw.close();
        fw.close();
        FileReader fileReader = new FileReader(file);
        BufferedReader br = new BufferedReader(fileReader);
        String str = null;
        while((str = br.readLine())!= null){
            System.out.println(str);
        }
        br.close();
        fileReader.close();
    }
}

