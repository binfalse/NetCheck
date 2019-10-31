#!/bin/bash
if [ -f NetCheck.svg ]
then
    convert NetCheck.svg -background '#566BA6' -gravity center -extent 700x700 -resize 512 NetCheck-google-play.png
else
    echo "no NetCheck.svg"
    exit 1
fi
