java-leaderboard
================

Leaderboards backed by Redis in Java, http://redis.io.

Builds off ideas proposed in http://blog.agoragames.com/2011/01/01/creating-high-score-tables-leaderboards-using-redis/.

Installation
============

You should be able to use the following as a dependency in your Maven pom.xml:

    <dependency>
      <groupId>com.agoragames</groupId>
      <artifactId>leaderboard</artifactId>
      <version>2.0.0</version>
    </dependency>
	
Make sure your redis server is running! Redis configuration is outside the scope of this README, but 
check out the Redis documentation, http://redis.io/documentation.

Compatibility
============

The JAR has been built under OS X using the following Java Runtime:

    java version "1.6.0_24"
    Java(TM) SE Runtime Environment (build 1.6.0_24-b07-334-10M3326)
    Java HotSpot(TM) 64-Bit Server VM (build 19.1-b02-334, mixed mode)
	
Usage
============

Future Ideas
============
  
Contributing to java-leaderboard
================================
 
* Check out the latest master to make sure the feature hasn't been implemented or the bug hasn't been fixed yet
* Check out the issue tracker to make sure someone already hasn't requested it and/or contributed it
* Fork the project
* Start a feature/bugfix branch
* Commit and push until you are happy with your contribution
* Make sure to add tests for it. This is important so I don't break it in a future version unintentionally.
* Please try not to mess with the pom.xml, version, or history. If you want to have your own version, or is otherwise necessary, that is fine, but please isolate to its own commit so I can cherry-pick around it.

Copyright
============

Copyright (c) 2011 David Czarnecki. See LICENSE.txt for further details.

