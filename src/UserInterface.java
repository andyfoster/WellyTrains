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

public class UserInterface {

  private final HashMap<String, Station> stations = new HashMap<>();
  private final HashMap<String, TrainLine> trainLines = new HashMap<>();
  private final HashMap<Integer, Double> fares = new HashMap<>();

  private final JButton timeButton;
  private final JButton currentStationBtn;
  private final JButton destStationBtn;
  private final JButton listAllLinesBtn;

  private double pressedX = 0;
  private double pressedY = 0;

  private String currentStationName = "Wellington";
  private String destinationStationName = "Waikanae";
  private String currentTime = "1530";

  public UserInterface() {
    UI.initialise();
    UI.setWindowSize(1000, 750);
    UI.addButton("Clear", () -> {
      this.resetScreen();
    });

    UI.addButton("Load fares", this::loadFares);

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

    this.loadAllData();
    this.resetScreen();
    UI.setMouseListener(this::mouseListener);
  }

  public void setCurrentTime() {

    // Ask the user for time input
    final String timeInput = JOptionPane.showInputDialog(null, "Enter the time in format HHMM", "1530",
        JOptionPane.PLAIN_MESSAGE);
    this.currentTime = timeInput;

    // Validate the time input

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

        if (currentStationIdx < destinationStationIdx) {
          // UI.println("You are travelling in the right direction");

          // find a service at or after the currentTime

          for (TrainService trainService : trainLine.getTrainServices()) {

            if (trainService.getTimes().get(currentStationIdx) >= Integer.parseInt(currentTime)) {
              // UI.println("Found a service at or after the current time");

              UI.println("Leaves " + currentStationName + " at " + trainService.getTimes().get(currentStationIdx));
              UI.println("Arrives " + destinationStationName + " at "
                  + trainService.getTimes().get(destinationStationIdx));
              break;
            }
          }

        } else {
          // UI.println("You are travelling in the wrong direction");
        }
      }
    }

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

      UI.println(pressedX + " " + pressedY + " " + x + " " + y);

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
    UI.println("detectClickOnStation");
    // UI.println(x + " " + y);
    // UI.println("detectClickOnStation");

    stations.forEach((k, v) -> {

      if ((x > v.getLeftEdge()) && (x < v.getRightEdge()) && (y > v.getTopEdge()) && (y < v.getBottomEdge())) {
        UI.println("Clicked on " + v.getName());
        this.printLinesForStation(v.getName());

        UI.drawRect(v.getLeftEdge(), v.getTopEdge(), v.calculateWidth(), v.calculateHeight());
      }

    });

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

    fares.forEach((k, v) -> {
      UI.println("zone: " + k + " $" + v);
    });

  }

  public void loadAllData() {
    this.loadStationData();
    this.loadTrainLineData();
    this.loadServiceData();
    this.loadStationCoordinates();
  }

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
        // UI.println(name + " " + " zone: " + zone + " / dist: " + distance);
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
    // Popup
    // UI.println("Setting current station to " + name);
    currentStationName = name;
    currentStationBtn.setText("Current Station: " + currentStationName);
    listAllLinesBtn.setText("List All Lines through " + this.currentStationName);
    // displayCurrentValues();
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
   * Method to get a string from a dialog box with a list of options
   *
   * Method written by VUW faculty for COMP102 assignment
   *
   * Just used for choosing station without typing.
   *
   * Can also select station from UI map (my code)
   */
  public String getOptionFromList(final String question, final Collection<String> options) {
    final Object[] possibilities = options.toArray();
    Arrays.sort(possibilities);
    return (String) javax.swing.JOptionPane.showInputDialog(UI.getFrame(), question, "",
        javax.swing.JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0].toString());
  }

  public static void main(final String[] args) {
    new UserInterface();
  }

}
