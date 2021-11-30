# StudyBot (Work in progress)

I started this project to learn more about Java.

The goal for this bot is to provide assistance while learning online at the University of Newcastle.

The project uses [JDA](https://github.com/DV8FromTheWorld/JDA) to build the bot and interact with Discord's API.

## Usage

### Importing A Program Handbook

Using the slash command `/import` followed by the absolute URL to the program handbook for a course; every course is created as 
a mentionable role and text channel.

#### Required Parameters

URL: absolute URL to the program handbook

#### Optional Parameters

Semester: can be either "Semester 1" or "Semester 2", default is both semesters.

Year: can be either the current year or the next year, default is current year.
