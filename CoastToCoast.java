package Assingment3;

import org.jsoup.nodes.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import java.util.*;
import java.util.regex.*;

import java.io.IOException;

public class CoastToCoast {

    public ArrayList<String> getData() throws IOException {
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> url = new ArrayList<>();
        String year;
        for(int i = 1990; i < 2022; i++) {
            if(i <= 2019) {
                year = String.valueOf(i);
                urls.add("https://top40weekly.com/" + year + "-all-charts/");
            } else {
                //different url format for these pages > 2019
                year = String.valueOf(i);
                urls.add("https://top40weekly.com/all-us-top-40-singles-for-" + year + "/");
            }
        }
        //get the data from the page using helper method
        for(String page : urls) {
            url = helper(page);
        }
        return url;
    }

    // takes in the urls and puts all the artists into an array list called artists
    public ArrayList<String> helper(String url) throws IOException {
        Document document = Jsoup.connect(url).get();
        //get song line
        Elements content = document.select("div.entry-content.content");
        String[] first = content.toString().split("(?=<p>)");
        ArrayList<String> second = new ArrayList<>();
        for(int i = 0; i < first.length; i++) {
            second.addAll(Arrays.asList(first[i].split("(?=</p)")));
        }
        //remove <p>
        String regex = "[<][p][>]\\d";
        Pattern pattern = Pattern.compile(regex);
        ArrayList<String> removePs = new ArrayList<>();
        for(int j = 0; j < second.size(); j++) {
            Matcher match = pattern.matcher(second.get(j));
            if(match.find()) {
                removePs.add(second.get(j));
            }
        }
        //get artists name
        ArrayList<String> removeBreaks = new ArrayList<>();
        for(int j = 0; j < removePs.size(); j++) {
            String line = removePs.get(j).replace("–", "(");
            String [] artistArray = line.split("<br>");
            removeBreaks.addAll(Arrays.asList(artistArray));
        }
        //remove leftovers
        ArrayList<String> cleanUp = new ArrayList<>();
        for(int l = 0; l < removeBreaks.size(); l++) {
            String[] artistArray = removeBreaks.get(l).split("<p>");
            cleanUp.addAll(Arrays.asList(artistArray));
        }
        //just get artist name
        String delimiter = "[(•(] (.*?) [(]";
        pattern = Pattern.compile(delimiter);
        ArrayList<String> artists = new ArrayList<String>();
        for(String s : cleanUp) {
            Matcher matcher = pattern.matcher(s);
            if(matcher.find()) {
                artists.add(matcher.group(1));
            }
        }
        return artists;
    }

    public Hashtable<String, LinkedList<String>> createHash(ArrayList<String> artistList){
        //make a hash table to store an artist with their collaborators as a linked list
        Hashtable<String, LinkedList<String>> archive = new Hashtable<>();
        for(String name: artistList) {
            if(name.contains("featuring") || name.contains("Featuring") || name.contains("&amp;")) {
                //featuring
                if(name.contains("featuring")) {
                    // split the two names into their own strings
                    String[] hold = name.split("featuring");
                    archive = hashHelp(archive, hold);
                }
                //featuring ^ but make it caps
                else if(name.contains("Featuring")) {
                    // split the two names into their own strings
                    String[] hold = name.split("Featuring");
                    archive = hashHelp(archive, hold);
                }
                //& amp;
                else if(name.contains("&amp;")) {
                    // split the two names into their own strings
                    String[] hold = name.split("&amp;");
                    archive = hashHelp(archive, hold);
                }
            } else {
                //add if they don't already exist
                if(!archive.containsKey(name)) {
                    archive.put(name, new LinkedList<String>());
                }
            }
        }
        return archive;
    }

    public Hashtable<String, LinkedList<String>> hashHelp(Hashtable<String, LinkedList<String>> table, String[] hold) {
        ArrayList<String> collect = new ArrayList<>();
        for(String names: hold) {
            String cleaned = names.strip();
            collect.add(cleaned);
        }
        //for each name, add them
        for(String artist: collect) {
            //create new if needed
            if(!table.containsKey(artist)) {
                String otherArtist = "";
                for(int i = 0; i < collect.size(); i++) {
                    if(!collect.get(i).equals(artist)) {
                        otherArtist = collect.get(i);
                    }
                }
                //add to linked list
                table.put(artist, new LinkedList<String>(Arrays.asList(otherArtist)));
            }
            else {
                //if they already exist
                String otherArtist = "";
                for(int i = 0; i < collect.size(); i++) {
                    if(!collect.get(i).equals(artist)) {
                        otherArtist = collect.get(i);
                    }
                }
                //for if it already exists
                if(!table.get(artist).contains(otherArtist)) {
                    LinkedList<String> holdList = table.get(artist);
                    holdList.add(otherArtist);
                    table.put(artist, holdList);
                }
            }
        }
        return table;
    }

    public ArrayList<String> recomendation(String name, Hashtable<String, LinkedList<String>> collection){
        ArrayList<String> collabs = new ArrayList<String>();
        boolean changes = true;
        int count = 0;
        //see if they even exist
        if(collection.containsKey(name)) {
            collabs.addAll(collection.get(name));
            while(changes) {
                changes = false;
                String collab = " ";
                //to not stop getting items
                try {
                    collab = collabs.get(count);
                } catch (IndexOutOfBoundsException e) {
                    changes = false;
                    break;
                }
                if(collection.containsKey(collab)) {
                    ArrayList<String> hold = new ArrayList<String>(collection.get(collab));
                    changes = true;
                    for(String collItem: hold) {
                        if(!collabs.contains(collItem) && !collItem.equals(name)) {
                            collabs.add(collItem);
                            changes = true;
                        }
                    }
                }
                count++;
            }
        } else {
            System.out.println("Artist does not exist.");
        }
        return collabs;
    }

    public void fixPrint(String name) {
        if(name.contains("&amp;")) {
            String[] first = name.split("&amp;");
            for(String n : first) {
                System.out.println(n.strip());
            }
        } else if(name.contains(",")) {
            String[] first = name.split(",");
            for(String n : first) {
                System.out.println(n.strip());
            }
        } else if(name.contains(",") && name.contains("&amp;")) {
            String[] first = name.split(",");
            for(String n : first) {
                if(n.contains(",")) {
                    String[] second = name.split("&amp;");
                    for(String two: second) {
                        System.out.println(n.strip());
                    }
                }
            }
        } else {
            System.out.println(name);
        }
    }

    public static void main(String[] args) throws IOException {
        CoastToCoast coast = new CoastToCoast();
        Scanner scan = new Scanner(System.in);
        System.out.println("Who is your favourite artist? ");
        String userArtist = scan.nextLine();
        System.out.println("Creating a curated list just for you. Be back in a moment...");
        ArrayList<String> listOfArtists = coast.getData();
        Hashtable<String, LinkedList<String>> listAll = coast.createHash(listOfArtists);
        ArrayList<String> recommendations = coast.recomendation(userArtist, listAll);
        for (String r: recommendations){
            coast.fixPrint(r);
        }
    }
}
