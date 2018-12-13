
# Cifi

Cifi helps you deploy automation from github releases.

 - With all the below setup, here is how it will work out. 
   - I commit the code to local repo
   - If I need it published, I execute [gtag](https://github.com/anandchakru/rason/blob/master/gtag.sh).
     - Bumps the current version and creates a tag
     - Pushes the commits and tags to github. 
   - Travis, [config](https://github.com/anandchakru/rason/blob/master/.travis.yml) 
     - Build all tagged versions
     - Push it to github releases. 
   - Github 
     - Receives release artifact
     - Invokes the webhook. 
   - cifi 
     - Downloads the artifact
     - Shutdown the current application
     - Restart with the new version. 

## Setup
Here is how you'd use cifi to automate deployment of the target application, [rason](https://github.com/anandchakru/rason) on your server.

- Create a user (eg: *ucifi*) 

```sh
sudo adduser ucifi
Adding user ucifi ...
Adding new group ucifi (9004) ...
Adding user ucifi (9004) with group ucifi ...
Creating home directory /home/ucifi ...
Copying files from /etc/skel ...
Enter new UNIX password:
Retype new UNIX password:
passwd: password updated successfully
Changing the user information for ucifi
Enter the new value, or press ENTEr for the default
	Full Name []: Cifi User
	Room Number []: 0
	Work Phone []: 0
	Home Phone []: 0
	Other []:0
Is the information correct? [Y/n]
```

- Make desired directory (eg: */apps*) with appropriate permissions

```sh
sudo mkdir -p /apps/cifi
chown -R ucifi:ucifi /apps
```

- Login as ucifi

```sh
su ucifi
Password:
cd /apps/cifi/
```

- Download [latest](https://github.com/anandchakru/cifi/releases/latest) cifi

```sh
curl -o /app/cifi/cifi.jar -H 'Accept: application/octet-stream' -JL $(curl -s https://api.github.com/repos/anandchakru/rason/releases/latest | grep '"browser_download_url":' | sed -E 's/.*"([^"]+)".*/\1/')
```

- Add desired settings in application.yml (this will override whatever is in src/main/resources/application.yml)

```sh
touch /app/cifi/application.yml
```

- Make directory for target application (eg: [rason](https://github.com/anandchakru/rason))

```sh
mkdir -p /apps/rason
```

- Start cifi in a sub shell with current directory set to /apps/rason/ (,. or whichever dir you prefer)

```sh
(cd /apps/rason/ && nohup java -jar /apps/cifi/cifi4-1.0.1.jar &)
```

- Now cifi listens to github releases events, and if there is a new release, cifi deployes and restarts the server for you.

## Few other things:
 - Sample nginx config to listen on github's webhook. On https://github.com/anandchakru/rason/settings/hooks/ this is what we have `https://ops.jrvite.com/wh/gh/rason`
```sh
server {
        server_name ops.jrvite.com;
	listen 443 ssl;
        location = /wh/gh {
                proxy_pass https://localhost:8089;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header X-NginX-Proxy true;
                proxy_ssl_session_reuse off;
                proxy_set_header Host $http_host;
                proxy_cache_bypass $http_upgrade;
                proxy_redirect off;
        }
}
```
**Note:** ssl config is off topic, to quickly test, you mat remove `listen 443 ssl;` and setup http url in github webhooks.

 - Sample app - [rason](https://github.com/anandchakru/rason), managed by cifi is live [here](https://rason.jrvite.com/index).

 - Recommend using ApplicationPidFileWriter to capture the pid after application starts.
 
```java
@Import({ RasonConfig.class })
@SpringBootApplication
public class RasonInitializer {
	public static void main(String[] args) {
		SpringApplication app = new SpringApplicationBuilder(RasonInitializer.class).build(args);
		app.addListeners(new ApplicationPidFileWriter());
		app.run();
	}
}
//..
spring: 
  application: 
    name: rason	
  pid:
    fail-on-write-error: true
    file: ${spring.application.name}.pid
```
 
 There goes my own little *continuous integration and continuous delivery*!
 
 - Feedback, comments - [@anandchakru](https://twitter.com/anandchakru)

## Tasks
- [x] Lean app to manage automatic deploys from github release (eg: [rason](https://github.com/anandchakru/rason) after [travis](https://travis-ci.org/anandchakru/rason) does its *magic*.
- [x] Support private repo.
- [ ] Support for ui app.
- [ ] cifi-master to manage multi-cifi nodes & multi-cluster deployment.
- [ ] Support for windows based environment.
