// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a SWEN502 assignment.
// You may not distribute it in any other way without permission.

// Code for SWEN502, Assignment W2

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Station Information about an individual station: - The name - The fare zone
 * it is in (1 - 14) - The distance from the hub station (Wellington) - The set
 * of TrainLines that go through that station. The constructor just takes the
 * name, zone and distance; TrainLines must then be added to the station, one by
 * one.
 */

public class Station {

  private String name;
  private int zone; // fare zone
  private double distance; // distance from Wellington
  private Set<TrainLine> trainLines = new HashSet<>();

  private double leftEdge;
  private double rightEdge;
  private double topEdge;
  private double bottomEdge;

  public Station(String name, int zone, double dist) {
    this.name = name;
    this.zone = zone;
    this.distance = dist;
  }

  public void setCoords(double leftEdge, double topEdge, double rightEdge, double bottomEdge) {
    this.leftEdge = leftEdge;
    this.rightEdge = rightEdge;
    this.topEdge = topEdge;
    this.bottomEdge = bottomEdge;
  }

  public double getLeftEdge() {
    return this.leftEdge;
  }

  public double getRightEdge() {
    return this.rightEdge;
  }

  public double getTopEdge() {
    return this.topEdge;
  }

  public double getBottomEdge() {
    return this.bottomEdge;
  }

  public boolean isExchange() {
    return this.trainLines.size() > 2;
  }

  public String getName() {
    return this.name;
  }

  public int getZone() {
    return this.zone;
  }

  public double calculateWidth() {
    return this.rightEdge - this.leftEdge;
  }

  public double calculateHeight() {
    return this.bottomEdge - this.topEdge;
  }

  /**
   * Add a TrainLine to the station
   */
  public void addTrainLine(TrainLine line) {
    trainLines.add(line);
  }

  public Set<TrainLine> getTrainLines() {
    return Collections.unmodifiableSet(trainLines); // Return an unmodifiable version of the set of train lines.
  }

  /**
   * toString is the station name plus zone, plus number of train lines
   */
  @Override
  public String toString() {
    return name + " (zone " + zone + ", " + trainLines.size() + " lines)";
  }

}
