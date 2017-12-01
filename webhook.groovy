#!/usr/bin/env groovy

// This script is used to receive Webhooks from GitHub.

@Grab('com.github.nao20010128nao:HttpServerJava:d24e696')
@GrabResolver(name='jitpack',root='https://jitpack.io')
import net.freeutils.httpserver.HTTPServer

import groovy.json.*

def parseTwitter={params->
  // AccessToken, AccessTokenSecret, ConsumerKey, ConsumerSecret
  ["twitterAk","twitterAs","twitterCk","twitterCs"].collect{ params[it] }
}

def parseGitHub={params->
  ["githubUser","githubPass","githubChecksum"].collect{ params[it] }
}

def markdownToText={md->
  for(def file: ["./escape-markdown.groovy","./escape-markdown-2.groovy"]){
    file.execute().with{
      out.write(md.getBytes("utf-8"))
      out.flush()
      
      if(!waitFor()){
        return in.text
      }
    }
  }
  throw new RuntimeException("Error: Failed to convert Markdown to text")
}.memoize()

def queryTwitter={query,ak,asec,ck,cs->
  new ProcessBuilder("./twitter.groovy").with{
    environment().putAll([
      "twitter4j.oauth.accessToken"       :"$ak",
      "twitter4j.oauth.accessTokenSecret" :"$asec",
      "twitter4j.oauth.consumerKey"       :"$ck",
      "twitter4j.oauth.consumerSecret"    :"$cs"
    ])
    redirectErrorStream(true)
    start()
  }.in.text
}

def server=new HTTPServer(8080)

server.getVirtualHost(null).with {
  addContext('/created'){req,resp->
    try{
      println 'Requested'
      assert req.method.toLowerCase()=="post"
      def params=req.params
      
      def (ak,asec,ck,cs)=parseTwitter(params)
      def (ghUser,ghPass,ghChecksum)=parseGitHub(params)
      
      def json=new JsonSlurper().parseText(req.body.text)
      def issue=json.issue
      // Checks that we have called from the right author
      assert issue.user.name.toLowerCase()==ghUser.toLowerCase()
      // Checks that the user *CREATED* the issue
      assert issue.action=="opened"
      // Checks that the issue is not closed
      assert !issue.closed_at
      
      resp.headers.add("Content-Length","0")
      resp.sendHeaders 200
      
      new Thread({
        try{
          def issueBody=issue.body
          def query=markdownToText(issueBody)
          def replyForIssue="Auto Message\n\n```\n"+queryTwitter(query)+"\n```\n"
          
          def commentUrl=issue.comments_url
          def githubAuth="$githubUser:$githubPass".bytes.encodeBase64()
          new URL(commentUrl).openConnection().with{
            setRequestProperty("Authorization", "Basic $githubAuth")
            requestMethod="post"
            doOutput = true
            doInput = true
            outputStream.write JsonOutput.toJson([body: replyForIssue]).bytes
            inputStream.text
          }
        }catch(Throwable e){
          e.printStackTrace()
        }
      }).start()
    }catch(Throwable e){
      e.printStackTrace()
      throw e
    }
    0
  }
}

server.start()

println 'Ready'

