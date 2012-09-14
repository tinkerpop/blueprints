package com.tinkerpop.blueprints.impls.datomic;

import com.tinkerpop.blueprints.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Illustrates the use of Datomic-graph with respect to it's temporal graph features
 *
 * @author Davy Suvee (http://datablend.be)
 */
public class TemporalGraphTest {

    // Create the datomic-based graph for holding the olympic data
    private DatomicGraph graph = new DatomicGraph("datomic:mem://olympics");

    private Map<String,Vertex> athletes = new HashMap<String, Vertex>();
    private Map<String,Vertex> disciplines = new HashMap<String, Vertex>();
    private Map<String,Vertex> countries = new HashMap<String, Vertex>();

    public void executeTemporalQueries() {
        // Get the current vertex version for Michael Phelps
        Vertex phelps = graph.getVertices("athlete_name", "Michael Phelps").iterator().next();

        // Retrieve the version of Michael Phelps that has exactly one medal
        Vertex phelpsWithOneMedal = ((DatomicVertex)phelps).getPreviousVersions(new TimeAwareFilter() {
            @Override
            public TimeAwareElement filter(TimeAwareElement element) {
                return (countMedals(element) == 1) ? element : null;
            }
        }).iterator().next();


        // Print the medal type information for the first medal of Michael Phelps
        Vertex medalHyperNode = phelpsWithOneMedal.getVertices(Direction.IN,"athlete").iterator().next();
        System.out.println("The first medal of Michael Phelps was : " + medalHyperNode.getVertices(Direction.OUT,"medal").iterator().next().getProperty("medal_type"));

        // Retrieve the number of medals the USA already won at that moment in time
        // Let's retrieve the USA node through Michael Phelps
        Vertex usa = phelpsWithOneMedal.getVertices(Direction.OUT, "country").iterator().next();

        // Retrieve all the athletes associated with the USA
        Iterator<Vertex> usaAthletes = usa.getVertices(Direction.IN, "country").iterator();
        int medalcount = 0;
        // Retrieves the medals for each of these athletes
        while (usaAthletes.hasNext()) {
            Iterator<Edge> medalHyperNodes = usaAthletes.next().getEdges(Direction.IN).iterator();
            while (medalHyperNodes.hasNext()) {
                medalHyperNodes.next();
                medalcount++;
            }
        }
        System.out.println("Number of medals for the USA at " + ((DatomicVertex)phelpsWithOneMedal).getTimeInterval().getStart() + " : " + medalcount + " medals");

    }

    // Imports the medal in the temporal graph database
    public void importData() throws IOException, InterruptedException {
        // Retrieve the medals
        List<Medal> medals = parseMedals();
        // Get the first date (gold, silver, bronze medals created at the date of the first attributed medal)
        graph.setTransactionTime(medals.get(0).getDate());

        long start = System.currentTimeMillis();

        // Create the golden, silver and bronze medal
        Vertex goldMedal = graph.addVertex(null);
        goldMedal.setProperty("medal_type","gold");
        Vertex silverMedal = graph.addVertex(null);
        silverMedal.setProperty("medal_type","silver");
        Vertex bronzeMedal = graph.addVertex(null);
        bronzeMedal.setProperty("medal_type","bronze");

        // Add the medals one by one, including the transaction time
        for (Medal medal : medals) {
            // Set the transaction time (time at which the elements will exist in the graph)
            graph.setTransactionTime(medal.getDate());
            Vertex person =  getOrCreateAthlete(medal.getPerson(), medal.getCountry());
            Vertex discipline = getOrCreateDiscipline(medal.getDiscipline(), medal.getCategory());
            Vertex hyperVertexForMedal = graph.addVertex(null);
            hyperVertexForMedal.setProperty("date",medal.getDate().getTime());
            if (medal.getType().equals("gold")) {
                graph.addEdge(null, hyperVertexForMedal, goldMedal, "medal");
            }
            if (medal.getType().equals("silver")) {
                graph.addEdge(null, hyperVertexForMedal, silverMedal, "medal");
            }
            if (medal.getType().equals("bronze")) {
                graph.addEdge(null, hyperVertexForMedal, bronzeMedal, "medal");
            }
            graph.addEdge(null, hyperVertexForMedal, person, "athlete");
            graph.addEdge(null, hyperVertexForMedal, discipline, "discipline");
        }
        long stop = System.currentTimeMillis();
        System.out.println("Imported " + medals.size() + " medals in " + (stop - start) + " ms.");
    }

    // Helper method for creating country vertices
    private Vertex getOrCreateCountry(String country) {
        if (countries.containsKey(country)) {
            return countries.get(country);
        }
        else {
            Vertex countryVertex = graph.addVertex(null);
            countryVertex.setProperty("country_name", country);
            countries.put(country, countryVertex);
            return countryVertex;
        }
    }

    // Helper method for creating category vertices
    private Vertex getOrCreateCategory(String category) {
        Iterator<Vertex> categoryIt = graph.getVertices("category_name", category).iterator();
        if (categoryIt.hasNext()) {
            return categoryIt.next();
        }
        else {
            Vertex categoryVertex = graph.addVertex(null);
            categoryVertex.setProperty("category_name", category);
            return categoryVertex;
        }
    }

    // Helper method for creating athlete vertices
    private Vertex getOrCreateAthlete(String athlete, String country) {
        if (athletes.containsKey(athlete)) {
            return athletes.get(athlete);
        }
        else {
            // Person does not yet exist, create and link to country
            Vertex athleteVertex = graph.addVertex(null);
            athleteVertex.setProperty("athlete_name", athlete);
            Vertex countryVertex = getOrCreateCountry(country);
            graph.addEdge(null,athleteVertex,countryVertex,"country");
            athletes.put(athlete, athleteVertex);
            return athleteVertex;
        }

    }

    // Helper method for creating discipline vertices
    private Vertex getOrCreateDiscipline(String discipline, String category) {
        if (disciplines.containsKey(discipline)) {
            return disciplines.get(discipline);
        }
        else {
            // Discipline does not yet exist, create and link to country
            Vertex disciplineVertex = graph.addVertex(null);
            disciplineVertex.setProperty("discipline_name", discipline);
            Vertex categoryVertex = getOrCreateCategory(category);
            graph.addEdge(null,disciplineVertex,categoryVertex,"category");
            disciplines.put(discipline, disciplineVertex);
            return disciplineVertex;
        }
    }

    private int countMedals(TimeAwareElement person) {
        Iterator<Edge> edges = ((TimeAwareVertex)person).getEdges(Direction.IN, "athlete").iterator();
        int count = 0;
        while (edges.hasNext()) {
            count++;
            edges.next();
        }
        return count;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        TemporalGraphTest test = new TemporalGraphTest();
        test.importData();
        test.executeTemporalQueries();
    }

    class Medal implements Comparable<Medal> {

        private String category;
        private String discipline;
        private String type;
        private String person;
        private String country;
        private Date date;

        public Medal(String category, String discipline, String type, String person, String country, Date date) {
            this.category = category;
            this.discipline = discipline;
            this.type = type;
            this.person = person;
            this.country = country;
            this.date = date;
        }

        public String getCategory() {
            return category;
        }


        public String getDiscipline() {
            return discipline;
        }

        public String getType() {
            return type;
        }

        public String getPerson() {
            return person;
        }

        public Date getDate() {
            return date;
        }

        public String getCountry() {
            return country;
        }

        @Override
        public int compareTo(Medal medal) {
            return this.date.compareTo(medal.getDate());
        }
    }

    public List<Medal> parseMedals() throws IOException {
        List<Medal> medals = new ArrayList<Medal>();
        File file = new File(getClass().getClassLoader().getResource("medals.txt").getFile());
        BufferedReader input =  new BufferedReader(new FileReader(file));

        String line = null;
        // Skip the header
        input.readLine();
        while ((line = input.readLine()) != null) {
            String[] splitted = line.split("\t");
            String[] sport = splitted[0].split("-");
            String category = sport[0].trim();
            String discipline = sport[1].trim();
            Date date = new Date(splitted[4]);
            medals.addAll(getMedal(getPersons(splitted[1]),category,discipline,date,"gold"));
            medals.addAll(getMedal(getPersons(splitted[2]),category,discipline,date,"silver"));
            medals.addAll(getMedal(getPersons(splitted[3]),category,discipline,date,"bronze"));
        }
        Collections.sort(medals);
        return medals;
    }

    public static String[] getPersons(String person) {
        return person.split(",");
    }

    public List<Medal> getMedal(String[] persons, String category, String discipline, Date date, String medal) {
        List<Medal> medals = new ArrayList<Medal>();
        for (String person : persons) {
            String[] split = person.split("\\(");
            medals.add(new Medal(category, discipline, medal,split[0].trim(), split[1].substring(0,split[1].length()-1), date));
        }
        return medals;
    }

}
