#!/usr/bin/env groovy

// This script parses the text and queries Twitter via API.

@Grab('org.twitter4j:twitter4j-core:4.0.6')
import twitter4j.*

import java.text.*
def twitter=TwitterFactory.singleton

def input=System.in.text

assert input

def lines=input.readLines()
def head=lines[0]

def body=lines.drop(1).join("\n")

def printStatus={Status stat->
  stat.user.with{
    println "@$screenName $name"
  }
  println ""
  println stat.text
  println ""
  def sdf=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z")
  println sdf.format(stat.createdAt)
  println "RT $stat.retweetCount, Fav $stat.favoriteCount"
}

def printStatusIterated={List<Status> stats->
  def index=0
  stats.each{tw->
    index++
    println "========== ($index/${stats.size()})"
    if(tw.retweetedStatus){
      printStatus tw.retweetedStatus
    }else if(tw.quotedStatus){
      printStatus tw
      println "~~~"
      printStatus tw.quotedStatus
    }else{
      printStatus tw
    }
  }
}

def bodyInLines=body.readLines()

switch(head.toLowerCase()){
////
case "auto message":
case "auto":
  System.exit(0)
  break
case "tweet":
  twitter.updateStatus(body)
  println "Tweeted"
  break
////
case "rentwi":
  def tweets=[""]
  bodyInLines.each{ln->
    if(ln.matches("%{3,}")){
      tweets.add("")
    }else{
      def last=tweets.last()
      if(last.trim()){
        tweets[tweets.size()-1]+="\n$ln"
      }else{
        tweets[tweets.size()-1]=ln
      }
    }
  }
  def finalTweets=tweets.collect{it.trim()}.findAll()
  def lastTweet=null
  def index=0
  finalTweets.each{pointer->
    try{
      index++
      println "Working for #$index"
      if(lastTweet){
        def status=new StatusUpdate(pointer)
        status.inReplyToStatusId=lastTweet.id
        lastTweet=twitter.updateStatus(status)
      }else{
        lastTweet=twitter.updateStatus(pointer)
      }
    }catch(Throwable e){
      e.printStackTrace()
    }
  }
  println "Tweeting finished"
  break
////
case "mylast":
  def count=20
  if(bodyInLines){
    try{
      count=Integer.valueOf(bodyInLines[0])
    }catch(Throwable e){}
  }
  def paging=new Paging(1,count)
  def statuses=twitter.getUserTimeline(twitter.verifyCredentials().screenName,paging)
  printStatusIterated statuses
  break
////
case "oneslast":
  def count=20
  if(bodyInLines.size()>1){
    try{
      count=Integer.valueOf(bodyInLines[1])
    }catch(Throwable e){}
  }
  def paging=new Paging(1,count)
  def statuses=twitter.getUserTimeline(bodyInLines[0],paging)
  printStatusIterated statuses
  break
////
case "timeline":
  def count=20
  if(bodyInLines){
    try{
      count=Integer.valueOf(bodyInLines[0])
    }catch(Throwable e){}
  }
  def paging=new Paging(1,count)
  def statuses=twitter.getHomeTimeline(paging)
  printStatusIterated statuses
  break
////
default:
  // maybe a real issue...
  break
}
