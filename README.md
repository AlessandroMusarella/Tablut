# Tablut Challenge 2021

Project for the [Tablut Competition](http://ai.unibo.it/games/boardgamecompetition/tablut),
Fondamenti di Intelligenza Artificiale M UNIBO.
The project realizes a player for an ancient Nordic strategy board game: Tablut.


## Requirements

To run this application a working Java environment is required. Software is tested on Java version 8.
To install Java in your Ubuntu/Debian machine you can execute the following command:

```bash
sudo apt install openjdk-8-jdk
```

## Usage

You can run the player with:
```bash
cd Executables
java -jar Mr.Meeseeks.jar <white|black> [<timeout>] [<server_address>]
```

If not specified, the default values are **60** for `timeout` and **localhost** for `server_address`.

Alternatively, you can run the player with the bash script provided:

```bash
./runmyplayer  <white|black> [<timeout>] [<server_address>]
```
With the same default values written above.

For convenience, we provide the Server. You can find information about it in the [main repository](https://github.com/AGalassi/TablutCompetition)

## Authors

Simone Amorati, Alessandro Musarella, Giulio Tripi