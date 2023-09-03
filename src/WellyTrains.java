import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

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
  private JButton findRouteBtn;

  private double pressedX = 0;
  private double pressedY = 0;

  private String currentStationName = "Wellington";
  private String destinationStationName = "Waikanae";
  private String currentTime = "1530";

  public void setupUserInterface() {
    UI.initialise();
    UI.setWindowSize(1000, 750);
    UI.addButton("Clear", this::resetScreen);

    UI.addButton("All Stations in Region", this::showAllStations);
    UI.addButton("All Train Lines in Region", this::showAllTrainLines);
    // UI.addButton("Show All Info For All Train Lines", this::showAllInfoAllLines);

    UI.addButton("", null);

    timeButton = UI.addButton("Time: " + this.currentTime, this::setCurrentTime);
    currentStationBtn = UI.addButton("Current Station: " + currentStationName, this::setCurrentStation);
    destStationBtn = UI.addButton("Destination Stn: " + this.destinationStationName, this::setDestinationStation);

    listAllLinesBtn = UI.addButton("List All Lines through " + this.currentStationName,
        this::printSimpleLinesForStation);

    findRouteBtn = UI.addButton("Find a route from " + currentStationName + " to " + destinationStationName,
        this::findARoute);

    UI.addButton("Next services from current station", this::findNextServiceOnEachLine);

    this.resetScreen();
    this.printInstructions();
    UI.setMouseListener(this::mouseListener);
  }

  public void resetGraphicsPane() {
    UI.clearGraphics();
    UI.drawImage("data/system-map.png", 0, 0);
  }

  public void setCurrentTime() {

    // Ask the user for time input
    final String timeInput = JOptionPane.showInputDialog(null, "Enter the time in format HHMM", "Enter Time",
        JOptionPane.PLAIN_MESSAGE);
    this.currentTime = timeInput;

    // Validate the time input
    // It will be in string HHMM form but it should still be a valid time
    // E.g. 1530 is valid but 2560 is not
    // Here are a few checks but I am sure there are more

    if (timeInput.length() != 4) { // stops 124
      UI.println("Invalid time");
      this.setCurrentTime();
      return;
    }

    if (timeInput.charAt(2) > '5') { // stops 2360
      UI.println("Invalid time");
      this.setCurrentTime();
      return;
    }

    if (timeInput.charAt(0) > '2') { // stops 3230
      UI.println("Invalid time");
      this.setCurrentTime();
      return;
    }

    if (timeInput.charAt(0) == '2' && timeInput.charAt(1) > '3') { // stops 2400
      UI.println("Invalid time");
      this.setCurrentTime();
      return;
    }

    timeButton.setText("Time: " + timeInput.toString());
    // UI.printMessage("You entered time: " + time);
  }

  /*
   * Find the next train service for each line at the current station immediately
   * after a
   * user specified time
   */
  public void findNextServiceOnEachLine() {

    UI.println("-- Showing next time on each line through " + currentStationName + " station --");
    final Station s = stations.get(currentStationName);
    for (final TrainLine tl : s.getTrainLines()) {
      int currentIndex = tl.getStations().indexOf(s);
      int nextServiceTime = -1;
      for (final TrainService service : tl.getTrainServices()) {
        int currentTimeInt = Integer.parseInt(currentTime);
        int currentServiceTime = service.getTimes().get(currentIndex);
        if (currentServiceTime > currentTimeInt) {
          nextServiceTime = currentServiceTime;
          break;
        }
      }
      UI.println(tl.getName() + ": " + addColonToTime(nextServiceTime));
    }
  }

  /*
   * Adds a colon to a time in the format HHMM to HH:MM
   */
  public String addColonToTime(int time) {
    String timeStr = Integer.toString(time);
    return timeStr.substring(0, 2) + ":" + timeStr.substring(2, 4);
  }

  /**
   * Finds a route between two stations.
   */
  public void findARoute() {
    boolean onSameLine = false;
    Station currentStation = stations.get(currentStationName);
    Station destinationStation = stations.get(destinationStationName);
    Set<TrainLine> linesInCommon = new HashSet<>();

    // Print search information
    UI.printf("Searching for route from %s to %s from %s...\n",
        currentStationName, destinationStationName, currentTime);

    // Check if the stations are on the same line
    Set<TrainLine> linesForCurrentStation = currentStation.getTrainLines();
    Set<TrainLine> linesForDestinationStation = destinationStation.getTrainLines();
    for (TrainLine trainLine : linesForCurrentStation) {
      if (linesForDestinationStation.contains(trainLine)) {
        onSameLine = true;
        linesInCommon.add(trainLine);
      }
    }

    // If the stations are on the same line, find the route
    if (onSameLine) {
      findDirectRoute(linesInCommon, currentStation, destinationStation);
    } else {
      UI.println("No match. You will have to transfer lines");
      // TODO: Implement logic for transferring lines
    }
  }

  /**
   * Finds a direct route between two stations on the same line.
   */
  private void findDirectRoute(Set<TrainLine> linesInCommon,
      Station currentStation,
      Station destinationStation) {
    for (TrainLine trainLine : linesInCommon) {
      int currentIndex = trainLine.getStations().indexOf(currentStation);
      int destinationIndex = trainLine.getStations().indexOf(destinationStation);

      if (currentIndex < destinationIndex) {
        findServiceForStations(trainLine, currentIndex, destinationIndex);
      } else {
        // TODO: Implement logic for changing lines if necessary
      }
    }
  }

  /**
   * Finds the train service for two stations on the same line.
   */
  private void findServiceForStations(TrainLine trainLine,
      int currentIndex,
      int destinationIndex) {
    for (TrainService service : trainLine.getTrainServices()) {
      int currentTimeInt = Integer.parseInt(currentTime);
      int currentServiceTime = service.getTimes().get(currentIndex);
      int destinationServiceTime = service.getTimes().get(destinationIndex);

      // if there are no more services for the day, tell the user
      if (currentServiceTime == -1 || destinationServiceTime == -1) {
        UI.println("No more services for the day");
        // break;
      }

      if (destinationServiceTime > -1 && currentServiceTime >= currentTimeInt) {
        printRouteInfo(currentServiceTime, destinationServiceTime);
        break;
      }
    }
  }

  /**
   * Prints the route information including time and fare.
   */
  private void printRouteInfo(int currentServiceTime, int destinationServiceTime) {
    UI.printf("Leaves %s at %s\n", currentStationName, addColonToTime(currentServiceTime));
    UI.printf("Arrives %s at %d\n", destinationStationName, addColonToTime(destinationServiceTime));

    Station currentStation = stations.get(currentStationName);
    Station destinationStation = stations.get(destinationStationName);
    int zoneDifference = Math.max(1, destinationStation.getZone() - currentStation.getZone());
    double fare = fares.get(zoneDifference);

    UI.printf("Fare: $%.2f (%d zones)\n", fare, zoneDifference);
  }

  public void listAllLinesThroughStation() {
    this.printLinesForStation(currentStationName);
  }

  public void resetScreen() {
    UI.clearText();
    UI.clearGraphics();
    UI.drawImage("data/system-map.png", 0, 0);
  }

  /*
   * Detects if the user has clicked on a train line header or station
   */
  public void mouseListener(final String action, final double x, final double y) {

    if ("pressed".equals(action)) {
      pressedX = x;
      pressedY = y;
    }

    if ("released".equals(action)) {

      // Code for getting the coordinates of the mouse click for the map
      // UI.println(pressedX + " " + pressedY + " " + x + " " + y);

      // UI.printMessage("(x > " + pressedX + ") && (x < " + x + ") && (y > " +
      // pressedY + ") && (y < " + y + ")");

      /*
       * Detect if the user has clicked on a train line
       */
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
        updateButtons();
        // currentStationBtn.setText("Current Station: " + currentStationName);
        // listAllLinesBtn.setText("List All Lines through " + currentStationName);
        drawBoxAroundStationName(v.getName());
        UI.drawRect(v.getLeftEdge(), v.getTopEdge(), v.calculateWidth(), v.calculateHeight());
      }
    });
  }

  /*
   * Updates the text on the buttons to reflect the current station and
   * destination
   */
  public void updateButtons() {
    currentStationBtn.setText("Current Station: " + currentStationName);
    destStationBtn.setText("Destination Stn: " + this.destinationStationName);
    listAllLinesBtn.setText("List All Lines through " + currentStationName);
    findRouteBtn.setText("Find a route from " + currentStationName + " to " + destinationStationName);
  }

  /*
   * Draws a box around the station name on the map
   */
  public void drawBoxAroundStationName(String stationName) {
    this.resetGraphicsPane();
    Station s = stations.get(stationName);
    UI.drawRect(s.getLeftEdge(), s.getTopEdge(), s.calculateWidth(), s.calculateHeight());
  }

  /*
   * Prints all the stations on a train line
   */
  public void printStationsOnTrainLine(final TrainLine trainLine) {
    UI.println("Stations on the " + trainLine.getName() + " line");
    trainLine.getStations().forEach(station -> {
      UI.println("  " + station.getName());
    });
    UI.println();
  }

  /*
   * Prints the instructions for the user
   */
  public void printInstructions() {
    UI.println("Click on the train line header to list stations on that line");
  }

  /*
   * Prints the lines that go through the current station
   */
  public void printSimpleLinesForStation() {
    UI.println("-- Showing lines through " + currentStationName + " station --");
    final Station s = stations.get(currentStationName);
    for (final TrainLine tl : s.getTrainLines()) {
      UI.println(tl.getName());
    }
  }

  /*
   * Prints the lines that go through the current station
   * and the stations on those lines
   */
  public void printLinesForStation(final String stationName) {
    final Station s = stations.get(stationName);
    UI.println("Printing Lines that go through " + stationName + " station");
    // UI.println("In Zone " + s.getZone());
    UI.println("On Lines");
    for (final TrainLine tl : s.getTrainLines()) {
      UI.println("- " + tl.getName());
      // for (int i = 0; i < tl.getStations().size(); i++) {
      // UI.print(" " + i + ". ");

      // UI.print(tl.getStations().get(i).getName());

      // if (tl.getStations().get(i).getName().equals(s.getName())) {
      // UI.println(" ***");
      // } else {
      // UI.println();
      // }
      // }
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
          boolean isFirstStop = false;
          for (int i = 0; i < times.length; i++) {

            if (i == 0) {
              isFirstStop = true;
            } else {
              isFirstStop = false;
            }
            tService.addTime(Integer.parseInt(times[i]), isFirstStop);
          }
          tl.addTrainService(tService);
        }
      } catch (final FileNotFoundException e) {
        e.printStackTrace();
      }
    });
  }

  /*
   * List all train lines in the trainLines hashmap
   * in a nice format
   */
  public void showAllTrainLines() {
    UI.println("All Train Lines");
    UI.println("---");
    trainLines.forEach((k, v) -> {
      UI.println(v.getName().replace("_", " to ").replace("-", " ") + " line");
    });
  }

  /*
   * List all stations in the stations hashmap without the dash
   *
   */
  public void showAllStations() {
    UI.println("All Stations");
    UI.println("---");
    stations.forEach((k, v) -> {
      UI.println(v.getName().replace("-", " "));
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

  /*
   * Open the train-lines.data file and build that data into the trainLines
   */
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

  /*
   * Open the *-stations.data file for each line and build that data into the
   * train
   */
  public void loadStationsOnLine() {
    trainLines.forEach((trainLineKey, trainLineVal) -> {
      Scanner sc;
      try {
        sc = new Scanner(new File("data/" + trainLineKey + "-stations.data"));
        while (sc.hasNext()) {

          final TrainLine trainLine = trainLines.get(trainLineKey);
          final String stationName = sc.next();
          final Station s = stations.get(stationName);
          trainLine.addStation(s);

          s.addTrainLine(trainLine);
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
    this.currentStationName = name;
    updateButtons();
    drawBoxAroundStationName(this.currentStationName);
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
    // UI.println("Setting destination station to " + name);
    destinationStationName = name;
    updateButtons();
  }

  //
  /**
   * Not my code
   * Method to get a string from a dialog box with a list of options
   * Written by VUW faculty for COMP103 assignment 2023
   * Just used for choosing station without typing.
   * Can also select current station from UI map (Andy's code)
   */
  public String getOptionFromList(final String question, final Collection<String> options) {
    final Object[] possibilities = options.toArray();
    Arrays.sort(possibilities);
    return (String) javax.swing.JOptionPane.showInputDialog(UI.getFrame(), question, "",
        javax.swing.JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0].toString());
  }

  public static void main(final String[] args) {
    WellyTrains wellyTrains = new WellyTrains();
    wellyTrains.setupUserInterface();
    wellyTrains.loadAllData();
  }

}
