package us.penrose.bzr2git

import java.time.Instant
import java.time.LocalTime
import java.time.LocalDate
import java.time.OffsetTime
import java.time.ZoneOffset
import java.time.OffsetDateTime

object Bzr {
  
  private case class Accum(props:Map[String, String] = Map(), message:String = "")
  
  private def readCommitLog(accum:Accum, lines:Stream[String]):Stream[Accum] = {
    lines.headOption match {
      case None => Stream.cons(accum, Stream.Empty)
      case Some(line) => {
        if(line.startsWith("--------")){
          Stream.cons(accum, readCommitLog(Accum(), lines.tail))
        }else{
          val pattern = "([a-zA-Z ]*):(.*)".r
          line match {
            case pattern(key, value) => {
              readCommitLog(accum.copy(props = accum.props + (key -> value.trim())), lines.tail)
            }
            case _ => readCommitLog(accum.copy(message = accum.message + line + "\n"), lines.tail)
          }
        }
      }
    }
    
  }
  
  
  
  
  private def parseTime(text:String) = {
    println(text)
    val pattern = "[A-Z][a-z][a-z] ([0-9]*)-([0-9]*)-([0-9]*) ([0-9]*):([0-9]*):([0-9]*) (.*)".r
    val odt = text match {
      case pattern(year, month, day, hour, minute, second, offset) => {
        val time = OffsetTime.of(hour.toInt, minute.toInt, second.toInt, 0, ZoneOffset.ofHours(offset.toInt/100))
        val date = LocalDate.of(year.toInt, month.toInt, day.toInt)
        date.atTime(time)
      }
    }
    println(odt + " vs " + text)
    odt
  }
  
  case class BzrLogEntry(revno:Int, committer:String, timestamp:OffsetDateTime, message:String)
  
  private def parseEntry(logData:Accum):BzrLogEntry = {
    BzrLogEntry(
        timestamp = parseTime(logData.props("timestamp")),
        revno = logData.props("revno").toInt,
        committer = logData.props("committer"),
        message = logData.message)
  }
  
  
  def parseEntries(lines:Stream[String]) = readCommitLog(Accum(), lines.tail).map(parseEntry)
  
}