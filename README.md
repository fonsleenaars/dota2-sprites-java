# dota2-sprites-java
Small java project to automatically download the latest Dota2 Hero &amp; Item sprites, and create a CSS file to go with it.

I quickly put this together to use in my own projects, it's a bit messy but the gist of it works, feel free to fork & improve as you see fit!

## Usage
Grab your own API key here: [steamcommunity.com/dev/apikey](http://steamcommunity.com/dev/apikey)
You will need your API key in order to fetch the hero & item data from the API.

If you make any changes to the project in your IDE, build & run it, otherwise just enter the target directory in a command prompt and enter the following command to run the project. 
> java -jar Dota2Sprites-1.0.jar <your-steam-api-key>

If no arguments are specified after the JAR name, it will put the sprites and CSS into your user.home/dota2sprites directory (C:/User/<username>/dota2sprites) on windows.

Otherwise you can specify an output directory like so:
> java -jar Dota2Sprites-1.0.jar <your-steam-api-key> <output-directory>
