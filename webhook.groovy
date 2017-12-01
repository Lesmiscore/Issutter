#!/usr/bin/env groovy

// This script is used to receive Webhooks from GitHub.

@Grab('com.github.nao20010128nao:HttpServerJava:d24e696')
@GrabResolver(name='jitpack',root='https://jitpack.io')
import net.freeutils.httpserver.HTTPServer

import groovy.json.*
import java.security.*

def parseTwitter={params->
  // AccessToken, AccessTokenSecret, ConsumerKey, ConsumerSecret
  ["twitterAk","twitterAs","twitterCk","twitterCs"].collect{ params[it] }
}

def parseGitHub={params->
  ["githubUser","githubPass","secret"].collect{ params[it] }
}

def markdownToText={md->
  for(def file: ["./escape-markdown.groovy","./escape-markdown-2.groovy"]){
    def result=null
    file.execute().with{
      out.write(md.getBytes("utf-8"))
      out.flush()
      out.close()
      
      if(!waitFor()){
        result = in.text
      }else{
        println err.text
      }
    }
    if(result!=null){
      return result
    }
  }
  throw new RuntimeException("Error: Failed to convert Markdown to text")
}.memoize()

def queryTwitter={query,ak,asec,ck,cs->
  def tokens=[
    "twitter4j.oauth.accessToken"       :"$ak",
    "twitter4j.oauth.accessTokenSecret" :"$asec".toString(),
    "twitter4j.oauth.consumerKey"       :"$ck".toString(),
    "twitter4j.oauth.consumerSecret"    :"$cs".toString()
  ].collect { "-D$it.key=$it.value".toString() }
  return new ProcessBuilder("groovy",*tokens,"./twitter.groovy").with{
    redirectErrorStream(true)
    start()
  }.with{
    out.write(query.getBytes("utf-8"))
    out.flush()
    out.close()

    waitFor()
    return in.text
  }
}

def server=new HTTPServer(8080)

server.getVirtualHost(null).with {
  addContext('/created',{req,resp->
    try{
      println 'Requested'
      assert req.method.toLowerCase()=="post"
      def params=req.params
      def check=params.check.bytes
      assert MessageDigest.getInstance("sha-256").digest(check)
          .encodeHex().toString().toLowerCase()=="d2cd4ea23cbdf757840c506ae2f305fe652a09c99ebeb5ebc3cd0defda48ecaa"
      
      def json=new JsonSlurper().parseText(req.body.text)
      
      resp.headers.add("Content-Length","0")
      resp.sendHeaders 200
      
      new Thread({
        try{
          def twitterTokens=parseTwitter(params)
          def (ghUser,ghPass,ghChecksum)=parseGitHub(params)
          assert true

          def issue=json.issue
          // Checks that we have called from the right author
          if(issue.user.login.toLowerCase()!=ghUser.toLowerCase())return
          // Checks that the user *CREATED* the issue
          if(json.action!="opened")return

          def issueBody=issue.body
          def query=markdownToText(issueBody)
          def replyForIssue="Auto Message\n\n```\n"+queryTwitter(query,*twitterTokens)+"\n```\n"
          
          def commentUrl=issue.comments_url
          def githubAuth="$ghUser:$ghPass".bytes.encodeBase64()
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
  },"POST")
}

server.start()

println 'Ready'

