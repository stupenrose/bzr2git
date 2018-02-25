package us.penrose.bzr2git

import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.File
import Util._

object Main extends App {
  val source = new File(args(0))
  val dest = new File(args(1))
  
  val proc = new ProcessBuilder()
              .directory(source)
              .command("/usr/local/bin/bzr", "log").start()
  
  val log = Bzr.parseEntries(toLines(proc.getInputStream, "UTF8"))
  
  if(dest.exists()) throw new Exception(dest.getAbsolutePath + " already exists!")
  
  log.reverse.zipWithIndex.foreach{case (log, idx)=>
    
    // GET BZR CHANGES
    if(idx == 0){
      watchExec(new ProcessBuilder()
              .command("/usr/local/bin/bzr", "branch", "-r", log.revno.toString(), source.getAbsolutePath, dest.getAbsolutePath))
              
      watchExec(new ProcessBuilder()
              .directory(dest)
              .command("git", "init", "."))
    }else{
      watchExec(new ProcessBuilder()
              .directory(dest)
              .command("/usr/local/bin/bzr", "pull", "-r", log.revno.toString()))
    }
    
    // ADD THEM TO GIT
    watchExec(new ProcessBuilder()
            .directory(dest)
            .command("git", "add", "--all"))
            
            
    watchExec(new ProcessBuilder()
            .directory(dest)
            .command("git", "rm", "-r", "--cached", ".bzr"))
  
    watchExec(new ProcessBuilder()
            .directory(dest)
            .command("git", "commit",
                "--date=" + log.timestamp,
                "--author=" + log.committer, 
                "-m", log.message))
    
  }
  
  
  println("done")
}