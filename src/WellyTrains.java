import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import ecs100.UI;

public class WellyTrains {

  private final HashMap<String, Station> stations = new HashMap<>();
  private final HashMap<String, TrainLine> trainLines = new HashMap<>();
  private final HashMap<Integer, Double> fares = new HashMap<>();

  private JButton timeButton;
  private JButton currentStationBtn;
  private JButton destStationBtn;
  private JButton listAllLinesBtn;

  private double pressedX = 0;
  private double pressedY = 0;

  private String currentStationName = "Wellington";
  private String destinationStationName = "Waikanae";
  private String currentTime = "1530";

  public void setupUserInterface() {
    UI.initialise();
    UI.setWindowSize(1000, 750);
    UI.addButton("Clear", () -> {
      this.resetScreen();
    });

    UI.addButton("All Stations in Region", this::showAllStations);
    UI.addButton("All Train Lines in Region", this::showAllTrainLines);
    UI.addButton("Show All Info For All Train Lines", this::showAllInfoAllLines);

    UI.addButton("", null);

    timeButton = UI.addButton("Time: " + this.currentTime, this::setCurrentTime);
    currentStationBtn = UI.addButton("Current Station: " + currentStationName, this::setCurrentStation);
    destStationBtn = UI.addButton("Destination Stn: " + this.destinationStationName, this::setDestinationStation);

    listAllLinesBtn = UI.addButton("List All Lines through " + this.currentStationName,
        this::printSimpleLinesForStation);

    // UI.addButton("Print lines and stations on line through current station",
    // this::this::listAllLinesThroughStation);

    UI.addButton("Find a route from Current to Destination", this::findARoute);

    this.resetScreen();
    UI.setMouseListener(this::mouseListener);
  }

  public void resetGraphicsPane() {
    UI.clearGraphics();
    UI.drawImage("data/system-map.png", 0, 0);
  }

  public void setCurrentTime() {

    // Ask the user for time input
    final String timeInput = JOptionPane.showInputDialog(null, "Enter the time in format HHMM", "1530",
        JOptionPane.PLAIN_MESSAGE);
    this.currentTime = timeInput;

    // Validate the time input
    // It will be in string HHMM form but it should still be a valid time
    // E.g. 1530 is valid but 2560 is not

    // Here are a few checks but I am sure there are more

    if (timeInput.length() != 4) { // stops 124
      UI.println("Invalid time");
      return;
    }

    if (timeInput.charAt(2) > '5') { // stops 2360
      UI.println("Invalid time");
      return;
    }

    if (timeInput.charAt(0) > '2') { // stops 3230
      UI.println("Invalid time");
      return;
    }

    if (timeInput.charAt(0) == '2' && timeInput.charAt(1) > '3') { // stops 2400
      UI.println("Invalid time");
      return;
    }

    timeButton.setText("Time: " + timeInput.toString());
    // UI.printMessage("You entered time: " + time);
  }

  public void timeInputListener(final String time) {
    // UI.println(time);
    this.currentTime = time;
  }

  /*
   * Tries to find a route between two stations
   */
  public void findARoute() {
    boolean onSameLine = false;

    UI.println("Searching for route from "
        + currentStationName +
        " to " + destinationStationName +
        " from " + currentTime + "...");

    // Find out if they share a line

    Station currentStation = stations.get(currentStationName);
    Station destinationStation = stations.get(destinationStationName);

    Set<TrainLine> linesForStation1 = stations.get(currentStationName).getTrainLines();
    Set<TrainLine> linesForStation2 = stations.get(destinationStationName).getTrainLines();

    Set<TrainLine> linesInCommon = new HashSet<>();

    for (TrainLine trainLineThroughStation1 : linesForStation1) {
      if (linesForStation2.contains(trainLineThroughStation1)) {
        onSameLine = true;
        linesInCommon.add(trainLineThroughStation1);
      }
    }

    if (onSameLine) {

      for (TrainLine trainLine : linesInCommon) {
        // UI.println(trainLine.toString());

        int currentStationIdx = trainLine.getStations().indexOf(currentStation);
        int destinationStationIdx = trainLine.getStations().indexOf(destinationStation);

        // TODO: handle behaviour here
        // if (destinationStationIdx == -1) {
        // UI.println("This train does not stop at " + destinationStationName);
        // // break;
        // // skip this line and try the next line
        // }

        if (currentStationIdx < destinationStationIdx) {
          // UI.println("You are travelling in the right direction");

          // find a service at or after the currentTime

          for (TrainService trainService : trainLine.getTrainServices()) {

            if (trainService.getTimes().get(destinationStationIdx) > -1) {
              // UI.println("Found a service that stops at the destination station");

              if (trainService.getTimes().get(currentStationIdx) >= Integer.parseInt(currentTime)) {
                // UI.println("Found a service at or after the current time");

                UI.println("Leaves " + currentStationName + " at " + trainService.getTimes().get(currentStationIdx));
                UI.println("Arrives " + destinationStationName + " at "
                    + String.valueOf(trainService.getTimes().get(destinationStationIdx)));

                int zoneDifference = destinationStation.getZone() - currentStation.getZone();
                if (zoneDifference <= 0) {
                  zoneDifference = 1;
                }
                double fare = fares.get(zoneDifference);

                UI.printf("Fare: $%.2f (%d zones)\n", fare, zoneDifference);

                break;
              }

            } else {
              // UI.println("This train does not stop at " + destinationStationName);
            }

          }

        } else {
          // UI.println("You are travelling in the wrong direction");
        }
      }
    }

    // TODO: handle if there is a -1 on the stattion!
    // then the train is not stopping at that station

    if (!onSameLine) {
      UI.println("No match. You will have to transfer lines");

      // TODO: " current_station.line + " to " + destination_station.line
    }

  }

  public void displayStationsInStatusBar() {
    UI.printMessage("Current: " + currentStationName + " | " + " Destination: " + destinationStationName);
  }

  public void listAllLinesThroughStation() {
    this.printLinesForStation(currentStationName);
  }

  public void resetScreen() {
    UI.clearText();
    UI.clearGraphics();
    UI.drawImage("data/system-map.png", 0, 0);
  }

  // TODO: fix data for woodsite and matarawa

  public void mouseListener(final String action, final double x, final double y) {

    if ("pressed".equals(action)) {
      pressedX = x;
      pressedY = y;
    }

    if ("released".equals(action)) {

      // UI.println(pressedX + " " + pressedY + " " + x + " " + y);

      // UI.printMessage("(x > " + pressedX + ") && (x < " + x + ") && (y > " +
      // pressedY + ") && (y < " + y + ")");

      if ((x > 16.0) && (x < 159.0) && (y > 417.0) && (y < 434.0)) {
        // UI.print(trainLines.get("Johnsonville_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Johnsonville_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Wellington_Johnsonville"));

      }
      if ((x > 123.0) && (x < 266.0) && (y > 284.0) && (y < 301.0)) {
        this.printStationsOnTrainLine(trainLines.get("Waikanae_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Wellington_Waikanae"));
      }

      if ((x > 238.0) && (x < 381.0) && (y > 251.0) && (y < 268.0)) {
        this.printStationsOnTrainLine(trainLines.get("Upper-Hutt_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Wellington_Upper-Hutt"));
      }

      if ((x > 256.0) && (x < 372.0) && (y > 428.0) && (y < 446.0)) {
        this.printStationsOnTrainLine(trainLines.get("Melling_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Wellington_Melling"));

      }

      if ((x > 308.0) && (x < 484.0) && (y > 16.0) && (y < 36.0)) {
        this.printStationsOnTrainLine(trainLines.get("Masterton_Wellington"));
        this.printStationsOnTrainLine(trainLines.get("Wellington_Masterton"));
      }

      this.detectClickOnStation(x, y);
    }
  }

  /*
   * Detect if the user has clicked on a station
   * by searching through the stations and checking if the x and y coordinates
   */
  public void detectClickOnStation(double x, double y) {
    stations.forEach((k, v) -> {

      if ((x > v.getLeftEdge()) && (x < v.getRightEdge()) && (y > v.getTopEdge()) && (y < v.getBottomEdge())) {
        UI.println("Clicked on " + v.getName());
        this.printLinesForStation(v.getName());

        this.resetGraphicsPane();
        this.currentStationName = v.getName();
        currentStationBtn.setText("Current Station: " + currentStationName);
        drawBoxAroundStationName(v.getName());
        UI.drawRect(v.getLeftEdge(), v.getTopEdge(), v.calculateWidth(), v.calculateHeight());
      }
    });
  }

  public void drawBoxAroundStationName(String stationName) {
    this.resetGraphicsPane();
    Station s = stations.get(stationName);
    UI.drawRect(s.getLeftEdge(), s.getTopEdge(), s.calculateWidth(), s.calculateHeight());
  }

  public void printStationsOnTrainLine(final TrainLine trainLine) {
    UI.println("Stations on the " + trainLine.getName() + " line");
    trainLine.getStations().forEach(station -> {
      UI.println("  " + station.getName());
    });
    UI.println();
  }

  public void printInstructions() {
    UI.println("Click on the train line header to list stations on that line");
  }

  public void printSimpleLinesForStation() {
    UI.println("-- Showing lines through " + currentStationName + " station --");
    final Station s = stations.get(currentStationName);
    for (final TrainLine tl : s.getTrainLines()) {
      UI.println(tl.getName());
    }
  }

  public void printLinesForStation(final String stationName) {
    final Station s = stations.get(stationName);
    UI.println("Printing Lines that go through " + stationName + " station");
    UI.println("In Zone " + s.getZone());
    UI.println("On Lines");
    for (final TrainLine tl : s.getTrainLines()) {
      UI.println("- " + tl.getName());
      for (int i = 0; i < tl.getStations().size(); i++) {
        UI.print("   " + i + ". ");

        UI.print(tl.getStations().get(i).getName());

        if (tl.getStations().get(i).getName().equals(s.getName())) {
          UI.println(" ***");
        } else {
          UI.println();
        }
      }
    }
  }

  /*
   * Reads the fares.data file and loads the data into the fares HashMap
   *
   * Format:
   *
   * zone fare
   * 1 2.50
   * 2 4.00
   */
  public void loadFares() {

    Scanner sc;
    try {
      sc = new Scanner(new File("data/fares.data"));

      sc.nextLine(); // skip the header lne

      while (sc.hasNextLine()) {
        if (!sc.hasNext()) {
          break;
        }
        int zone = sc.nextInt();
        double fare = sc.nextDouble();
        fares.put(zone, fare);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /*
   * Runs at the start of the program to load all the data
   * into the HashMaps
   */
  public void loadAllData() {
    this.loadStationData();
    this.loadTrainLineData();
    this.loadServiceData();
    this.loadStationCoordinates();
    this.loadFares();
  }

  /*
   * Prints all the info for all the lines
   */
  public void showAllInfoAllLines() {
    trainLines.forEach((k, v) -> {
      UI.println(k);
      for (final TrainService service : v.getTrainServices()) {
        UI.println("ID: " + service.getTrainID());
        for (final Integer time : service.getTimes()) {
          UI.println(time);
        }
        // UI.println("-end of service-");
      }
    });
    UI.println("====");
    UI.println();
  }

  /*
   * Reads the station-coords.data file and loads the data into the stations
   * I have added extra fields in the Station class to accomodate this data
   */
  public void loadStationCoordinates() {
    String dataFile = "data/station-coords.data";
    Scanner sc;

    try {
      sc = new Scanner(new File(dataFile));

      while (sc.hasNextLine()) {
        if (!sc.hasNext()) {
          break;
        }
        String name = sc.next();
        double topLeft = sc.nextDouble();
        double topRight = sc.nextDouble();
        double bottomLeft = sc.nextDouble();
        double bottomRight = sc.nextDouble();

        // UI.println("Here!");

        // UI.println(name);
        // UI.println(topLeft + " " + topRight + " " + bottomLeft + " " + bottomRight);
        // UI.sleep(500);
        stations.get(name).setCoords(topLeft, topRight, bottomLeft, bottomRight);
        // UI.println(name + " " + " zone: " + zone + " / dist: " + distance);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    UI.println("Station Coordinates loaded");
  }

  /*
   * Open the -services.data file for each line and build that data into the train
   * line
   *
   * E.g. Masterton_Wellington-services.data
   *
   *
   * 550 556 602 604 607 610 612 614 616 619 621 623 626 628 630 633 635
   *
   * 608 614 620 622 625 628 630 632 634 637 639 641 644 646 648 651 653
   */
  public void loadServiceData() {

    trainLines.forEach((k, v) -> {
      try {

        final Scanner sc = new Scanner(new File("data/" + k + "-services.data"));
        final TrainLine tl = trainLines.get(k);

        TrainService tService;

        while (sc.hasNextLine()) {
          tService = new TrainService(v);
          final String line = sc.nextLine();
          final String[] times = line.split(" ");
          // UI.println("line " + line);
          boolean isFirstStop = false;
          for (int i = 0; i < times.length; i++) {

            // public void addTime(int time, boolean firstStop){

            if (i == 0) {
              isFirstStop = true;
            } else {
              isFirstStop = false;
            }
            tService.addTime(Integer.parseInt(times[i]), isFirstStop);
            // tService.addTime(Integer.parseInt(times[i]), !(i == 0));
          }
          tl.addTrainService(tService);

        }

      } catch (final FileNotFoundException e) {
        e.printStackTrace();
      }

    });
  }

  public void showAllTrainLines() {
    UI.println("All Train Lines");
    UI.println("---");
    trainLines.forEach((k, v) -> {
      UI.println(v.getName());
    });
  }

  public void showAllStations() {
    UI.println("All Stations");
    UI.println("---");
    stations.forEach((k, v) -> {
      UI.println(v.getName() + " in zone " + v.getZone());
    });
  }

  public void loadStationData() {
    final String dataFile = "data/stations.data";
    Scanner sc;

    try {
      sc = new Scanner(new File(dataFile));

      while (sc.hasNextLine()) {
        if (!sc.hasNext()) {
          break;
        }
        final String name = sc.next();
        final int zone = sc.nextInt();
        final double distance = sc.nextDouble();
        stations.put(name, new Station(name, zone, distance));
      }

    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
    UI.println("Stations loaded");

  }

  public void loadTrainLineData() {
    final String dataFile = "data/train-lines.data";
    Scanner sc;

    try {
      sc = new Scanner(new File(dataFile));

      while (sc.hasNextLine()) {
        if (!sc.hasNext()) {
          break;
        }
        final String name = sc.next();
        trainLines.put(name, new TrainLine(name));
      }

    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
    UI.println("Train Lines Loaded");

    this.loadStationsOnLine();
  }

  public void loadStationsOnLine() {
    trainLines.forEach((trainLineKey, trainLineVal) -> {
      Scanner sc;
      try {
        sc = new Scanner(new File("data/" + trainLineKey + "-stations.data"));
        while (sc.hasNext()) {

          // 1. add station to the trainLine
          final TrainLine trainLine = trainLines.get(trainLineKey);
          final String stationName = sc.next();
          final Station s = stations.get(stationName);
          trainLine.addStation(s);

          s.addTrainLine(trainLine);

          // Station station = stations.get(trainLineVal);
          // station.addTrainLine(trainLineVal);

          // UI.print("**" + trainLineKey + "** ");
          // UI.println(sc.next());
        }
      } catch (final FileNotFoundException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Ask the user for a station name and assign it to the currentStationName field
   * Must pass a collection of the names of the stations to getOptionFromList
   */
  public void setCurrentStation() {
    final String name = getOptionFromList("Choose current station", stations.keySet());
    if (name == null) {
      return;
    }
    currentStationName = name;
    currentStationBtn.setText("Current Station: " + currentStationName);
    listAllLinesBtn.setText("List All Lines through " + this.currentStationName);
    drawBoxAroundStationName(currentStationName);
  }

  /**
   * Ask the user for a destination station name and assign it to the
   * destinationName field Must pass a collection of the names of the stations to
   * getOptionFromList
   */
  public void setDestinationStation() {
    final String name = getOptionFromList("Choose destination station", stations.keySet());
    if (name == null) {
      return;
    }
    UI.println("Setting destination station to " + name);
    destinationStationName = name;
    destStationBtn.setText("Destination Station: " + destinationStationName);
    // displayCurrentValues();
    this.displayStationsInStatusBar();

  }

  //
  /**
   * Not my code.
   *
   * Method to get a string from a dialog box with a list of options
   * Written by VUW faculty for COMP103 assignment 2023
   *
   * Just used for choosing station without typing.
   *
   * Can also select current station from UI map (Andy's code)
   */
  public String getOptionFromList(final String question, final Collection<String> options) {
    final Object[] possibilities = options.toArray();
    Arrays.sort(possibilities);
    return (String) javax.swing.JOptionPane.showInputDialog(UI.getFrame(), question, "",
        javax.swing.JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0].toString());
  }

  public static void main(final String[] args) {
    WellyTrains welllyTrains = new WellyTrains();
    welllyTrains.setupUserInterface();
    welllyTrains.loadAllData();
  }

}