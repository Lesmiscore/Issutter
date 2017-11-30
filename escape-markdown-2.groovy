#!/usr/bin/env groovy
// This script cleans up all elements of Markdown

@Grab("io.github.gitbucket:markedj:1.0.13")
import io.github.gitbucket.markedj.*

@Grab("org.jsoup:jsoup:1.11.2")
import org.jsoup.Jsoup

def mdString=System.in.text

def mdOpt=new Options()
def html=Marked.marked(mdString)

//System.err.println html

def soupDoc=Jsoup.parse(html)
println soupDoc.wholeText()
