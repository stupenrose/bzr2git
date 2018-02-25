package us.penrose.bzr2git

import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.File

object Util {
  
  
  def next(reader:BufferedReader):Stream[String] = {
    reader.readLine() match {
      case null => Stream.Empty
      case line => Stream.cons(line, next(reader))
    }
  }
  
  def toLines(is:InputStream, charSet:String) = {
    val reader = new BufferedReader(new InputStreamReader(is, charSet))
    next(reader)
  }
  
  def watchExec(pb:ProcessBuilder) = {
    import scala.collection.JavaConversions._
    println(pb.command().mkString(" "))
    watch(pb.start())
  }
  
  def watch(p:Process) = {
    new Thread(){
      override def run(){
        toLines(p.getInputStream, "UTF8").foreach(println)
      }
    }.start
    new Thread(){
      override def run(){
        toLines(p.getErrorStream, "UTF8").foreach(println)
      }
    }.start
    p.waitFor()
  }
}