package com.knoldus

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}

object DriverMain extends App {

  // Create an instance of the GradeCalculator class
  private val gradeCalculator = new GradeCalculator

  // Specify the path to the CSV file to be parsed
  val pathOfCsv = "/home/knoldus/Desktop/FuturesAssignment/studentGrades.csv"

  // Parse the CSV file and print the parsed data or error message
  val parsedData = gradeCalculator.parseCsv(pathOfCsv)
  parsedData.onComplete {
    case Success(parsedData) =>
      // Print the parsed data
      parsedData.foreach(row => println(row))
    case Failure(exception) =>
      println(s"Failed to parse CSV: ${exception.getMessage}")
  }
  // Wait for the Future to complete
  Await.result(parsedData, Duration.Inf)

  // Calculate the student averages and print them or error message
  val studentAverages = gradeCalculator.calculateStudentAverages(parsedData)
  studentAverages.onComplete {
    case Success(averageResult) =>
      averageResult.foreach(average => {
        println(s"Student ${average._1} has an average of ${average._2}")
      })
    case Failure(exception) =>
      println(s"An exception occurred: ${exception.getMessage}")
  }
  // Wait for the Future to complete
  Await.result(studentAverages, Duration.Inf)

  // Calculate the class average and print it or error message
  private val classAverage = gradeCalculator.calculateClassAverage(studentAverages)
  classAverage.onComplete {
    case Success(avg) => println(s"Class average is $avg")
    case Failure(ex) => println(s"Failed to calculate class average: ${ex.getMessage}")
  }
  // Wait for the Future to complete
  Await.result(classAverage, Duration.Inf)

  // Calculate the grades for each student and print them or error message
  val grades = gradeCalculator.calculateGrades(pathOfCsv)
  grades.onComplete {
    case Success(result) =>
      println("Student grades:")
      result.foreach(grade => {
        println(s"Student ${grade._1} got ${grade._2}")
      })
    case Failure(ex) => println(s"Failed to calculate student grades: ${ex.getMessage}")
  }
  // Wait for the Future to complete
  Await.result(grades, Duration.Inf)
}
