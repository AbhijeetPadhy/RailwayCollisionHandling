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
import java.util.logging.Level;
import java.util.logging.Logger;



public class Station extends Agent {
    
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
        double reatard[] = new double[10];
        
        int top = -1;
        
        String coordinates;
        
        static int headon=0;
        static int rear=0;
        static int noOfMessages=0;
        
    protected void setup() {
        Object[] args = getArguments();
        coordinates = args[0].toString();
        System.out.println("Station "+getAID().getLocalName()+" started at coordinates "+coordinates+"...");
        
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
                
                if(diff>0)
                    return diff;
                else
                    return diff*-1;
            }
            
            void writeToFile() throws IOException{
                try (FileOutputStream fos = new FileOutputStream("Result/Result_Station.txt")) {
                    String str = "headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages;
                    fos.write(str.getBytes());
                }
                System.out.println("headon:"+headon+",rear:"+rear+",noOfMessages:"+noOfMessages);
            }
            
            boolean isHeadOn(int a,int b){
                if((dir[a] == 0 && dir[b]!=0)||(dir[a] != 0 && dir[b]==0))
                    return true;
                if(dir[a]!=dir[b] && source[a].equals(dest[b]) && dest[a].equals(source[b]))
                    return true;
                if(dir[a]==dir[b] && dest[a].equals(dest[b]) && !source[a].equals(source[b]))
                    return true;
                return false;
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
                    System.out.println("\nStation "+getAID().getLocalName()+":");
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
                    reatard[top] = Double.parseDouble(arr[10]);
                    
                    boolean flag = false;
                    for(int i=0;i<top;i++){
                        if(track[i] == track[top] && timeDiff(time[top],time[i])<=1200 && distance(trainCoordinates[top],coordinates)<1000 && distance(trainCoordinates[i],coordinates)<1000){
                            if(dir[i]==0 && dir[top]==0)
                                ;
                            else if( isHeadOn(i,top) ){
                                flag = true;
                                mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                                headon++;
                            }
                            
                            else{
                                int first=-1,second=-1;
                                if(source[i].equals(source[top])){
                                    System.out.println(distance(trainCoordinates[i],coordinates)+"......"+distance(trainCoordinates[top],coordinates));
                                    if(distance(trainCoordinates[i],coordinates)<distance(trainCoordinates[top],coordinates)){
                                        if(dir[i] == 1){
                                            first = i;
                                            second = top;
                                        }
                                        else{
                                            first = top;
                                            second = i;
                                        }
                                    }else{
                                        if(dir[i] == 1){
                                            first = top;
                                            second = i;
                                        }
                                        else{
                                            first = i;
                                            second = top;
                                        }
                                    }
                                }
                                else if(dest[i].equals(dest[top])){
                                    if(distance(trainCoordinates[i],coordinates)<distance(trainCoordinates[top],coordinates)){
                                        first = top;
                                        second = i;
                                    }else{
                                        first = i;
                                        second = top;
                                    }
                                }
                                else if(source[i].equals(dest[top])){
                                    first = i;
                                    second = top;
                                }
                                else if(dest[i].equals(source[top])){
                                    first = top;
                                    second = i;
                                }
                                if(first!=-1 && second!=-1 && velocity[first]<velocity[second]){
                                    flag = true;
                                    mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                                    rear++;
                                }
                            }
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
                    
                    //System.out.println("----------Collisions detected by stations: headon="+headon+" rear="+rear+" ----------");
                    
                }else {
                   // block();
                }
            }
    });
}
}