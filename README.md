# Issutter
Use Twitter from GitHub's Issue

# Usage

## Launch Webhook server
1. `git clone https://github.com/nao20010128nao/Issutter`
2. `./dep.sh` (Required only one time)
3. Decide a secret password
4. Hash it with `echo <CHECK PASSWORD> | sha256d`
5. Edit line 12 of `webhook.groovy`
6. `./webhook.groovy`
7. The server will start on port 8080


## Get Twitter OAuth token
Go to [`api.twitter.com`](https://api.twitter.com) to get one.    
You will need `Consumer Key`, `Consumer Secret`, `Access Token`, and `Access Token Secret`.

## Create a GitHub repository with `README` or `LICENSE`
You need at least one file because `Issues` won't appear without commits.

## Configure GitHub webhook
1. Go `Settings` -> `Webhook` on the repository

### Run when Issue is created
1. The URL should be:    
   `http://<HOST>/created?check=<CHECK PASSWORD>&githubUser=<GITHUB USER>&githubPass=<GITHUB PASSWORD>&twitterAk=<TWITTER ACCESS TOKEN>&twitterAs=<TWITTER ACCESS TOKEN SECRET>&twitterCk=<TWITTER CONSUMER KEY>&twitterCs=<TWITTER CONSUMER SECRET>`
2. Type anything for `Secret`
3. Choose only `Issues` for events.

### Run when Issue is commented
1. The URL should be:    
   `http://<HOST>/commented?check=<CHECK PASSWORD>&githubUser=<GITHUB USER>&githubPass=<GITHUB PASSWORD>&twitterAk=<TWITTER ACCESS TOKEN>&twitterAs=<TWITTER ACCESS TOKEN SECRET>&twitterCk=<TWITTER CONSUMER KEY>&twitterCs=<TWITTER CONSUMER SECRET>`
2. Type anything for `Secret`
3. Choose only `Issue Comments` for events.

# What to Issue
The text will be parsed as Markdown and all styles will be **stripped**.    
You need to put one command per issue/comment.    
Any errors and results will be notified as a Issue comment.    
The Issue text and comment is basically consist of following structure:

```
<COMMAND>    
<ARGUMENTS>    
<ARGUMENTS>    
```

You need at least 2 spaces to break a line, like this Markdown does.

`{CONSTANT}` means a constant text.    
`<REQUIRED>` means required text.    
`[OPTIONAL]` means optional text.

## Check my timeline

```
{timeline}
[N]
```

`N` means number of tweets to display. Default is `20`.    

## Check my last N tweets

```
{mylast}
[N]
```

`N` means number of tweets to display. Default is `20`.    

## Single Tweet

```
{tweet}
<TEXT>
```

`TEXT` means a text to tweet.    

## Do multiple tweet and chain as replies

```
{rentwi}
<TEXT>
{%%%%%%%...}
<TEXT>
{%%%%%%%...}
...
```

`TEXT` means a text to tweet.   
`%%%%%%%...` means end of a tweet. Requires more than 3 character of `%`.   
Repeat `TEXT` and `%%%%%%%...` to do multiple tweet.

## Check someone's last N tweets

```
{oneslast}
<ACCOUNT>
[N]
```

`ACCOUNT` means an account to display.    
`N` means number of tweets to display. Default is `20`.    
