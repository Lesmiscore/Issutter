#!/usr/bin/env groovy
// This script cleans up all elements of Markdown

@Grab("com.vladsch.flexmark:flexmark-all:0.28.12")
import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.util.options.MutableDataSet

@Grab("org.jsoup:jsoup:1.11.2")
import org.jsoup.Jsoup

def mdString=System.in.text

def parser = Parser.builder().build()
def renderer = HtmlRenderer.builder().build()

def document = parser.parse(mdString)
def html=renderer.render(document)

//System.err.println html

def soupDoc=Jsoup.parse(html)
println soupDoc.wholeText()
