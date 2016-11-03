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



public class Junction extends Agent {
    
        String Name[] = new String[10];
        int track[] = new int[10];
        int dir[] = new int[10];
        String time[] = new String[10];
        String trainCoordinates[] = new String[10];
        double velocity[] = new double[10];
        String source[] = new String[10];
        String dest[] = new String[10];
        
        int top = -1;
        
        String coordinates;
        
        static int collisions=0;
        static int headon=0;
        static int rear=0;
        
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
                try (FileOutputStream fos = new FileOutputStream("Result.txt")) {
                    readFromFile();
                    String str = "headon:"+headon+",rear:"+rear;
                    fos.write(str.getBytes());
                }
            }
            
            void readFromFile(){
                try{  
                    InputStream fis=new FileInputStream("Result.txt");   
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String str;
                    if((str= br.readLine())!=null){
                        headon = Integer.parseInt(str.split(",")[0].split(":")[1]);
                        rear = Integer.parseInt(str.split(",")[1].split(":")[1]);
                        fis.close();
                    }
                }catch(Exception e){System.out.println(e);}  
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
                    
                    boolean flag = false;
                    for(int i=0;i<top;i++){
                        double TDiff = timeDiff(time[top],time[i]);
                        double C1 = distance(trainCoordinates[top],coordinates)/velocity[top];
                        double C2 = distance(trainCoordinates[i],coordinates)/velocity[i];
                        double CDiff = C1-C2;
                        
                        //same track
                        if(track[i] == track[top] && Math.abs(TDiff)<=1200 && distance(trainCoordinates[top],coordinates)<2000 && distance(trainCoordinates[i],coordinates)<2000){
                            //headon
                            if(dir[i] != dir[top]){
                                readFromFile();
                                headon++;
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
                                    readFromFile();
                                    rear++;
                                    flag = true;
                                    mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                                }
                            }
                        }
                        //different track
                        else if(track[i] != track[top] && dir[i]==1 && dir[top]==1 && Math.abs(TDiff + CDiff)<=1200 && distance(trainCoordinates[top],coordinates)<2000 && distance(trainCoordinates[i],coordinates)<2000){
                            readFromFile();
                            headon++;
                            flag = true;
                            mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                        }
                    }
                    if(flag){
                        mt1.setContent("You are going to collide!!!");
                        try {
                            writeToFile();
                        } catch (IOException ex) {
                            Logger.getLogger(Station.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        collisions++;
                    }
                    else
                        mt1.setContent("You are safe!!!");
                    mt1.addReceiver(sen);
                    send(mt1);
                    
                    System.out.println("----------Collisions detected by junctions: "+collisions+" ----------");
                    
                }else {
                   // block();
                }
            }
    });
}
}