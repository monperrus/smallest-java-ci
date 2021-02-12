A small Java Continuous Integration server.
===========================================================
This is a simple server for Continuous Integration development. It is meant to be called as webhook by Github. The HTTP part of it is based on Jetty. We use Maven for building and managing our project.

We assume here that you have a standard Linux machine (eg with Ubuntu), with Java and Maven installed. 


## How to run:
After checking out the repository, build it in the root directory using the following command:

```
mvn package
```

Then start the server on your local machine:
```
java -jar target/gs-maven-0.1.0.jar
```

The serverr is visible on the Internet by using [Ngrok](https://ngrok.com/). The public url can be found by running the following commnand in a second terminal window:
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
