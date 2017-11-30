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
  println sdf.format(stat.date)
  println "RT $stat.retweetCount, Fav $stat.favoriteCount"
}

switch(head){
////
case "tweet":
  twitter.updateStatus(body)
  println "Tweeted"
  break
////
case "rentwi":
  def bodyInLines=body.readLines()
  def tweets=[false]
  bodyInLines.each{ln->
    if(ln.matches("={3,}")){
      tweets.add(false)
    }else{
      def last=tweets.last()
      if(last){
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
        def status=new StatusUpdate(poiner)
        status.inReplyToStatusId=lastTweet.inReplyToStatusId
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
case "mylast10":
  def paging=new Paging(1,10)
  def statuses=twitter.getUserTimeline(twitter.verifyCredentials().screenName,paging)
  def index=0
  statuses.each{tw->
    index++
    println "========== ($index/10)"
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
  break
////
}
