package com.knoldus

import scala.io.Source
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GradeCalculator {

  // Method to parse CSV file and return a list of maps containing the data
  def parseCsv(pathOfCsv: String): Future[List[Map[String, String]]] = {
    Future {
      // Open file and read lines
      val bufferedSource = Source.fromFile(pathOfCsv)
      val rows = bufferedSource.getLines().toList
      bufferedSource.close()

      // Get header from first row
      val header = rows.take(1).flatMap(_.split(","))

      // Create list of maps from remaining rows, using header as keys
      rows.tail.map(row => {
        val values = row.split(",").toList
        header.zip(values).toMap
      })
    }.recoverWith {
      case exception: Exception => Future.failed(new Exception(s"Cannot read File"))
    }
  }

  // Method to calculate student averages from parsed data
  def calculateStudentAverages(parsedData: Future[List[Map[String, String]]]): Future[List[(String, Double)]] = {
    parsedData.map { data =>
      // Group data by student ID
      val groupedData = data.groupBy(_("StudentID"))

      // Calculate average for each student
      groupedData.map { case (id, rows) =>
        val grades = rows.map(_("English").toInt) ++ rows.map(_("Physics").toInt) ++
          rows.map(_("Chemistry").toInt) ++ rows.map(_("Maths").toInt)
        val average = grades.sum.toDouble / grades.length
        (id, average)
      }.toList
    } recoverWith {
      case exception: Exception => Future.failed(new Exception(s"Failed to calculate student averages "))
    }
  }

  // Method to calculate class average from student averages
  def calculateClassAverage(studentAverages: Future[List[(String, Double)]]): Future[Double] = {
    studentAverages.flatMap(averages =>
      if (averages.isEmpty) {
        Future.failed(new Exception("Cannot find any student average"))
      }
      else {
        val sum = averages.map(_._2).sum
        val count = averages.length
        Future.successful(sum / count)
      })
  }.recoverWith {
    case exception: Exception => Future.failed(new Exception(s"Cannot Calculate Average"))
  }

  // Method to calculate grades from CSV file
  def calculateGrades(pathOfCsv: String): Future[List[(String, String)]] = {
    for {
      parsedData <- parseCsv(pathOfCsv)
      studentAverages <- calculateStudentAverages(Future.successful(parsedData))
    } yield {
      // Calculate grade for each student based on their average
      studentAverages.map { case (id, average) =>
        val grade =
          if (average >= 90) "A"
          else if (average >= 80) "B"
          else if (average >= 70) "C"
          else if (average >= 60) "D"
          else if (average >= 50) "E"
          else "F"
        (id, grade)
      }
    }
  }.recoverWith {
    case exception: Exception => Future.failed(new Exception(s"Cannot Calculate Grades:"))
  }
}

