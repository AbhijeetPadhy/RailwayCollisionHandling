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



public class Station extends Agent {
    
        String Name[] = new String[10];
        int track[] = new int[10];
        int dir[] = new int[10];
        String time[] = new String[10];
        int top = -1;
        
        String coordinates;
        
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
                
                return Math.sqrt((bX-aX)*(bX-aX)+(bY-aY)*(bY-aY));
            }
            
            public int dateDiff(String a, String b){
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
            
            @Override
            public void action() {
                
                ACLMessage msg=receive(mt);
                mt1.clearAllReceiver();
                if (msg != null) {
                    // Message received. Process it
                    String title = msg.getContent();
                    AID sen= msg.getSender();
                    String arr[] = title.split(",");
                    System.out.println("Train: " +arr[0]);
                    System.out.println("Track: "+arr[1]);
                    System.out.println("Direc: " +arr[2]);
                    System.out.println("Time: " +arr[3]);
                    
                    ++top;
                    Name[top] = arr[0];
                    track[top] = Integer.parseInt(arr[1]);
                    dir[top] = Integer.parseInt(arr[2]);
                    time[top] = arr[3];
                    boolean flag = false;
                    for(int i=0;i<top;i++){
                        if(track[i] == track[top] && dateDiff(time[top],time[i])<=1200){
                            flag = true;
                            mt1.addReceiver(new AID(Name[i],AID.ISLOCALNAME));
                        }
                    }
                    if(flag)
                        mt1.setContent("You are going to collide!!!");
                    else
                        mt1.setContent("You are safe!!!");
                    mt1.addReceiver(sen);
                    send(mt1);
                    
                }else {
                   // block();
                }
            }
    });
}
}