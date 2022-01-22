# Bertilware Vault
A secure and open source password manager that is written in Java and uses the JavaFX library.

## Features
* Encrypts every user's data using AES-128.
* Supports syncing data between two devices connected to the same network.
* Supports importing/exporting passwords from/to browsers.
* Includes search functionality to help manage huge numbers of saved passwords.
* Includes various themes!

## Installation
You can find installer packages in the releases section.

The installer packages are compiled using Inno Setup and include:
* A jar file, compiled using Apache Maven.
* An executable file, compiled using Launch4j.
* A bundled Java Runtime Environment, compiled by running jlink on an Eclipse Adoptium JDK, that also includes JavaFX.
* A license file.
* An icon file. 

## Notes
If you plan on using the syncing feature, make sure you use **_the same username and password on both devices._**

All user files are saved within `/.bertilware/vault/` in your home directory. 
