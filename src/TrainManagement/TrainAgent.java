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

public class TrainAgent extends Agent {
    
    String Name[] = new String[10];
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
   
    String Msg;
    Scanner in = new Scanner(System.in);
    static int count = 0;
    
    protected void setup() {
        // receiving messages from First
        ACLMessage msg=new ACLMessage(ACLMessage.REQUEST);
        //Object[] args = getArguments();
        String args[]=null;
        
        try{
            File dir = new File(".");
            File fin = new File(dir.getCanonicalPath() + File.separator + "dataset4.txt");		
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
            System.out.println(line+"----Length="+args.length);
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
            @Override
            public void action() {
                
                ACLMessage msg1=receive(mt);
                
                //if a message has been sent 
                if(msg1!=null) {
                    
                    //if the message sent is by Station
                    if(msg1.getSender().getLocalName().charAt(0) == 's'){
                        Msg=msg1.getContent();
                        
                        //if the message sent by the station is about ListOfTrains
                        if(Msg.contains("ListOfTrains")){
                            System.out.println("I have got the list of trains!! "+Msg);
                            String list[];
                            list = Msg.split(":")[1].split(",");
                            for(int i=0;i<list.length;i++){
                                int j;
                                for(j=0;j<=top;j++){
                                    if(list[i].equals(Name[j]))
                                        break;
                                }
                                if(j == top+1)
                                    Name[++top] = list[i];
                            }
                        }
                        
                        //if it is something else
                        else{
                            //msg1.clearAllReceiver(); 
                            AID sen= msg1.getSender();
                            System.out.println(getAID().getLocalName()+": Message received from station "+sen.getLocalName()+"\n\t"+Msg);
                        }
                    }
                    
                }else{
                    //System.out.println("No got!!");
                    //block();
                }
            }
        });
    }
}
