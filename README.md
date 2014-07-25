# Medusa
*A Programming Framework for Crowd-Sensing Applications*

Have you ever wished, in the middle of a research project, for a 
convenient way to recruit smartphone and tablet users worldwide 
to contribute data (images, videos, sound) using the sensors on 
their devices for your project?

Medusa is software that makes this capability, that we call 
crowd-sensing, possible. Medusa will let you program a crowd-sensing 
task easily and its software will automatically handle the tedious 
aspects of crowd-sensing: recruiting contributors, paying them, 
and processing and collecting data from mobile devices. 
Medusa also respects contributor privacy and anonymity.

The [demo video](http://www.youtube.com/watch?v=jL1dGA21ciA) explains 
Medusa with an example.

## Project Details

Medusa was developed by Moo-Ryong Ra in collaboration with Bin Liu, Tom La Porta, Ramesh Govindan, and Matthew McCartney at [USC](http://www.usc.edu).

A thorough description of Medusa can be found at the Networked Systems Lab [project page for Medusa](http://nsl.cs.usc.edu/Projects/Medusa) 

## Getting Started

Please read the text in "Project Home" link above first to understand what the Medusa system does. This document provides a way on how to configure Medusa client and cloud components to your environment.

### Requirements

This source code is tested in the following platforms and devices.

- Medusa Cloud
    - OS: Ubuntu 9.04/10.04 And Linux Mint 15
        - package required. (may use 'apt-get install' command)
            - to use mysql: python-mysqldb
            - to use php: php5-common libapache2-mod-php5 php5-cli php5-curl
            - to use sendmail: sendmail-bin
    - TaskTracker
        - python 2.6.x
        - LAMP
            - Ubuntu 9.04(jaunty), 10.04(lucid)
            - Apache web server
            - Mysql 5.0.x
            - PHP
    - WorkerManager
        - apache-tomcat-6.0.35 with jdk-1.5.x.
    - Websocket (GCM alternative)
        - Requirements
            - PHP
                - [PECL](http://pecl.php.net/) Extension for PHP (aka, PEAR)
                    - apt-get install php-pear
                    - apt-get install php5-dev
                    - apt-get install php5-mcrypt
            - [ZeroMQ](http://zeromq.org/) for communication between the existing Medusa Server and the new WebSocket Server
                - Follow [these](http://zeromq.org/bindings:php) instructions
                    - Download the source, extract, and cd into it
                    - ./autogen, ./configure, make, and then sudo make install
                - sudo pecl install zmq-beta
                - add extension=zmq.so to php.ini
        - [Other Considerations](http://socketo.me/docs/deploy)
            - if you plan on connecting hundreds of users, you may want to update linux’s maximum number of allowed per-process file descriptors
                - see [ulimit](http://ss64.com/bash/ulimit.html)
            - [Libevent](http://libevent.org/) speed’s up server-side web socket operations
                - sudo apt-get install libevent libevent-dev
                - sudo pecl install libevent
                - add extension=libevent.so to php.ini
- Android client.
    - Google Nexus I with Android 2.3.6
    - Samsung Galaxy Nexus with Android 4.0.4