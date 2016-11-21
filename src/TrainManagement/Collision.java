/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TrainManagement;

import java.util.ArrayList;

/**
 *
 * @author abhijeet
 */
public class Collision{
    String t1;
    String t2;
    String type;
    boolean avoidance;
    String solt1;
    String solt2;
    ArrayList <String> reportedBy = new ArrayList<>();
    Collision(String a,String b,String c,String d, boolean e, String s1,String s2){
        t1 = a;
        t2 = b;
        type = d;
        reportedBy.add(c);

        avoidance = e;
        solt1 = s1;
        solt2 = s2;
    }
}
