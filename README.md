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

