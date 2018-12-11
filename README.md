
# Cifi

Cifi helps you deploy automation from github releases.

## Setup

Create a user (eg: *ucifi*) 

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

Make desired directory (eg: */apps*) with appropriate permissions

```sh
sudo mkdir -p /apps/cifi
chown -R ucifi:ucifi /apps
```

Login as ucifi

```sh
su ucifi
Password:
cd /apps/cifi/
```

Download [latest](https://github.com/anandchakru/cifi/releases/latest) cifi

```sh
curl -o /app/cifi/cifi.jar -H 'Accept: application/octet-stream' -JL $(curl -s https://api.github.com/repos/anandchakru/rason/releases/latest | grep '"browser_download_url":' | sed -E 's/.*"([^"]+)".*/\1/')
```

Add desired settings in application.yml (this will override whatever is in src/main/resources/application.yml)

```sh
touch /app/cifi/application.yml
```

Make directory for target (eg: rason)

```sh
mkdir -p /apps/rason
```

Start cifi in a sub shell with current directory set to /apps/rason/ (,. or whichever dir you prefer)

```sh
(cd /apps/rason/ && nohup java -jar /apps/cifi/cifi4-1.0.1.jar &)
```

## Few other things:

 - Sample app - [rason](https://github.com/anandchakru/rason), managed by cifi is live [here](https://rason.jrvite.com/index).

 - Use ApplicationPidFileWriter to capture the pid after application starts.
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

## Tasks
- [x] Lean app to manage automatic deploys from github release (eg: [rason](https://github.com/anandchakru/rason) after [travis](https://travis-ci.org/anandchakru/rason) does its *magic*.
- [x] Support private repo.
- [ ] Support for ui app.
- [ ] cifi4-master to manage multi-cifi nodes & multi-cluster deployment.
- [ ] Support for windows based environment.