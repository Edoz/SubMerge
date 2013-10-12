SubMerge
========

java utility to offset the start time of a .srt subtitle file, as well as merge multiple .srt subtitle files offsetting the start time of each

created this because I had several short films merged into a single mkv file and needed to merge the subtitles of each to create a subtitle srt file that was properly offset and formatted to work with the single mkv video file. Too tedious to do manually so I wrote some code to do it then decided to polish and publish it

Usage
========

You can use this utility to offset a single subtitle file to a new start time and/or to merge multiple subtitle files into a single (correctly formatted) srt file. You'll need the proper start time for each of the files

how to use:

clone repo

put your srt file(s) in the SubMerge directory (same directory with folders submerge and submergetests)

open terminal/command prompt

cd into SubMerge/submerge

javac *.java

cd ..

java submerge.SubMerge

follow instructions
