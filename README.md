A small Java Continuous Integration server.
===========================================================
This is a simple server for Continuous Integration development. It is meant to be called as webhook by Github. The HTTP part of it is based on Jetty. Maven is used to build and test, and notifications to the repository are sent through the GitHub status API. The server keeps a history of the past builds and log files attached to them. 

## Contributions
**Philip Andersson (CSCphilp):** Maven handling, JSON handling, bug fixes

**Zehua Guo (gzh0528):** Cloning, building and testing the repository, Build History

**Jonatan Yao Håkansson (jonte450):** Testing,Notifify function helped together with Kalle

**Elisabet Lövkvist (SQUEEEE):** Documentation, code skeleton for server functions, tests

**Kalle Meurman (Wizkas0):** Cloning the repo, sending notification to GitHub

## How to run:
We assume here that you have a standard Linux machine (eg with Ubuntu), with Java and Maven installed. After checking out the repository, build it in the root directory using the following command:

```
mvn package
```

Then start the server on your local machine:
```
java -jar target/gs-maven-0.1.0.jar
```
## Using Ngrok to connect the server to GitHub
The server can be made visible on the Internet by using [Ngrok](https://ngrok.com/). 

### Download Ngrok
First you need to download it:

```
curl -LO --tlsv1 https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok-stable-linux-amd64.zip 
```
### Run Ngrok and connect to GitHub

The public url can be found by running the following commnand in a separate terminal window to the one running the server (in the same folder as Ngrok was downloaded):
```
# open a second terminal window
# this gives you the public URL of your CI server to set in Github
# copy-paste the forwarding URL "Forwarding                    http://8929b010.ngrok.io -> localhost:8080"
# note that this url is short-lived, and is reset everytime you run ngrok
./ngrok http 8080
```
Copy the url looking like [number sequence].ngrok.io, then go to the GitHub repository you want to the server to monitor. 

* go to `Settings >> Webhooks`, click on `Add webhook`.
* paste the forwarding URL (eg `http://8929b010.ngrok.io`) in field `Payload URL`) and send click on `Add webhook`. 
* **Set the content type to application/json**

We test that everything works:

* go to <http://localhost:8080> tp check that the CI server is running locally
* go to your Ngrok forwarding URL (eg <http://8929b010.ngrok.io>) to check that the CI server is visible from the internet, hence visible from Github
* make a commit in your repository
* observe the result, in two ways:
  * locally: in the console of your first terminal window, observe the requested URL printed on the console
  * on github: go to `Settings >> Webhooks` in your repo, click on your newly created webhook, scroll down to "Recent Deliveries", click on the last delivery and the on the `Response tab`, you'll see the output of your server `CI job done`
  * on ngrok: raise the terminal window with Ngrok, and you'll also the see URLs requested by Github.

We shutdown everything:

* `Ctrl-C` in the ngrok terminal window
* `Ctrl-C` in the ngrok java window
* delete the webhook in the webhook configuration page.

Notes:
* by default, Github delivers a `push` JSON payload, documented here: <https://developer.github.com/v3/activity/events/types/#pushevent>, this information can be used to get interesting information about the commit that has just been pushed.

### View history of past builds
The history data is stored in data.json. To see the history, you can click the html file called History.html in the Build_History folder. The html page contains a list of URLs. By clicking each of them, you will see the log of that build. 
