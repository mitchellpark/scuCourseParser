import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.Character;

public class javaParser{

    Map<String, Map<String, List<Course>>> sections = null;
    Map<String, String> files = null; 
    List<ToTake> toTake = null;

    public javaParser(){
        sections = new HashMap<>();
        files = new HashMap<>();
        toTake = new ArrayList<>();

        implementFiles();
        implementSections();
        addCoursesToTake();

    }

    public List<Course> getData(String s, String sectionName){
        List<Course> courses = new ArrayList<>();

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(stream==null){
            throw new IllegalArgumentException();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String strLine;
        try {
            String abbreviation = "";
            while ((strLine = reader.readLine()) != null) {
                Course course = new Course();
                if(strLine.length()!=0){
                    if(strLine.charAt(strLine.length()-1)==')'){
                        int parenthesesRange = strLine.indexOf(')')-strLine.indexOf('(');
                        if(parenthesesRange>0 && parenthesesRange <=5){
                            abbreviation = strLine.substring(strLine.indexOf('(')+1, strLine.length()-1);
                        }
                    }
                    if(Character.isDigit(strLine.charAt(0))){
                        int end = strLine.indexOf(' ') == -1 ? strLine.length() : strLine.indexOf(' ');
                        course.courseNum = strLine.substring(0, end);
                        course.courseName = strLine.substring(end);
                        course.abbrv = abbreviation;
                        course.sectionName = sectionName;
                        courses.add(course);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public void implementFiles(){
        files.put("courses_data/arts.txt", "Arts");
        
        files.put("courses_data/ci3.txt", "Culture and Ideas");
        files.put("courses_data/civicEngagement.txt", "Civic Engagement");
        files.put("courses_data/ethics.txt","Ethics");
        
        files.put("courses_data/socialJustice.txt","Social Justice");
        files.put("courses_data/rtc1.txt", "Religion, Theology, and Culture 1");
        files.put("courses_data/rtc2.txt", "Religion, Theology, and Culture 2");
        files.put("courses_data/rtc3.txt", "Religion, Theology, and Culture 3");
    }

    public void implementSections(){
        for(Map.Entry<String, String> ent: files.entrySet()){
            List<Course> c = getData(ent.getKey(), ent.getValue());

            for(int i=0; i<c.size(); i++){
                Course course = c.get(i);
                if(!sections.containsKey(course.abbrv)){
                    Map<String, List<Course>> newMap = new HashMap<>();
                    List<Course> arr = new ArrayList<>();
                    arr.add(course);
                    newMap.put(course.courseNum, arr);
                    sections.put(course.abbrv, newMap);
                    course.frequency = 1;
                }else{
                    if(sections.get(course.abbrv).containsKey(course.courseNum)){
                        List<Course> similarsList = sections.get(course.abbrv).get(course.courseNum);
                        for(int s=0; s<similarsList.size(); s++){
                            similarsList.get(s).frequency++;
                        }
                        course.frequency = similarsList.get(0).frequency+1;
                        sections.get(course.abbrv).get(course.courseNum).add(course);
                    }else{
                        List<Course> similarCourses = new ArrayList<>();
                        similarCourses.add(course);
                        sections.get(course.abbrv).put(course.courseNum,similarCourses);
                        course.frequency = 1;
                    }
                }
            }
            files = null;
        }
    }

    public void addCoursesToTake(){
        for(Map.Entry<String, Map<String, List<Course>>> ent: sections.entrySet()){
            for(Map.Entry<String, List<Course>> anotherEnt: ent.getValue().entrySet()){
                if(anotherEnt.getValue().size()>1){
                    int size = anotherEnt.getValue().size();
                    String[] fs = new String[size];
                    for(int i=0; i<anotherEnt.getValue().size(); i++){
                        fs[i] = anotherEnt.getValue().get(i).sectionName;
                    }
                    Course first = anotherEnt.getValue().get(0);
                    String title = first.abbrv + " " + first.courseNum + ": " + first.courseName;
                    String section = first.sectionName;
                    ToTake tt = new ToTake(fs, title, size);
                    sortedInsert(tt);
                }
            }
        }
        System.out.println(toTake.size());
    }
    
    public void sortedInsert(ToTake c){
        if(toTake.size()==0) toTake.add(c);
        
        boolean added = false;
        for(int i=0; i<toTake.size(); i++){
            if(c.frequency>=toTake.get(i).frequency){
                toTake.add(i, c);
                added = true;
                break;
            }
        }
        if(!added) toTake.add(c);
    }

    public void printClasses(){
        
        int limit = toTake.size()<10? toTake.size(): 10;
        for(int i=0; i<limit; i++){
            System.out.println("Take " + toTake.get(i).title + " for a frequency of " + toTake.get(i).frequency + ".");
            System.out.println("Fulfilled Sections: ");

            for(int j=0; j<toTake.get(i).fulfilledSections.length; j++){
                System.out.println("     -" +toTake.get(i).fulfilledSections[j]);
            }
            System.out.println("");
        }
    }

    public static void main(String[] args){
        javaParser j = new javaParser();
        j.printClasses();
    }
}
class ToTake{
    public String title;
    public int frequency;
    public String[] fulfilledSections;
    public ToTake(String[] fs, String title, int size){
        this.fulfilledSections = fs;
        this.title = title;
        this.frequency = size;
    }
}
class Course{
    public String courseName = "";
    public String courseNum = "";
    public String sectionName = "";
    public String abbrv = "";
    public int frequency = 0;
}

