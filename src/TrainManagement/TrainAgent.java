/*
Arguments to create a TrainAgent
name
track
//dir
station from
station to
time
coordinates
*/
package TrainManagement;

import jade.core.Agent;
//import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
//import jade.core.behaviours.WakerBehaviour;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

class Collision{
    String t1;
    String t2;
    String type;
    String avoidance;
    String solt1;
    String solt2;
    ArrayList <String> reportedBy = new ArrayList<>();
    Collision(String a,String b,String c,String d){
        t1 = a;
        t2 = b;
        type = d;
        reportedBy.add(c);
    }
}

public class TrainAgent extends Agent {
    
    String Name[] = new String[10];
    boolean coll[] = new boolean[10];
    int top = -1;
    String name;
    int track;
    int dir;
    String stationFrom;
    String stationTo;
    String time;
    String coordinates;
    double velocity;
    String path[];
    
    static int headon=0;
    static int rear=0;
    static int noOfMessages=0;
    static ArrayList <Collision> detectedCollisions = new ArrayList<>();
    
    String Msg;
    Scanner in = new Scanner(System.in);
    static int count = 0;
    
    protected void setup() {
        // receiving messages from First
        ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
        ACLMessage mt2=new ACLMessage(ACLMessage.INFORM);
        //Object[] args = getArguments();
        String args[]=null;
        
        try{
            File dir = new File(".");
            File fin = new File(dir.getCanonicalPath() + File.separator + "Dataset/Dataset_OA.txt");		
            FileInputStream fis = new FileInputStream(fin);
            //Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            int i=0;
            
            while ((line = br.readLine()) != null && i<=count) {
                //System.out.println(line);
                args = line.split(" ");
                i++;
            }
            //System.out.println(line+"----Length="+args.length);
        }catch(IOException e){
            System.out.println("Train Agent cannot be created!! "+e);
        }
        
        name = args[0];
        track = Integer.parseInt(args[4]);
        dir = Integer.parseInt(args[5]);
        stationTo = args[2];
        time = args[1];
        coordinates = args[3];
        velocity = Double.parseDouble(args[6]);
        path = args[8].substring(1,args[8].length()-1).split(",");
        
        int i;
        if(dir==1){
            for(i=0; i<path.length;i++){
                if(path[i].equals(stationTo))
                    break;
            }
            stationFrom = path[i-1];
        }else{
            for(i=path.length-1; i>=0;i--){
                if(path[i].equals(stationTo))
                    break;
            }
            stationFrom = path[i+1];
        }
        
        count++;
        String abc;
        
        msg.clearAllReceiver();
        abc = name+","+track+","+2+","+time+","+coordinates+","+velocity+","+stationFrom+","+stationTo;
        msg.setContent(abc);
        msg.addReceiver(new AID(stationFrom,AID.ISLOCALNAME));
        send(msg);
        
        msg.clearAllReceiver();
        abc = name+","+track+","+1+","+time+","+coordinates+","+velocity+","+stationFrom+","+stationTo;
        msg.setContent(abc);
        msg.addReceiver(new AID(stationTo,AID.ISLOCALNAME));
        send(msg);
                
        addBehaviour(new CyclicBehaviour(this) {
            
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            
            public double distance(String a, String b){
                double aX = Double.parseDouble(a.split(":")[0]);
                double aY = Double.parseDouble(a.split(":")[1]);
                
                double bX = Double.parseDouble(b.split(":")[0]);
                double bY = Double.parseDouble(b.split(":")[1]);
                
                double dis = Math.sqrt((bX-aX)*(bX-aX)+(bY-aY)*(bY-aY));
                                
                return dis;
            }
            
            public int timeDiff(String a, String b){
                String aA[] = a.split(":");
                int aH = Integer.parseInt(aA[0]);
                int aM = Integer.parseInt(aA[1]);
                int aS = Integer.parseInt(aA[2]);
                
                String bA[] = b.split(":");
                int bH = Integer.parseInt(bA[0]);
                int bM = Integer.parseInt(bA[1]);
                int bS = Integer.parseInt(bA[2]);
                
                int diff = (aH-bH)*60*60 + (aM-bM)*60 + (aS-bS);
                
                if(diff>0)
                    return diff;
                else
                    return diff*-1;
            }
            
            void writeToFile() throws IOException{
                try (FileOutputStream fos = new FileOutputStream("Result_Train.txt")) {
                    String str = "headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages;
                    fos.write(str.getBytes());
                }
                System.out.println("headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages);
            }
            
            @Override
            public void action() {
                
                ACLMessage msg1=receive(mt);
                
                //if a message has been sent 
                if(msg1!=null) {
                    //if the message sent is by Station/Junction
                    if((msg1.getSender().getLocalName().charAt(0) == 's')||
                            (msg1.getSender().getLocalName().charAt(0) == 'j')){
                        Msg=msg1.getContent();
                        
                        //if the message sent by the station/junction is about ListOfTrains
                        if(Msg.contains("ListOfTrains")){
                            String list[];
                            if(Msg.split(":").length != 1){
                                list = Msg.split(":")[1].split(",");
                                for (String list1 : list) {
                                    
                                    int j;
                                    if(list1.equals(getAID().getLocalName()))
                                        continue;
                                    for (j=0; j<=top; j++) {
                                        if (list1.equals(Name[j])) {
                                            break;
                                        }
                                    }
                                    if (j == top+1) {
                                        //System.out.println(getAID().getLocalName()+": train added!!!!");
                                        Name[++top] = list1;
                                        coll[top] = false;
                                    }
                                }
                            }
                        }
                        
                        //if it is something else
                        else{
                            //msg1.clearAllReceiver(); 
                            AID sen= msg1.getSender();
                            System.out.println(getAID().getLocalName()+": Message received from "+sen.getLocalName()+"\n\t"+Msg);
                        }
                    }
                    
                    //message is sent by trains
                    else{
                        Msg=msg1.getContent();
                        
                        if(Msg.split(",").length < 2){                          
                            String sender = msg1.getSender().getLocalName();
                            for(int i =0;i<=top;i++){
                                if(sender.equals(Name[i])){
                                    System.out.println(getAID().getLocalName()+": "+"Message received from train "+sender);
                                    System.out.println('\t'+Msg);
                                    coll[i]=true;
                                    /*
                                    if(Msg.contains("Headon")){
                                        readFromFile();
                                        headon++;
                                        try {
                                            writeToFile();
                                        } catch (IOException ex) {
                                            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    else if(Msg.contains("RearEnd")){
                                        readFromFile();
                                        rear++;
                                        try {
                                            writeToFile();
                                        } catch (IOException ex) {
                                            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    */
                                    break;
                                }
                            }
                        }
                        else{
                            String n = Msg.split(",")[0];
                            String t = Msg.split(",")[1];
                            String c = Msg.split(",")[2];
                            String sT = Msg.split(",")[3];
                            String sF = Msg.split(",")[4];
                            double v = Double.parseDouble(Msg.split(",")[5]);
                            
                            int i;
                            String sender = msg1.getSender().getLocalName();
                            for(i =0;i<=top;i++){
                                if(sender.equals(Name[i]))
                                    break;
                            }
                            if(sender.equals(Name[i]) && !coll[i]){
                                if(timeDiff(t,time)<=1200 && distance(c,coordinates)<200){
                                    String str;
                                    //headon
                                    if(stationTo.equals(sF)){
                                        str = "Headon Collision Detected";
                                        headon++;
                                    }
                                    //rear
                                    else{
                                        str = "RearEnd Collision Detected";
                                        rear++;
                                    }
                                    mt2.clearAllReceiver();
                                    mt2.setContent(str);
                                    mt2.addReceiver(msg1.getSender());
                                    send(mt2);
                                    noOfMessages++;
                                    coll[i] = true;
                                    try {
                                        writeToFile();
                                    } catch (IOException ex) {
                                        Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }    
                            }
                        }
                    }                  
                }else{
                    mt2.clearAllReceiver();
                    String str = name+","+time+","+coordinates+","+stationTo+","+stationFrom+","+velocity;
                    mt2.setContent(str);
                    for(int i=0;i<=top;i++){
                        if(coll[i] == false)
                            mt2.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                    }
                    send(mt2);
                }
            }
        });
    }
}
