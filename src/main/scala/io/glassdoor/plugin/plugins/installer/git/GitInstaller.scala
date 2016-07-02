package io.glassdoor.plugin.plugins.installer.git

import java.io.{File, IOException}
import java.nio.file.{Files, Path}

import io.glassdoor.application._
import io.glassdoor.plugin.Plugin

import scala.collection.immutable.HashMap
import scala.sys.process._

/**
  * A plugin used to download and update data from Git repositories.
  * Could be used to update/receive resources or other plugins.
  * Created by Florian Schrofner on 4/15/16.
  */
class GitInstaller extends Plugin {

  var mResult:Option[Map[String,String]] = None

  override def apply(data:Map[String,String], parameters: Array[String]): Unit = {
    try {
      var repoUrl:Option[String] = None
      var path:Option[String] = None
      var keymapDescription:Option[String] = None

      if(parameters.length == 2) {
        repoUrl = Some(parameters(0))
        path = Some(parameters(1))
      } else if(parameters.length == 3) {
        keymapDescription = Some(parameters(0))
        repoUrl = Some(parameters(1))
        path = Some(parameters(2))
      } else {
        //TODO: send error message
        Log.debug("incorrect number of parameters!")
      }

      val executor = new SystemCommandExecutor

      if(repoUrl.isDefined && path.isDefined){
        showEndlessProgress()

        //TODO: if repository exists, just merge the newest commit from master
        val destinationDirectory = new File(path.get)

        if(destinationDirectory.exists && destinationDirectory.isDirectory && !isDirEmpty(destinationDirectory.toPath)){
          //TODO: just update the repository
          Log.debug("directory already exists, starting update..")
        } else {
          Log.debug("directory does not exist, initialising download..")
          destinationDirectory.mkdirs()

          val command = "git clone " + repoUrl.get + " " +  path.get
          executor.executeSystemCommand(command)
        }

        if(executor.lastCommandSuccessful){
          if(keymapDescription.isDefined){
            val result = HashMap[String,String](keymapDescription.get -> path.get)
            mResult = Some(result)
          }
        } else {
          mResult = None
        }
      }
    } catch {
      case e:ArrayIndexOutOfBoundsException =>
        mResult = None
    } finally {
      ready()
    }


  }

  def isDirEmpty(directory:Path):Boolean = {
    try {
      val dirStream = Files.newDirectoryStream(directory)
      !dirStream.iterator.hasNext
    } catch {
      case e:IOException =>
        true
    }

  }

  override def result: Option[Map[String,String]] = {
    mResult
  }

  override def help(parameters: Array[String]): Unit = ???
}
