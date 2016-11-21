/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package TrainManagement;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.AID;
import TrainManagement.TrainAgent;
import java.io.*;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Junction extends Agent {
    
        String Name[] = new String[10];
        int track[] = new int[10];
        int dir[] = new int[10];
        String time[] = new String[10];
        String trainCoordinates[] = new String[10];
        double velocity[] = new double[10];
        String source[] = new String[10];
        String dest[] = new String[10];
        String stationToCoordinates[] = new String[10];
        String stationFromCoordinates[] = new String[10];
        double retard[] = new double[10];
        int priority[] = new int[10];
        
        int top = -1;
        
        String coordinates;
        
        static int headon=0;
        static int rear=0;
        static int noOfMessages=0;
        static ArrayList <Collision> detectedCollisions = new ArrayList<>();
        
    protected void setup() {
        Object[] args = getArguments();
        coordinates = args[0].toString();
        System.out.println("junction "+getAID().getLocalName()+" present at coordinates "+coordinates+"...");
        
        addBehaviour(new CyclicBehaviour(this) {
            MessageTemplate mt=MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage mt1=new ACLMessage(ACLMessage.INFORM);
            
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
                
                return diff;
            }
            
            void writeToFile() throws IOException{
                try (FileOutputStream fos = new FileOutputStream("Result/Result_Junction.txt")) {
                    String str = "headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages;
                    fos.write(str.getBytes());
                }
                System.out.println("headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages);
            }
            
            void addColl(String a, String b, String c, double v1, double v2, double r1, double r2, String c1, String c2, int p1, int p2){
                ListIterator<Collision> litr = detectedCollisions.listIterator();
                boolean flag = false;
                while(litr.hasNext()){
                    Collision obj = litr.next();
                    if((obj.t1.equals(a) && obj.t2.equals(b))||
                        (obj.t2.equals(a) && obj.t1.equals(b))){
                        flag = true;
                        obj.reportedBy.add(getAID().getLocalName());
                        break;
                    }
                }
                if(flag == false){                    
                    double s1 = v1*v1/(2*r1);
                    double s2 = v2*v2/(2*r2);
                    //finding avoidance
                    if(c.equals("HEADONsame")){
                        if(distance(c1,c2)> s1+s2+20)
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"STOP","STOP"));
                        else
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,false,"",""));
                    }
                    else if(c.equals("HEADONdiff")){
                        if(distance(coordinates,c1)>s1 && distance(coordinates,c2)>s2){
                            if(p1>p2)
                                detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"MOVE","STOP"));
                            else
                                detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"STOP","MOVE"));
                        }
                        else if(distance(coordinates,c1)>s1)
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"STOP","MOVE"));
                        else if(distance(coordinates,c2)>s2)
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"MOVE","STOP"));
                        else
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,false,"STOP","STOP"));
                    }
                    else{
                        if(distance(c1,c2)-s2 > 20)
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,true,"MOVE","STOP"));
                        else
                            detectedCollisions.add(new Collision(a,b,getAID().getLocalName(),c,false,"STOP","STOP"));
                    }
                }
            }
            
            @Override
            public void action() {
                
                ACLMessage msg=receive(mt);
                mt1.clearAllReceiver();
                if (msg != null) {
                    // Message received. Process it
                    String title = msg.getContent();
                    AID sen= msg.getSender();
                    String arr[] = title.split(",");
                    System.out.println("\nJunction "+getAID().getLocalName()+":");
                    System.out.println("Train\t\t:" +arr[0]);
                    System.out.println("Track\t\t:"+arr[1]);
                    //System.out.println("Direc\t\t:" +arr[2]);
                    //System.out.println("Time\t\t:" +arr[3]);
                    //System.out.println("Coordinates\t:"+arr[4]);
                    //System.out.println("Velocity \t:"+arr[5]);
                    System.out.println("Source \t\t:"+arr[6]);
                    System.out.println("Destination \t:"+arr[7]);
                    
                    ++top;
                    Name[top] = arr[0];
                    track[top] = Integer.parseInt(arr[1]);
                    dir[top] = Integer.parseInt(arr[2]);
                    time[top] = arr[3];
                    trainCoordinates[top] = arr[4];
                    velocity[top] = Double.parseDouble(arr[5]);
                    source[top] = arr[6];
                    dest[top] = arr[7];
                    stationToCoordinates[top] = arr[8];
                    stationFromCoordinates[top] = arr[9];
                    retard[top] = Double.parseDouble(arr[10]);
                    priority[top] = Integer.parseInt(arr[11]);
                    
                    boolean flag = false;
                    for(int i=0;i<top;i++){
                        double TDiff = timeDiff(time[top],time[i]);
                        double C1 = distance(trainCoordinates[top],coordinates)/velocity[top];
                        double C2 = distance(trainCoordinates[i],coordinates)/velocity[i];
                        double CDiff = C1-C2;
                        
                        //same track
                        if(track[i] == track[top] && Math.abs(TDiff)<=1200 && distance(trainCoordinates[top],coordinates)<1000 && distance(trainCoordinates[i],coordinates)<1000){
                            //headon
                            if(dir[i] != dir[top]){
                                headon++;
                                addColl(Name[top],Name[i],"HEADONsame",velocity[top],velocity[i],retard[top],retard[i],trainCoordinates[top],trainCoordinates[i],priority[top],priority[i]);
                                flag = true;
                                mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                            }
                            //rear
                            else{
                                int first=-1,second=-1;
                                //approach
                                if(dir[i] == 1){
                                    if(distance(trainCoordinates[i],coordinates)<distance(trainCoordinates[top],coordinates)){
                                        first = i;
                                        second = top;
                                    }
                                    else{
                                        first = top;
                                        second = i;
                                    }
                                }
                                //going away
                                else{
                                    if(distance(trainCoordinates[i],coordinates)<distance(trainCoordinates[top],coordinates)){
                                        first = top;
                                        second = i;
                                    }
                                    else{
                                        first = i;
                                        second = top;
                                    }
                                }
                                if(velocity[first]<velocity[second]){
                                    rear++;
                                    addColl(Name[first],Name[second],"REAREND",velocity[first],velocity[second],retard[first],retard[second],trainCoordinates[first],trainCoordinates[second],priority[top],priority[i]);
                                    flag = true;
                                    mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                                }
                            }
                        }
                        //different track
                        else if(track[i] != track[top] && dir[i]==1 && dir[top]==1 && Math.abs(TDiff + CDiff)<=1200 && distance(trainCoordinates[top],coordinates)<1000 && distance(trainCoordinates[i],coordinates)<1000){
                            headon++;
                            addColl(Name[top],Name[i],"HEADONdiff",velocity[top],velocity[i],retard[top],retard[i],trainCoordinates[top],trainCoordinates[i],priority[top],priority[i]);
                            flag = true;
                            mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                        }
                    }
                    if(flag){
                        mt1.setContent("You are going to collide!!!");
                        noOfMessages++;
                        try {
                            writeToFile();
                        } catch (IOException ex) {
                            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                        mt1.setContent("You are safe!!!");
                    mt1.addReceiver(sen);
                    send(mt1);
                    
                    //Broadcast no of trains to each train
                    mt1.clearAllReceiver();
                    String str="ListOfTrains:";
                    for(int i=0;i<=top;i++){
                        if(track[i] == Integer.parseInt(arr[1])){
                            mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                                 str=str+Name[i]+",";
                        } 
                    }
                    mt1.setContent(str);
                    send(mt1);
                    
                    //System.out.println("----------Collisions detected by junction: headon="+headon+" rear="+rear+" ----------");
                    
                }else {
                   // block();
                }
            }
    });
}
}