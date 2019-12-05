# Repository for the Text Visualization on the Media Wall

* The scripts export individual frames into the `frames` folder (which is not synced with this repo).
* To convert the frames to an `.mp4` file use the following script: `ffmpeg -r 60 -f image2 -i <inputFileName> -vcodec libx264 -crf 20 -pix_fmt yuv420p <outputFileName>`.
* This script uses the `ffmpeg` package in the command line.
* The `<inputFileName>` that we used followed something like this: `%04d.png`
