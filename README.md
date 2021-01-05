# Streamera

This repo aims to build an android app to stream phone camera feed to a computer using sockets.

## Installation:
Download the apk file at [link](https://github.com/SubZer0811/streamera/releases) on to your android phone and install it on your phone.
Download server.py on your computer. Make sure the computer has python3 installed.

## Running
Make sure that both the phone and the laptop are connected to the same network.

### server
Run the following command on your computer.
```
python3 server.py <ip-address of your computer> <an open port on your computer>
```
To get the ip-address of your computer, run the following command ```ifconfig``` and find the inet value.

### app (client)
Run the app on your phone. Make sure to grant permission for the camera. 
<br>Enter the correct server IP and PORT. Press any of mode that you would like to use.

## Why this application???
There have been a lot of times when I needed to test out some Image Processing or Computer Vision algorithms. An issue that I used to face was trying to get a proper test image. I either had to take pictures, download them to my computer and then run my algorithm or I had to use the built-in camera on my computer.  Both methods have serious drawbacks. Hence I put together this app to solve these issues. A reason that I did not use existing apps was that I wanted the application running on my laptop to be coded in python (this is where I do most of my CV work). This also allowed me to learn android app development. 

## Demo
