# Build Monitor Maven Plugin for AEM <br>(buildmon)

This plugin performs some useful tasks while an AEM project is being built, especially on a developer's machine:

* Holds back finalization of a build until the target AEM instance is truly up and running (the execution goal is *wait*). When run in a developer's machine, saves one from refreshing a browser page multiple times before the instance becomes responsive.

* Verifies if a correct AEM instance is running at the given address before deploying new packages (the execution goal is *verify*). When run in a developer's machine, helps one to avoid deploying irrelevant content (JSPs, libraries overlays, etc.), thus "spoiling" an AEM instance.

* Can be used for non-AEM project building/deployment scenarios as well.

### Installation and usage

There are two options:

#### A. POM file

Add to the "plugins" section of one or more relevant POMs as in the following snippet:

```xml
<plugin>
    <groupId>com.paperspacecraft.aem</groupId>
    <artifactId>buildmon</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <goals>
                <!-- Use one of the two goals currently available: 
                     "verify" and "wait" --> 
                <goal>wait</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- See the full list of config options below -->
        <endpoint>http://localhost:4502/content/index.html</endpoint>
        <mustContainHtml>body .some-container .some-nested-tag</mustContainHtml>
    </configuration>
</plugin>
```

#### B. Command line (does not affect a project's codebase)

1) Locate your user-level or system-level Maven settings (*settings.xml*).
   
2) Add or modify the *pluginGroups* section in *settings.xml* to be able to use the brief plugin name in the command line:
```xml
<pluginGroups>
    <pluginGroup>com.paperspacecraft.aem</pluginGroup>
</pluginGroups>
```

3) Create a "common" Maven profile *settings.xml* and put plugin configuration in it:
```xml
<profile>
    <!-- Put any ID you will use further -->
    <id>buildmon-local</id>
    <properties>
        <!-- See the full list of config options below -->
        <buildmon.endpoint>http://localhost:4502/content/index.html</buildmon.endpoint>
        <buildmon.mustContainHtml>body footer search-box</buildmon.mustContainHtml>
    </properties>
</profile>
```
4) Run your Maven build with a command line like the following:
```
mvn clean install buildmon:wait -Psome-profile -Psome-other-profile -Pbuildmon-local
```

Or, to verify that the proper AEM instance is on, run:
```
mvn buildmon:verify clean install -Psome-profile -Psome-other-profile -Pbuildmon-local
```

NB: if you need another set of settings for a different project, just create another "common" profile.

### Configuration

The following config options are supported for both the *verify* and *wait* goals:

**endpoint** - the HTTP address the plugin will attempt to reach. Before contacting the endpoint the build flow is paused. In case the endpoint is accessible / when it becomes accessible:
- If the goal is *verify*, the build continues. Otherwise, an exception is thrown, and the build terminates;
- If the goal is *wait*, the waiting is over, and, normally, the build finishes.

When in *settings.xml* file, specify *buildmon.endpoint*.  
If not specified, the endpoint is "http://localhost:4502".

By default, the plugin just "pings" the endpoint, whatever it is. 
Below you'll find more options to verify that the endpoint is the right one.

**login** - the username to use for the request.

When in the *settings.xml* file, specify *buildmon.login*.
The default value is "admin".

**password** - the password to use for the request. 

When in the *settings.xml* file, specify *buildmon.password*.
The default value is "admin".

**pollingInterval** - used for the "wait" goal. Interval (in seconds) between requests to the endpoint until it responds.

When in the *settings.xml* file, specify *buildmon.pollingInterval*.
The default value is 3 sec.

**maxWaiting** - used for the "wait" goal. Maximal duration (in seconds) of the plugin trying to contact the endpoint. This setting is specified so that the plugin does not "hang" endlessly. A warning is issued if the limit is reached, and yet there's not a valid response.

When in the *settings.xml* file, specify *buildmon.maxWaiting*.
The default value is 120 sec.

**pollAfter** - used for the "wait" goal. Time (in seconds) before sending the first request to the endpoint. Might be
useful if the endpoint / "must contain" constraints are set up in such a manner that the endpoint might be available
immediately after the build finishes but then becomes unavailable for a while (due to bundles restarting, etc.).

When in the *settings.xml* file, specify *buildmon.pollAfter*.
The default value is 0 (the first request is sent immediately).

**mustContainHtml** - used to specify that the endpoint must not just be reachable but respond with a valid HTML. Useful to rule out that the endpoint gives back a "Startup in progress" or some "404 not found" page.
The value is a CSS-style selector (to be precise, the format recognizable by the *Jsoup* library).
Some good candidate is:
```
<endpoint>http://localhost:4502/content/some/page.html</endpoint>
<mustContainHtml>.container > .nested-container #some-button</mustContainHtml> <!-- ...etc. -->
```
NB: prefer the tags that are rendered to make sure that the whole page is ready (not some "header" that can sit on top of a still blank page).

If you want to make sure that not only a specific HTML entity is present in the page, but it contains a particular text, use notation as the following: `<mustContainHtml>.container > .nested-container #some-button | Title to look for</mustContainHtml>`

When in the *settings.xml* file, specify *buildmon.mustContainHtml*.

**mustContainText** - an alternative that is much like the *mustContainHtml* but just searches for plain text. Can be used, e.g., when the endpoint is not an HTML page but a JSON. 

When in the *settings.xml* file, specify *buildmon.mustContainText*.

### Lifecycle phases

The "wait" goal is bound to the *install* lifecycle phase by default. The "verify" goal is bound to the *verify* phase. You can specify other phases in the "execution" section of your POM file.

### Sequential / parallel run

If the plugin is active for more than one module, it will only trigger the "wait" within the last module in the reactor - i.e., immediately before the end of the whole build.

The plugin seems to be safe and effective for multi-thread builds. 

### Licensing

The project is distributed under the Apache 2.0 license. See [LICENSE](LICENSE) for details. 
